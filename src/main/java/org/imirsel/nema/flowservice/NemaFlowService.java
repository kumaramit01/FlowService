package org.imirsel.nema.flowservice;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.jcr.Credentials;
import javax.jcr.SimpleCredentials;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.imirsel.nema.NoSuchEntityException;
import org.imirsel.nema.contentrepository.client.ArtifactService;
import org.imirsel.nema.contentrepository.client.ContentRepositoryServiceException;
import org.imirsel.nema.dao.DaoFactory;
import org.imirsel.nema.dao.FlowDao;
import org.imirsel.nema.dao.JobDao;
import org.imirsel.nema.dao.JobResultDao;
import org.imirsel.nema.flowservice.config.FlowServiceConfig;
import org.imirsel.nema.flowservice.config.ConfigChangeListener;
import org.imirsel.nema.flowservice.config.MeandreServerProxyConfig;
import org.imirsel.nema.flowservice.config.MeandreServerProxyStatus;
import org.imirsel.nema.flowservice.monitor.JobStatusMonitor;
import org.imirsel.nema.flowservice.notification.JobStatusNotificationCreator;
import org.imirsel.nema.flowservice.notification.NotificationSender;
import org.imirsel.nema.model.Component;
import org.imirsel.nema.model.Flow;
import org.imirsel.nema.model.Job;
import org.imirsel.nema.model.JobResult;
import org.imirsel.nema.model.Notification;
import org.imirsel.nema.model.Property;
import org.imirsel.nema.model.ResourcePath;
import org.imirsel.nema.model.Job.JobStatus;
import org.springframework.orm.ObjectRetrievalFailureException;

/**
 * A {@link FlowService} implementation for the NEMA project.
 * 
 * @author shirk
 * @since 0.4.0
 */
public class NemaFlowService implements FlowService, ConfigChangeListener {

    private static final Logger logger = Logger.getLogger(NemaFlowService.class.getName());
    private ArtifactService artifactService;
    private JobScheduler jobScheduler;
    private JobStatusMonitor jobStatusMonitor;
    private DaoFactory daoFactory;
    private MeandreServerProxyFactory meandreServerProxyFactory;
    private FlowServiceConfig flowServiceConfig;
    private NotificationSender notificationSender;
    private JobStatusNotificationCreator notificationCreator;
    private MeandreServerProxy headServer;

    public NemaFlowService() {
    }

    @PostConstruct
    public void init() {
        logger.info("Initializing NEMA Flow Service...");

        notificationCreator = new JobStatusNotificationCreator(daoFactory);
        try {
            headServer = meandreServerProxyFactory.getServerProxyInstance(flowServiceConfig.getHeadConfig(), true);
        } catch (MeandreServerException e) {
            throw new RuntimeException("Could not instantiate head server.", e);
        }
        flowServiceConfig.addChangeListener(this);

        // Any jobs marked as scheduled in the database will be put back in the
        // queue for execution.
        JobDao jobDao = daoFactory.getJobDao();
        List<Job> scheduledJobs;
        try {
            Session session = jobDao.getSessionFactory().openSession();
            jobDao.startManagedSession(session);
            scheduledJobs = jobDao.getJobsByStatus(Job.JobStatus.SCHEDULED);
            jobDao.endManagedSession();
            session.close();
        } catch (HibernateException e) {
            throw new RuntimeException("Problem searching for scheduled jobs"
                    + " in the database.", e);
        }

        if (scheduledJobs != null && scheduledJobs.size() > 0) {
            logger.info(scheduledJobs.size() + " scheduled jobs found in the "
                    + "database. Jobs will be rescheduled for execution.");
        }
        for (Job job : scheduledJobs) {
            jobScheduler.scheduleJob(job);
            jobStatusMonitor.start(job, notificationCreator);
        }

    }

    /**
     * @see FlowService#abortJob(long)
     */
    @Override
    public void abortJob(long jobId) throws IllegalStateException {
        Job job = daoFactory.getJobDao().findById(jobId, false);
        if (!job.isRunning()) {
            throw new IllegalStateException("Cannot abort job " + jobId
                    + " because it has already completed.");
        }
        try {
            jobScheduler.abortJob(job);
        } catch (MeandreServerException e) {
            throw new ServiceException(
                    "Job " + jobId + " could not be aborted.", e);
        }
    }

    /**
     * @see FlowService#deleteJob(long)
     */
    @Override
    public void deleteJob(long jobId) throws IllegalStateException {
        JobDao jobDao = daoFactory.getJobDao();
        JobResultDao resultDao = daoFactory.getJobResultDao();
        FlowDao flowDao = daoFactory.getFlowDao();

        Job job = jobDao.findById(jobId, false);
        // Job must be finished to be deleted.
        if (!job.isDone()) {
            throw new IllegalArgumentException("Cannot delete job " + jobId
                    + " because it is still running, scheduled or submitted.");
        }
        for (JobResult result : job.getResults()) {
            resultDao.makeTransient(result);
        }
        jobDao.makeTransient(job);
        flowDao.makeTransient(job.getFlow());
        // delete the results from disk?
    }

    /**
     * @see FlowService#executeJob(String, String, String, long, long, String)
     */
    @Override
    public Job executeJob(Credentials credentials, String token, String name, String description,
            long flowInstanceId, long userId, String userEmail) {
        FlowDao flowDao = daoFactory.getFlowDao();
        JobDao jobDao = daoFactory.getJobDao();

        Flow flowInstance = flowDao.findById(flowInstanceId, false);

        Job job = new Job();
        job.setToken(token);
        job.setName(name);
        job.setDescription(description);
        job.setFlow(flowInstance);
        job.setOwnerId(userId);
        job.setOwnerEmail(userEmail);
        SimpleCredentials simpleCreds = (SimpleCredentials) credentials;
        String serializedCreds = simpleCreds.getUserID() + ":"
                + new String(simpleCreds.getPassword());
        job.setCredentials(serializedCreds);
        jobDao.makePersistent(job);

        jobScheduler.scheduleJob(job);
        job.setJobStatus(JobStatus.SCHEDULED);
        job.setScheduleTimestamp(new Date());
        jobDao.makePersistent(job);

        jobStatusMonitor.start(job, notificationCreator);

        return job;
    }

    /**
     * @see FlowService#getScheduledJobs()
     */
    @Override
    public List<Job> getScheduledJobs() {
        return jobScheduler.getScheduledJobs();
    }

    /**
     * @see FlowService#getFlowTemplates()
     */
    @Override
    public Set<Flow> getFlowTemplates() {
        FlowDao flowDao = daoFactory.getFlowDao();

        Set<Flow> flowSet = new HashSet<Flow>();
        flowSet.addAll(flowDao.getFlowTemplates());
        return flowSet;
    }

    /**
     * @see FlowService#getJob(long)
     */
    @Override
    public Job getJob(long jobId) {
        JobDao jobDao = daoFactory.getJobDao();
        Job job;
        try {
            logger.log(Level.FINE, "Retrieving job {0}", jobId);
            job = jobDao.findById(jobId, false);
            logger.log(Level.FINE, "Job {0} has {1} results.", new Object[]{jobId, job.getResults().size()});
        } catch (ObjectRetrievalFailureException e) {
            throw new NoSuchEntityException("Job " + jobId + " does not exist.");
        }
        return job;
    }

    /**
     * @see FlowService#getUserJobs(long)
     */
    @Override
    public List<Job> getUserJobs(long userId) {
        return daoFactory.getJobDao().getJobsByOwnerId(userId);
    }


    /**
     * @see FlowService#getUserJobs(long,String)
     */
    @Override
    public List<Job> getUserJobs(long userId, String keyword) {
       return daoFactory.getJobDao().getJobsByOwnerIdAndKeyword(userId, keyword);
    }

    /**
     * @see FlowService#getUserJobsByTaskId(long,long)
     */
    @Override
    public List<Job> getJobsByTaskId(long userId, long taskId) {
        return daoFactory.getJobDao().getJobsByOwnerIdAndTaskId(userId, taskId);
    }
    /**
     * @see FlowService#getUserNotifications(long)
     */
    @Override
    public List<Notification> getUserNotifications(long userId) {
        return daoFactory.getNotificationDao().getNotificationsByRecipientId(
                userId);
    }

    /**
     * @see FlowService#storeFlowInstance(Flow)
     *
     */
    public Long storeFlowInstance(Flow instance) {
        FlowDao flowDao = daoFactory.getFlowDao();
        flowDao.makePersistent(instance);
        return instance.getId();
    }

    /**
     * @see FlowService#getFlow(long)
     */
    @Override
    public Flow getFlow(long flowId) {
        FlowDao flowDao = daoFactory.getFlowDao();
        Flow flow = flowDao.findById(flowId, false);
        return flow;
    }

    /**
     * @see FlowService#createNewFlow(Credentials, Flow, HashMap, String, long)
     */
    @Override
    public Flow createNewFlow(Credentials credentials, Flow flow,
            HashMap<String, String> paramMap, String flowUri, long userId) {
        String result = null;
        try {
            result = headServer.createFlow(credentials, paramMap, flowUri, userId);
            byte[] flowContent = readFileAsBytes(result);

            assert getArtifactService() != null : "Artifact service is null.";
            assert flow != null : "Flow is null.";
            assert credentials != null : "Credentials are null.";
            assert flowContent != null : "Flowcontent is null.";

            String id = UUID.randomUUID().toString();
            ResourcePath resourcePath = getArtifactService().
                    saveFlow((SimpleCredentials) credentials, flow, id, flowContent);

            String uri = resourcePath.getProtocol() + ":"
                    + resourcePath.getWorkspace() + "://" + resourcePath.getPath();
            flow.setUri(uri);

            storeFlowInstance(flow);
        } catch (MeandreServerException e) {
            throw new ServiceException("Could not create flow: " + flowUri, e);
        } catch (ContentRepositoryServiceException e) {
            throw new ServiceException("Could not create flow: " + flowUri, e);
        } catch (IOException e) {
            throw new ServiceException("Could not create flow: " + flowUri, e);
        }
        return flow;
    }

    /**
     * @see FlowService#getComponentPropertyDataType(Credentials,Component, Flow)
     */
    @Override
    public Map<String, Property> getComponentPropertyDataType(
            Credentials credentials, Component component, String flowUri) {
        Map<String, Property> propertyDataTypes = null;
        try {
            propertyDataTypes = headServer.getComponentPropertyDataType(
                    credentials, component, flowUri);
        } catch (MeandreServerException e) {
            throw new ServiceException("A problem occurred while retrieving "
                    + "component data types for flow: " + flowUri, e);
        }
        return propertyDataTypes;
    }

    /**
     * @see FlowService#getComponents(Credentials,Flow)
     */
    @Override
    public List<Component> getComponents(Credentials credentials, String flowUri) {
        List<Component> components = null;
        try {
            components = headServer.getComponents(credentials, flowUri);
        } catch (MeandreServerException e) {
            throw new ServiceException("A problem occurred while retrieving "
                    + "components for flow: " + flowUri, e);
        }
        return components;
    }

    /**
     * @see FlowService#getConsole(Job)
     */
    @Override
    public String getConsole(Job job) {
        String console = null;
        try {
            console = headServer.getConsole(job.getExecutionInstanceId());
        } catch (MeandreServerException e) {
            throw new ServiceException(
                    "Could not retrieve the console for job " + job.getId(), e);
        }
        return console;
    }

    /**
     * @see FlowService#removeFlow(Flow)
     */
    @Override
    public boolean removeFlow(String flowUri) {
        boolean result = false;
        try {
            result = headServer.removeFlow(flowUri);
        } catch (MeandreServerException e) {
            throw new ServiceException(
                    "A problem occured while trying to remove flow: " + flowUri, e);
        }
        return result;
    }

    /**
     * @see FlowService#getHeadConfig()
     */
    @Override
    public MeandreServerProxyConfig getHeadConfig() {
        return headServer.getConfig();
    }

    /**
     * @see FlowService#getWorkerConfigs()
     */
    @Override
    public Set<MeandreServerProxyConfig> getWorkerConfigs() {
        return flowServiceConfig.getWorkerConfigs();
    }

    /**
     * @see FlowService#getHeadStatus()
     */
    @Override
    public MeandreServerProxyStatus getHeadStatus() {
        return headServer.getStatus();
    }

    /**
     * @see FlowService#getWorkerStatus()
     */
    @Override
    public Map<MeandreServerProxyConfig, MeandreServerProxyStatus> getWorkerStatus() {
        return jobScheduler.getWorkerStatus();
    }

    /**
     * @see FlowService#getWorkerStatus(String, int)
     */
    @Override
    public MeandreServerProxyStatus getWorkerStatus(String host, int port) {
        if (host == null) {
            throw new IllegalArgumentException("Host must not be null.");
        }
        Map<MeandreServerProxyConfig, MeandreServerProxyStatus> status = jobScheduler.getWorkerStatus();
        Iterator<MeandreServerProxyConfig> configIterator = status.keySet().iterator();
        while (configIterator.hasNext()) {
            MeandreServerProxyConfig config = configIterator.next();
            if (host.equals(config.getHost()) && port == config.getPort()) {
                return status.get(config);
            }
        }
        return null;
    }

    /**
     * Return the {@link JobScheduler} instance currently being used.
     *
     * @return Current {@link JobScheduler} instance.
     */
    public JobScheduler getJobScheduler() {
        return jobScheduler;
    }

    /**
     * Set the {@link JobScheduler} instance to use.
     *
     * @param jobScheduler
     *            The {@link JobScheduler} to use.
     */
    public void setJobScheduler(JobScheduler jobScheduler) {
        this.jobScheduler = jobScheduler;
    }

    public void setDaoFactory(DaoFactory daoFactory) {
        this.daoFactory = daoFactory;
    }

    public DaoFactory getDaoFactory() {
        return daoFactory;
    }

    public JobStatusMonitor getJobStatusMonitor() {
        return jobStatusMonitor;
    }

    public void setJobStatusMonitor(JobStatusMonitor jobStatusMonitor) {
        this.jobStatusMonitor = jobStatusMonitor;
    }

    public NotificationSender getNotificationSender() {
        return notificationSender;
    }

    public void setNotificationSender(NotificationSender notificationSender) {
        this.notificationSender = notificationSender;
    }

    public MeandreServerProxyFactory getMeandreServerProxyFactory() {
        return meandreServerProxyFactory;
    }

    public void setMeandreServerProxyFactory(
            MeandreServerProxyFactory meandreServerProxyFactory) {
        this.meandreServerProxyFactory = meandreServerProxyFactory;
    }

    public FlowServiceConfig getFlowServiceConfig() {
        return flowServiceConfig;
    }

    public void setFlowServiceConfig(FlowServiceConfig nemaFlowServiceConfig) {
        this.flowServiceConfig = nemaFlowServiceConfig;
    }

    /**
     * @deprecated
     */
    @Override
    public String createNewFlow(HashMap<String, String> paramMap,
            String flowUri, long userId) {
        throw new IllegalArgumentException("DEPRECATED..");
    }

    /**
     * Set the artifact service to store the flow
     * @param artifactService
     */
    public void setArtifactService(ArtifactService artifactService) {
        this.artifactService = artifactService;
    }

    /**
     * Returns ArtifactService
     *
     * @return artifactservice {@link ArtifactService}}
     */
    public ArtifactService getArtifactService() {
        return artifactService;
    }

    private byte[] readFileAsBytes(String fileLocation) throws IOException {
        File file = new File(fileLocation);
        InputStream is = new FileInputStream(file);
        long length = file.length();
        if (length > Integer.MAX_VALUE) {
            // File is too large
        }
        byte[] bytes = new byte[(int) length];
        int offset = 0;
        int numRead = 0;
        while (offset < bytes.length
                && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
            offset += numRead;
        }
        if (offset < bytes.length) {
            throw new IOException("Could not completely read file " + file.getName());
        }
        try {
            is.close();
        } catch (Exception ex) {
            // error closing stream -okay
        }
        return bytes;
    }

    @Override
    public Map<Component, List<Property>> getAllComponentsAndPropertyDataTypes(
            Credentials credentials, String flowUri) {
        Map<Component, List<Property>> componentPropertyDataTypes = null;
        try {
            componentPropertyDataTypes = headServer.getAllComponentsAndPropertyDataTypes(credentials, flowUri);
        } catch (MeandreServerException e) {
            throw new ServiceException("A problem occurred while retrieving "
                    + "component data types for flow: " + flowUri, e);
        }
        return componentPropertyDataTypes;
    }

    /**
     * @see ConfigChangeListener#configChanged()
     */
    @Override
    public void configChanged() {
        logger.info("Received configuration change notification.");

        if (!headServer.getConfig().equals(flowServiceConfig.getHeadConfig())) {
            MeandreServerProxy newHead;
            try {
                newHead = meandreServerProxyFactory.getServerProxyInstance(
                        flowServiceConfig.getHeadConfig(), true);
            } catch (MeandreServerException e) {
                throw new RuntimeException("Could not instantiate head server.", e);
            }
            meandreServerProxyFactory.release(headServer);
            headServer = newHead;
            logger.info("Head server configuration has changed. New head "
                    + "server is " + headServer.toString());
        } else {
            logger.info("Head server has not changed.");
        }
        jobScheduler.setWorkerConfigs(flowServiceConfig.getWorkerConfigs());
    }
}
