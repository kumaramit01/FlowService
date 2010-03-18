package org.imirsel.nema.flowservice;

import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;

import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;

import org.imirsel.meandre.client.ExecResponse;
import org.imirsel.meandre.client.MeandreClient;
import org.imirsel.meandre.client.TransmissionException;
import org.imirsel.nema.flowservice.config.MeandreServerProxyConfig;
import org.imirsel.nema.flowservice.config.MeandreServerProxyStatus;
import org.imirsel.nema.flowservice.monitor.JobStatusMonitor;
import org.imirsel.nema.flowservice.monitor.JobStatusUpdateHandler;
import org.imirsel.nema.model.Component;
import org.imirsel.nema.model.Job;
import org.imirsel.nema.model.Property;
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
public class RemoteMeandreServerProxy implements JobStatusUpdateHandler, MeandreServerProxy {

   private static final Logger logger = Logger
         .getLogger(RemoteMeandreServerProxy.class.getName());

   private MeandreServerProxyConfig config;
   private String host;
   private int port;
   
   private RepositoryClientConnectionPool repositoryClientConnectionPool;

   @GuardedBy("runningLock")
   private final Set<Job> runningJobs = new HashSet<Job>(8);
   private final Lock runningLock = new ReentrantLock();

   @GuardedBy("abortingLock")
   private final Set<Job> abortPending = new HashSet<Job>(8);
   private final Lock abortingLock = new ReentrantLock();

   private JobStatusMonitor jobStatusMonitor;
   private MeandreClient meandreClient;
   private static MeandreFlowStore meandreFlowStore;
   
   public RemoteMeandreServerProxy(MeandreServerProxyConfig config) {
      this.config = config;
   }

   @SuppressWarnings("unused")
   private RemoteMeandreServerProxy() {

   }

   /* (non-Javadoc)
    * @see org.imirsel.nema.flowservice.MeandreServerProxy#init()
    */
   @PostConstruct
   public void init() {
      host=config.getHost();
      port=config.getPort();
      meandreClient = new MeandreClient(config.getHost(), config.getPort());
      meandreClient.setLogger(logger);
      meandreClient.setCredentials(config.getUsername(), config.getPassword());
      
      meandreFlowStore = new MeandreFlowStore();
      meandreFlowStore.setMeandreClient(meandreClient);
      meandreFlowStore.setRepositoryClientConnectionPool(repositoryClientConnectionPool);
      meandreFlowStore.init();
   }

   /* (non-Javadoc)
    * @see org.imirsel.nema.flowservice.MeandreServerProxy#getNumJobsRunning()
    */
   public int getNumJobsRunning() {
      runningLock.lock();
      try {
         return runningJobs.size();
      } finally {
         runningLock.unlock();
      }
   }

   /* (non-Javadoc)
    * @see org.imirsel.nema.flowservice.MeandreServerProxy#getNumJobsAborting()
    */
   public int getNumJobsAborting() {
      abortingLock.lock();
      try {
         return abortPending.size();
      } finally {
         abortingLock.unlock();
      }
   }
   
   /* (non-Javadoc)
    * @see org.imirsel.nema.flowservice.MeandreServerProxy#isBusy()
    */
   public boolean isBusy() {
      runningLock.lock();
      try {
         return config.getMaxConcurrentJobs() == runningJobs.size();
      } finally {
         runningLock.unlock();
      }
   }

   /* (non-Javadoc)
    * @see org.imirsel.nema.flowservice.MeandreServerProxy#isAborting(org.imirsel.nema.model.Job)
    */
   public boolean isAborting(Job job) {
      abortingLock.lock();
      try {
         return abortPending.contains(job);
      } finally {
         abortingLock.unlock();
      }
   }

   /* (non-Javadoc)
    * @see org.imirsel.nema.flowservice.MeandreServerProxy#executeJob(org.imirsel.nema.model.Job)
    */
   public ExecResponse executeJob(Job job) throws MeandreServerException {

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
         meandreClient.runAsyncModel(
               job.getFlow().getUri(), job.getToken(), probes);
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
      } finally {
         runningLock.unlock();
      }

      return response;
   }

   /* (non-Javadoc)
    * @see org.imirsel.nema.flowservice.MeandreServerProxy#abortJob(org.imirsel.nema.model.Job)
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

   /* (non-Javadoc)
    * @see org.imirsel.nema.flowservice.MeandreServerProxy#getServerString()
    */
   public String getServerString() {
      return config.getHost() + ":" + config.getPort();
   }

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

   /* (non-Javadoc)
    * @see org.imirsel.nema.flowservice.MeandreServerProxy#getConsole(java.lang.String)
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

   /* (non-Javadoc)
    * @see org.imirsel.nema.flowservice.MeandreServerProxy#getAvailableFlowDescriptionsMap()
    */
   public Map<String, FlowDescription> getAvailableFlowDescriptionsMap() {
      return meandreFlowStore.getAvailableFlowDescriptionsMap();
   }

   /* (non-Javadoc)
    * @see org.imirsel.nema.flowservice.MeandreServerProxy#getAvailableFlows()
    */
   public Set<Resource> getAvailableFlows() {
      return meandreFlowStore.getAvailableFlows();
   }

   /* (non-Javadoc)
    * @see org.imirsel.nema.flowservice.MeandreServerProxy#getComponentDescription(com.hp.hpl.jena.rdf.model.Resource)
    */
   public ExecutableComponentDescription getComponentDescription(
         Resource flowResource) {
      return meandreFlowStore.getComponentDescription(flowResource);
   }

   /* (non-Javadoc)
    * @see org.imirsel.nema.flowservice.MeandreServerProxy#getFlowUris()
    */
   public Set<URI> getFlowUris() throws MeandreServerException {
      return meandreFlowStore.getFlowUris();
   }

   /* (non-Javadoc)
    * @see org.imirsel.nema.flowservice.MeandreServerProxy#getComponentDescription(java.lang.String)
    */
   public ExecutableComponentDescription getComponentDescription(
         String componentUri) throws MeandreServerException {
      return meandreFlowStore.getComponentDescription(componentUri);
   }

   /* (non-Javadoc)
    * @see org.imirsel.nema.flowservice.MeandreServerProxy#getFlowDescription(java.lang.String)
    */
   public FlowDescription getFlowDescription(String flowUri)
         throws MeandreServerException {
      return meandreFlowStore.getFlowDescription(flowUri);
   }

   /* (non-Javadoc)
    * @see org.imirsel.nema.flowservice.MeandreServerProxy#getComponentUrisInRepository()
    */
   public Set<URI> getComponentUrisInRepository() throws MeandreServerException {
      return meandreFlowStore.getComponentUrisInRepository();
   }

   /* (non-Javadoc)
    * @see org.imirsel.nema.flowservice.MeandreServerProxy#getComponents(java.lang.String)
    */
   public List<Component> getComponents(String flowUri)
         throws MeandreServerException {
      return meandreFlowStore.getComponents(flowUri);
   }

   /* (non-Javadoc)
    * @see org.imirsel.nema.flowservice.MeandreServerProxy#createFlow(java.util.HashMap, java.lang.String, long)
    */
   public synchronized String createFlow(HashMap<String, String> paramMap,
         String flowUri, long userId) throws MeandreServerException {
      return meandreFlowStore.createFlow(paramMap, flowUri, userId);
   }

   /* (non-Javadoc)
    * @see org.imirsel.nema.flowservice.MeandreServerProxy#removeFlow(java.lang.String)
    */
   public boolean removeFlow(String uri) throws MeandreServerException {
      return meandreFlowStore.removeFlow(uri);
   }

   /* (non-Javadoc)
    * @see org.imirsel.nema.flowservice.MeandreServerProxy#getComponentPropertyDataType(org.imirsel.nema.model.Component, java.lang.String)
    */
   public Map<String, Property> getComponentPropertyDataType(
         Component component, String flowUri) throws MeandreServerException {
      return meandreFlowStore.getComponentPropertyDataType(component, flowUri);
   }

   /* (non-Javadoc)
    * @see org.imirsel.nema.flowservice.MeandreServerProxy#getRepositoryClientConnectionPool()
    */
   public RepositoryClientConnectionPool getRepositoryClientConnectionPool() {
      return repositoryClientConnectionPool;
   }

   /* (non-Javadoc)
    * @see org.imirsel.nema.flowservice.MeandreServerProxy#setRepositoryClientConnectionPool(org.imirsel.nema.repository.RepositoryClientConnectionPool)
    */
   public void setRepositoryClientConnectionPool(
         RepositoryClientConnectionPool repositoryClientConnectionPool) {
      this.repositoryClientConnectionPool = repositoryClientConnectionPool;
   }

   /* (non-Javadoc)
    * @see org.imirsel.nema.flowservice.MeandreServerProxy#getConfig()
    */
   public MeandreServerProxyConfig getConfig() {
      return config;
   }

   /* (non-Javadoc)
    * @see org.imirsel.nema.flowservice.MeandreServerProxy#setConfig(org.imirsel.nema.flowservice.config.MeandreServerProxyConfig)
    */
   public void setConfig(MeandreServerProxyConfig config) {
      this.config = config;
      host=config.getHost();
      port=config.getPort();
   }
   
   /* (non-Javadoc)
    * @see org.imirsel.nema.flowservice.MeandreServerProxy#getJobStatusMonitor()
    */
   public JobStatusMonitor getJobStatusMonitor() {
      return jobStatusMonitor;
   }

   /* (non-Javadoc)
    * @see org.imirsel.nema.flowservice.MeandreServerProxy#setJobStatusMonitor(org.imirsel.nema.flowservice.monitor.JobStatusMonitor)
    */
   public void setJobStatusMonitor(JobStatusMonitor jobStatusMonitor) {
      this.jobStatusMonitor = jobStatusMonitor;
   }
   
   /* (non-Javadoc)
    * @see org.imirsel.nema.flowservice.MeandreServerProxy#getMeandreClient()
    */
   public MeandreClient getMeandreClient() {
      return meandreClient;
   }
   
   /* (non-Javadoc)
    * @see org.imirsel.nema.flowservice.MeandreServerProxy#getStatus()
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

}
