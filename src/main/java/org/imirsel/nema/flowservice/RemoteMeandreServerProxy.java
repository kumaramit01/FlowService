package org.imirsel.nema.flowservice;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.jcr.SimpleCredentials;

import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;

import org.imirsel.meandre.client.ExecResponse;
import org.imirsel.meandre.client.MeandreClient;
import org.imirsel.meandre.client.TransmissionException;
import org.imirsel.nema.contentrepository.client.ArtifactService;
import org.imirsel.nema.contentrepository.client.ContentRepositoryServiceException;
import org.imirsel.nema.flowservice.config.MeandreServerProxyConfig;
import org.imirsel.nema.flowservice.config.MeandreServerProxyStatus;
import org.imirsel.nema.flowservice.monitor.JobStatusMonitor;
import org.imirsel.nema.flowservice.monitor.JobStatusUpdateListener;
import org.imirsel.nema.model.Component;
import org.imirsel.nema.model.Job;
import org.imirsel.nema.model.Property;
import org.imirsel.nema.model.RepositoryResourcePath;
import org.imirsel.nema.model.ResourcePath;
import org.imirsel.nema.repository.RepositoryClientConnectionPool;
import org.meandre.core.repository.ExecutableComponentDescription;
import org.meandre.core.repository.FlowDescription;

import com.hp.hpl.jena.rdf.model.Resource;

/**
 * A proxy class for a remote Meandre server.
 * 
 * @author shirk
 * @author kumaramit01
 * @since 0.4.0
 */
@ThreadSafe
public class RemoteMeandreServerProxy implements JobStatusUpdateListener, MeandreServerProxy {

   private static final String MEANDRE_STATUS_CODE_RUNNING = "R";

   private static final Logger logger = Logger
         .getLogger(RemoteMeandreServerProxy.class.getName());

   private MeandreServerProxyConfig config;
   private String host;
   private int port;
   private boolean head;
   
   private RepositoryClientConnectionPool repositoryClientConnectionPool;

   @GuardedBy("runningLock")
   private final Set<Job> runningJobs = new HashSet<Job>(8);
   private final Lock runningLock = new ReentrantLock();

   @GuardedBy("abortingLock")
   private final Set<Job> abortPending = new HashSet<Job>(8);
   private final Lock abortingLock = new ReentrantLock();

   private boolean acceptingJobs = true;
   private JobStatusMonitor jobStatusMonitor;
   private MeandreClient meandreClient;
   private MeandreFlowStore meandreFlowStore;
   private ArtifactService artifactService;
   
   /**
    * Creates a new instance using the provided configuration. Used for testing.
    * 
    * @param config Config to use.
    */
   public RemoteMeandreServerProxy(MeandreServerProxyConfig config) {
      this.config = config;
   }

   /**
    * Creates a new instance.
    */
   public RemoteMeandreServerProxy() {}

   /**
    * @see MeandreServerProxy#init()
    */
   @PostConstruct
   public void init() {
      host = config.getHost();
      port = config.getPort();
      meandreClient = new MeandreClient(config.getHost(), config.getPort());
      meandreClient.setLogger(logger);
      meandreClient.setCredentials(config.getUsername(), config.getPassword());

      if (head) {
         meandreFlowStore = new MeandreFlowStore();
         meandreFlowStore.setMeandreClient(meandreClient);
         meandreFlowStore
               .setRepositoryClientConnectionPool(repositoryClientConnectionPool);
         meandreFlowStore.init();
      }
   }

   /**
    * @see MeandreServerProxy#getNumJobsRunning()
    */
   public int getNumJobsRunning() {
      runningLock.lock();
      try {
         return runningJobs.size();
      } finally {
         runningLock.unlock();
      }
   }

   /**
    * @see MeandreServerProxy#getNumJobsAborting()
    */
   public int getNumJobsAborting() {
      abortingLock.lock();
      try {
         return abortPending.size();
      } finally {
         abortingLock.unlock();
      }
   }
   
   /**
    * @see MeandreServerProxy#isBusy()
    */
   public boolean isBusy() {
      runningLock.lock();
      abortingLock.lock();
      try {
         return config.getMaxConcurrentJobs() <= 
            (runningJobs.size() + abortPending.size());
      } finally {
         abortingLock.unlock();
         runningLock.unlock();
      }
   }
   
   /**
    * @see MeandreServerProxy#isIdle()
    */
   public boolean isIdle() {
      runningLock.lock();
      abortingLock.lock();
      try {
         return runningJobs.size() + abortPending.size() == 0;
      } finally {
         abortingLock.unlock();
         runningLock.unlock();
      }   
   }
   
   /**
    * @see MeandreServerProxy#isAcceptingJobs()
    */
   public boolean isAcceptingJobs() {
      return acceptingJobs;
   }

   /**
    * @see MeandreServerProxy#isAborting(org.imirsel.nema.model.Job)
    */
   public boolean isAborting(Job job) {
      abortingLock.lock();
      try {
         return abortPending.contains(job);
      } finally {
         abortingLock.unlock();
      }
   }

   /**
    * @see MeandreServerProxy#startAcceptingJobs()
    */
   public synchronized void startAcceptingJobs() {
      acceptingJobs = true;
   }
   
   /**
    * @see MeandreServerProxy#stopAcceptingJobs()
    */
   public synchronized void stopAcceptingJobs() {
      acceptingJobs = false;
   }
   
   /**
    * @see MeandreServerProxy#executeJob(org.imirsel.nema.model.Job)
    */
   public synchronized ExecResponse executeJob(Job job) throws MeandreServerException {

      if (!isAcceptingJobs()) {
         throw new IllegalStateException("Could not execute job " + job.getId()
               + " because server " + getServerString() + " is no longer " +
               		"accepting jobs.");
      }
      
      if (isBusy()) {
         throw new IllegalStateException("Could not execute job " + job.getId()
               + " because server " + getServerString() + " is busy.");
      }

      HashMap<String, String> probes = new HashMap<String, String>();
      // Instructs the Meandre server to use the NEMA probe while executing
      probes.put("nema", "true");

      logger.fine("Attempting to execute job " + job.getId() + " on server "
            + getServerString() + ".");
      ExecResponse response = null;
      runningLock.lock();
      try {
         assert meandreClient != null : "Meandre client null";
         assert artifactService != null : "Artifact service is null";

         ResourcePath resPath = getResourcePath(job.getFlow().getUri());
         SimpleCredentials credentials = getCredentials(job.getCredentials());
         byte[] flowData = artifactService.retrieveFlow(credentials, resPath);
         meandreClient.runAsyncModelBytes(flowData, job.getToken(), probes);
         response = meandreClient.getFlowExecutionInstanceId(job.getToken());
         logger.fine("Job " + job.getId()
               + " successfully submitted to server " + getServerString()
               + " for execution.");
         runningJobs.add(job);
         jobStatusMonitor.start(job, this);
      } catch (TransmissionException e) {
         throw new MeandreServerException("A problem occurred while "
               + "communicating with server " + getServerString()
               + " in order to execute job " + job.getId() + ".", e);
      } catch (ContentRepositoryServiceException e) {
         e.printStackTrace();
         throw new MeandreServerException("A problem occurred while "
               + "trying to retrive the flow ", e);
      } finally {
         runningLock.unlock();
      }

      return response;
   }
   
   private SimpleCredentials getCredentials(String credentials) {
      assert credentials != null : "User Credentials are null";
      String[] splits = credentials.split(":");
      String username = splits[0];
      String password = splits[1];
      SimpleCredentials sc = new SimpleCredentials(username, password
            .toCharArray());
      return sc;
   }

   private ResourcePath getResourcePath(String uri) {
      String splits[] = uri.split("://");
      String pcol = splits[0];
      String path = splits[1];
      String workspace = "default";
      RepositoryResourcePath rp = new RepositoryResourcePath(pcol, workspace,
            path);
      return rp;
   }

/**
    * @see MeandreServerProxy#abortJob(org.imirsel.nema.model.Job)
    */
   public void abortJob(Job job) throws MeandreServerException {
      abortingLock.lock();
      try {
         if (isAborting(job)) {
            throw new IllegalStateException("An abort request has already "
                  + "been made for job " + job.getId() + ".");
         }
         try {
            meandreClient.abortFlow(job.getExecPort());
         } catch (TransmissionException e) {
            throw new MeandreServerException("Could not abort job "
                  + job.getId() + ".", e);
         }
         abortPending.add(job);
      } finally {
         abortingLock.unlock();
      }
   }

   /**
    * @see MeandreServerProxy#getServerString()
    */
   public String getServerString() {
      return config.getHost() + ":" + config.getPort();
   }

   /** 
    * @see JobStatusUpdateListener#jobStatusUpdate(Job)
    */
   @Override
   public void jobStatusUpdate(Job job) {
      logger.fine("Status update received for job " + job.getId() + ".");
      if (!job.isRunning()) {
         runningLock.lock();
         abortingLock.lock();
         try {
            runningJobs.remove(job);
            abortPending.remove(job);
         } finally {
            runningLock.unlock();
            abortingLock.unlock();
         }
      }
   }

   /**
    * @see MeandreServerProxy#getConsole(java.lang.String)
    */
   public String getConsole(String uri) throws MeandreServerException {
      String consoleOutput;
      try {
         consoleOutput = meandreClient.retrieveJobConsole(uri);
      } catch (TransmissionException e) {
         throw new MeandreServerException(e.getMessage());
      }
      return consoleOutput;
   }

   /**
    * @see MeandreServerProxy#syncJobsWithServer()
    * 
    * <p>This implementation is a "best try" at synchronizing the jobs with
    * the server. In terms of concurrency, there's still a lot that could go
    * wrong. For example, a job might have just been completed on the server,
    * but the Flow Service does not have the new state yet. In that case,
    * this method will think the job is erroneous and will stop tracking it.
    * However, we can't control these variables, so this is the best we can do.
    */
   public void syncJobsWithServer() {
      // We'll make copies of the running and aborting lists and remove 
      // from them matching jobs that are running/aborting on the server.
      // What will be left are the jobs we need to dispose of.
      List<Job> runningJobsCopy = new ArrayList<Job>(runningJobs);
      List<Job> abortingJobsCopy = new ArrayList<Job>(abortPending);

      try {
         // Returns all jobs, all status, in Meandre server's database
         Vector<Map<String, String>> serverJobList = meandreClient
               .retrieveJobStatuses();

         for (Map<String, String> serverJobDetails : serverJobList) {
            // We're only interested in jobs that are running.
            if (MEANDRE_STATUS_CODE_RUNNING.equals(serverJobDetails
                  .get("status"))) {
               // Find running job matches
               for (Job job : runningJobsCopy) {
                  String execInstanceId = serverJobDetails.get("job_id");
                  if (job.getExecutionInstanceId().equals(execInstanceId)) {
                     runningJobsCopy.remove(job);
                  }
               }
               // Find aborting job matches
               for (Job job : abortingJobsCopy) {
                  String execInstanceId = serverJobDetails.get("job_id");
                  if (job.getExecutionInstanceId().equals(execInstanceId)) {
                     runningJobsCopy.remove(job);
                     // Meandre has no "aborting" state, so we'll go ahead 
                     // and try to abort again since we have no clue if 
                     // it's already aborting or not.
                     meandreClient.abortFlow(job.getExecPort());
                  }
               }
            }
         }

         // Now, in order to complete the synchronization with the server,
         // we have to get rid of jobs that are not actually running on the
         // server.
         for (Job job : runningJobsCopy) {
            logger.info("Job " + job.getExecutionInstanceId() + " is no "
                  + "longer running on server " + this.toString()
                  + ". It will " + "be removed from the list of running jobs.");
            runningJobs.remove(job);
            jobStatusMonitor.stop(job, this);
         }

         for (Job job : abortingJobsCopy) {
            logger.info("Job " + job.getExecutionInstanceId() + " is no "
                  + "longer aborting on server " + this.toString() + ". It "
                  + "will be removed from the list of aborting jobs.");
            runningJobs.remove(job);
            jobStatusMonitor.stop(job, this);
         }

      } catch (TransmissionException e) {
         logger.warning("A communication error occured while attempting to "
               + "sync jobs with the server: " + e.getMessage() + ". Server "
               + this.toString() + "may be in an invalid state. You "
               + "should remove this server from the configuration"
               + "and restart the Meandre server.");
      }
   }
   
   /**
    * @see MeandreServerProxy#getAvailableFlowDescriptionsMap()
    */
   public Map<String, FlowDescription> getAvailableFlowDescriptionsMap() {
      ensureHead();
      return meandreFlowStore.getAvailableFlowDescriptionsMap();
   }

   private void ensureHead() {
      if(!head) {
         throw new IllegalStateException("Server " + this.toString() + 
               " is configured as a worker. This method is not supported for" +
               " worker servers.");
      }
   }

   /**
    * @see MeandreServerProxy#getAvailableFlows()
    */
   public Set<Resource> getAvailableFlows() {
      ensureHead();
      return meandreFlowStore.getAvailableFlows();
   }

   /**
    * @see MeandreServerProxy#getComponentDescription(com.hp.hpl.jena.rdf.model.Resource)
    */
   public ExecutableComponentDescription getComponentDescription(
         Resource flowResource) {
      ensureHead();
      return meandreFlowStore.getComponentDescription(flowResource);
   }

   /**
    * @see MeandreServerProxy#getFlowUris()
    */
   public Set<URI> getFlowUris() throws MeandreServerException {
      ensureHead();
      return meandreFlowStore.getFlowUris();
   }

   /**
    * @see MeandreServerProxy#getComponentDescription(java.lang.String)
    */
   public ExecutableComponentDescription getComponentDescription(
         String componentUri) throws MeandreServerException {
      ensureHead();
      return meandreFlowStore.getComponentDescription(componentUri);
   }

   /**
    * @see MeandreServerProxy#getFlowDescription(java.lang.String)
    */
   public FlowDescription getFlowDescription(String flowUri)
         throws MeandreServerException {
      ensureHead();
      return meandreFlowStore.getFlowDescription(flowUri);
   }

   /**
    * @see MeandreServerProxy#getComponentUrisInRepository()
    */
   public Set<URI> getComponentUrisInRepository() throws MeandreServerException {
      ensureHead();
      return meandreFlowStore.getComponentUrisInRepository();
   }

   /**
    * @see MeandreServerProxy#getComponents(java.lang.String)
    */
   public List<Component> getComponents(String flowUri)
         throws MeandreServerException {
      ensureHead();
      return meandreFlowStore.getComponents(flowUri);
   }

   /**
    * @see MeandreServerProxy#createFlow(java.util.HashMap, java.lang.String, long)
    */
   public synchronized String createFlow(HashMap<String, String> paramMap,
         String flowUri, long userId) throws MeandreServerException {
      ensureHead();
      return meandreFlowStore.createFlow(paramMap, flowUri, userId);
   }

   /**
    * @see MeandreServerProxy#removeFlow(java.lang.String)
    */
   public boolean removeFlow(String uri) throws MeandreServerException {
      ensureHead();
      return meandreFlowStore.removeFlow(uri);
   }

   /**
    * @see MeandreServerProxy#getComponentPropertyDataType(org.imirsel.nema.model.Component, java.lang.String)
    */
   public Map<String, Property> getComponentPropertyDataType(
         Component component, String flowUri) throws MeandreServerException {
      ensureHead();
      return meandreFlowStore.getComponentPropertyDataType(component, flowUri);
   }

   @Override
   public Map<Component, List<Property>> getAllComponentsAndPropertyDataTypes(
         String flowUri) throws MeandreServerException {
      ensureHead();
      return meandreFlowStore.getAllComponentsAndPropertyDataTypes(flowUri);
   }
   
   /**
    * @see MeandreServerProxy#getRepositoryClientConnectionPool()
    */
   public RepositoryClientConnectionPool getRepositoryClientConnectionPool() {
      return repositoryClientConnectionPool;
   }

   /**
    * @see MeandreServerProxy#setRepositoryClientConnectionPool(org.imirsel.nema.repository.RepositoryClientConnectionPool)
    */
   public void setRepositoryClientConnectionPool(
         RepositoryClientConnectionPool repositoryClientConnectionPool) {
      this.repositoryClientConnectionPool = repositoryClientConnectionPool;
   }

   /**
    * @see MeandreServerProxy#setHead(boolean)
    */
   public void setHead(boolean head) {
      this.head = head;
   }
   
   /**
    * @see MeandreServerProxy#isHead()
    */
   public boolean isHead() {
      return head;
   }
   
   /**
    * @see MeandreServerProxy#getConfig()
    */
   public MeandreServerProxyConfig getConfig() {
      return config;
   }

   /**
    * @see MeandreServerProxy#setConfig(org.imirsel.nema.flowservice.config.MeandreServerProxyConfig)
    */
   public void setConfig(MeandreServerProxyConfig config) {
      this.config = config;
      host=config.getHost();
      port=config.getPort();
   }
   
   /**
    * @see MeandreServerProxy#getJobStatusMonitor()
    */
   public JobStatusMonitor getJobStatusMonitor() {
      return jobStatusMonitor;
   }

   /**
    * @see MeandreServerProxy#setJobStatusMonitor(org.imirsel.nema.flowservice.monitor.JobStatusMonitor)
    */
   public void setJobStatusMonitor(JobStatusMonitor jobStatusMonitor) {
      this.jobStatusMonitor = jobStatusMonitor;
   }
   
   /**
    * @see MeandreServerProxy#getMeandreClient()
    */
   public MeandreClient getMeandreClient() {
      return meandreClient;
   }
   
   /**
    * @see MeandreServerProxy#getStatus()
    */
   public MeandreServerProxyStatus getStatus() {
      return new MeandreServerProxyStatus(getNumJobsRunning(),getNumJobsAborting());
   }
   

   @Override
   public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((host == null) ? 0 : host.hashCode());
      result = prime * result + port;
      return result;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj)
         return true;
      if (obj == null)
         return false;
      if (getClass() != obj.getClass())
         return false;
      RemoteMeandreServerProxy other = (RemoteMeandreServerProxy) obj;
      if (host == null) {
         if (other.host != null)
            return false;
      } else if (!host.equals(other.host))
         return false;
      if (port != other.port)
         return false;
      return true;
   }

   @Override
   public String toString() {
      return config.getHost() + ":" + config.getPort();
   }

   @Override
   public void setArtifactService(ArtifactService artifactService) {
      this.artifactService = artifactService;

   }

}
