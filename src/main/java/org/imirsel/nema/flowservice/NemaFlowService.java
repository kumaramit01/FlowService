package org.imirsel.nema.flowservice;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;

import org.imirsel.nema.NoSuchEntityException;
import org.imirsel.nema.dao.DaoFactory;
import org.imirsel.nema.dao.FlowDao;
import org.imirsel.nema.dao.JobDao;
import org.imirsel.nema.dao.JobResultDao;
import org.imirsel.nema.flowservice.config.FlowServiceConfig;
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
import org.imirsel.nema.model.Job.JobStatus;
import org.springframework.orm.ObjectRetrievalFailureException;

/**
 * A {@link FlowService} implementation for the NEMA project.
 * 
 * @author shirk
 * @since 0.4.0
 */
public class NemaFlowService implements FlowService {

	private static final Logger logger = 
		Logger.getLogger(NemaFlowService.class.getName());
	
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
       
       notificationCreator = 
    	   new JobStatusNotificationCreator(daoFactory);
       headServer = meandreServerProxyFactory.
       getServerProxyInstance(flowServiceConfig.getHeadConfig());
	}
	
	/**
	 * @see org.imirsel.nema.flowservice.FlowService#abortJob(long)
	 */
	@Override
	public void abortJob(long jobId) throws IllegalStateException {
		Job job = daoFactory.getJobDao().findById(jobId, false);
		if(!job.isRunning()) {
			throw new IllegalStateException("Cannot abort job " + jobId + 
					" because it has already completed.");
		}
		jobScheduler.abortJob(job);
	}

	/**
	 * @see org.imirsel.nema.flowservice.FlowService#deleteJob(long)
	 */
	@Override
	public void deleteJob(long jobId) throws IllegalStateException {
		JobDao jobDao = daoFactory.getJobDao();
		JobResultDao resultDao = daoFactory.getJobResultDao();
		FlowDao flowDao = daoFactory.getFlowDao();
		
		Job job = jobDao.findById(jobId,false);
		// Job must be finished to be deleted.
		if(!job.isDone()) {
			throw new IllegalArgumentException("Cannot delete job " + jobId + 
					" because it is still running, scheduled or submitted.");
		}
		for(JobResult result : job.getResults()) {
			resultDao.makeTransient(result);
		}
		jobDao.makeTransient(job);
		flowDao.makeTransient(job.getFlow());
		// delete the results from disk?
	}

	/**
	 * @see org.imirsel.nema.flowservice.FlowService#executeJob(String, String, String, long, long, String)
	 */
	@Override
	public Job executeJob(String token, String name, String description, long flowInstanceId, long userId,
			String userEmail) {
		FlowDao flowDao = daoFactory.getFlowDao();
		JobDao jobDao = daoFactory.getJobDao();
		
		Flow flowInstance = flowDao.findById(flowInstanceId,false);
		
		Job job = new Job();
		job.setToken(token);
		job.setName(name);
		job.setDescription(description);
		job.setFlow(flowInstance);
		job.setOwnerId(userId);
		job.setOwnerEmail(userEmail);
		jobDao.makePersistent(job);
		
		jobScheduler.scheduleJob(job);
		job.setJobStatus(JobStatus.SCHEDULED);
		jobDao.makePersistent(job);
		
		jobStatusMonitor.start(job,notificationCreator);
		
		return job;
	}

	/**
	 * @see org.imirsel.nema.flowservice.FlowService#getFlowTemplates()
	 */
	@Override
	public Set<Flow> getFlowTemplates() {
		FlowDao flowDao = daoFactory.getFlowDao();
		
		Set<Flow> flowSet = new HashSet<Flow>();
		flowSet.addAll(flowDao.getFlowTemplates());
		return flowSet;
	}

	/**
	 * @see org.imirsel.nema.flowservice.FlowService#getJob(long)
	 */
	@Override
	public Job getJob(long jobId) {
		JobDao jobDao = daoFactory.getJobDao();
		logger.info("getting job with id " + jobId);
		Job job;
		try {
			logger.fine("Retrieving job "+ jobId);
			job = jobDao.findById(jobId,false);
			logger.fine("Job "+ jobId + " has " + job.getResults().size() + " results.");
		} catch (ObjectRetrievalFailureException e) {
			throw new NoSuchEntityException("Job " + jobId + " does not exist.");
		}
		return job;
	}
	
	/**
	 * @see org.imirsel.nema.flowservice.FlowService#getUserJobs(long)
	 */
	@Override
	public List<Job> getUserJobs(long userId) {
		return daoFactory.getJobDao().getJobsByOwnerId(userId);
	}

	/**
	 * @see org.imirsel.nema.flowservice.FlowService#getUserNotifications(long)
	 */
	@Override
	public List<Notification> getUserNotifications(long userId) {
		return daoFactory.getNotificationDao().getNotificationsByRecipientId(userId);
	}

	/**
	 * @see org.imirsel.nema.flowservice.FlowService#storeFlowInstance(Flow)
	 */
	@Override
	public Long storeFlowInstance(Flow instance) {
		FlowDao flowDao = daoFactory.getFlowDao();
		flowDao.makePersistent(instance);
		// store the flow on disk
		return instance.getId();
	}
	
	
	/**
	 * @see org.imirsel.nema.flowservice.FlowService#getFlow(long)
	 */
	@Override
	public Flow getFlow(long flowId) {
		FlowDao flowDao = daoFactory.getFlowDao();
		Flow flow = flowDao.findById(flowId, false);
		// set refs to meandre servers
		return flow;
	}


	@Override
	public String createNewFlow(HashMap<String, String> paramMap, String flowURI, long userId) throws MeandreServerException {
		return headServer.createFlow(paramMap, flowURI, userId);
	}

	@Override
	public Map<String, Property> getComponentPropertyDataType(
			Component component, String url) throws MeandreServerException {
		return headServer.getComponentPropertyDataType(component, url);
	}

	@Override
	public List<Component> getComponents(String flowUri) throws MeandreServerException {
		return headServer.getComponents(flowUri);
	}

	@Override
	public String getConsole(String uri) throws MeandreServerException {
		return headServer.getConsole(uri);
	}

	@Override
	public boolean removeFlow(String flowUri) throws MeandreServerException {
		return headServer.removeFlow(flowUri);
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
	 * @param jobScheduler The {@link JobScheduler} to use.
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

   
   public FlowServiceConfig getNemaFlowServiceConfig() {
      return flowServiceConfig;
   }

   public void setNemaFlowServiceConfig(
         FlowServiceConfig nemaFlowServiceConfig) {
      this.flowServiceConfig = nemaFlowServiceConfig;
   }

   @Override
	public HashMap<MeandreServerProxyConfig, MeandreServerProxyStatus> getMeandreServerProxyStatus() {
		// TODO Auto-generated method stub
		return null;
	@Override
	public Map<String, MeandreServerProxyStatus> getMeandreServerProxyStatus() {
		MeandreServerProxy head=this.getJobScheduler().getJobSchedulerConfig().getHead();
		HashMap<String, MeandreServerProxyStatus> map = new HashMap<String, MeandreServerProxyStatus>();
		MeandreServerProxyStatus proxyStatus = new MeandreServerProxyStatus();
		proxyStatus.setNumRunning(head.getNumJobsRunning());
		proxyStatus.setNumAborting(head.getNumJobsAborting());
		proxyStatus.setHead(true);
		proxyStatus.setMaxConcurrentJobs(head.getMeandreServerProxyConfig().getMaxConcurrentJobs());
		map.put(head.getServerString(), proxyStatus);
		for(MeandreServerProxy serverProxy:this.getJobScheduler().getJobSchedulerConfig().getServers()){
			proxyStatus = new MeandreServerProxyStatus();
			proxyStatus.setNumRunning(serverProxy.getNumJobsRunning());
			proxyStatus.setNumAborting(serverProxy.getNumJobsAborting());
			proxyStatus.setMaxConcurrentJobs(head.getMeandreServerProxyConfig().getMaxConcurrentJobs());
			proxyStatus.setHead(false);
			map.put(serverProxy.getServerString(),proxyStatus);
		}
		return map;
	}

	@Override
	public MeandreServerProxyStatus getMeandreServerProxyStatus(String host,
			int port) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<MeandreServerProxyConfig> getSchedulerConfig() {
		// TODO Auto-generated method stub
		return null;
	}

}
