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
 * @modified version 0.5.0 Abstracted the server configuration parameters Added
 *           flow specific functions used by the FlowMetadataServiceImpl and
 *           ComponentMetadataServiceImpl classes
 * 
 */
@ThreadSafe
public class MeandreServerProxy implements JobStatusUpdateHandler {

   private static final Logger logger = Logger
         .getLogger(MeandreServerProxy.class.getName());

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
   
   public MeandreServerProxy(MeandreServerProxyConfig config) {
      this.config = config;
   }

   @SuppressWarnings("unused")
   private MeandreServerProxy() {

   }

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

   /**
    * Return the number of jobs the server is currently processing.
    * 
    * @return Number of jobs the server is currently processing.
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
    * Return the number of jobs that are pending abort.
    * 
    * @return Number of jobs that are pending abort.
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
    * Tests if the server is busy such that it cannot process any more jobs.
    * 
    * @return busy true or false
    */
   public boolean isBusy() {
      runningLock.lock();
      try {
         return config.getMaxConcurrentJobs() == runningJobs.size();
      } finally {
         runningLock.unlock();
      }
   }

   public boolean isAborting(Job job) {
      abortingLock.lock();
      try {
         return abortPending.contains(job);
      } finally {
         abortingLock.unlock();
      }
   }

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
         boolean returnVal = meandreClient.runAsyncModel(
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



   /**
    * Returns the console from the job identified by uri
    * 
    * @param uri
    * @return console The console string
    * @throws MeandreServerException
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
    * 
    * @return Map<String,FlowDescription> The Map of String and FlowDescription
    */
   public Map<String, FlowDescription> getAvailableFlowDescriptionsMap() {
      return meandreFlowStore.getAvailableFlowDescriptionsMap();
   }

   /**
    * Flushes the cached repository.
    * 
    * @return success true/false
    * @throws MeandreServerException
    * 
    */
   public boolean flushRepository() throws MeandreServerException {
      return meandreFlowStore.flushRepository();
   }

   /**
    * Returns the list of Flows available as resource
    * 
    * @return Set<Resource> The set of resource
    */
   public Set<Resource> getAvailableFlows() {
      return meandreFlowStore.getAvailableFlows();
   }

   public ExecutableComponentDescription getComponentDescription(
         Resource flowResource) {
      return meandreFlowStore.getComponentDescription(flowResource);
   }

   public Set<URI> getFlowUris() throws MeandreServerException {
      return meandreFlowStore.getFlowUris();
   }

   public ExecutableComponentDescription getComponentDescription(
         String componentUri) throws MeandreServerException {
      return meandreFlowStore.getComponentDescription(componentUri);
   }

   public FlowDescription getFlowDescription(String flowUri)
         throws MeandreServerException {
      return meandreFlowStore.getFlowDescription(flowUri);
   }

   /**
    * Return the URIs of all the components in the Meandre server repository.
    * 
    * @return URIs of all the components in the Meandre server repository.
    * 
    * @throws MeandreServerException if a problem occurs while communicating
    *            with the remote Meandre server.
    */
   public Set<URI> getComponentUris() throws MeandreServerException {
      return meandreFlowStore.getComponentUrisInRepository();
   }

   /**
    * For the given flow URI, return the list of {@link Component}s that make up
    * the flow.
    * 
    * @param flowUri URI of the flow to get the components for.
    * @return List of {@link Component}s that make up the flow.
    * @throws MeandreServerException if a problem occurs while attempting to get
    *            the flows from the remote Meandre server.
    */
   public List<Component> getComponents(String flowUri)
         throws MeandreServerException {
      return meandreFlowStore.getComponents(flowUri);
   }

   /**
    * Create a new flow and save it in the local repository. The the supplied
    * parameter map containing custom properties that a user has set based on a
    * template flow and creates a new flow.
    * 
    * @returns URI of the new flow.
    */
   public synchronized String createFlow(HashMap<String, String> paramMap,
         String flowUri, long userId) throws MeandreServerException {
      return meandreFlowStore.createFlow(paramMap, flowUri, userId);
   }

   /**
    * This method removes a flow from meandre
    * 
    * @throws MeandreServerException
    * 
    * @returns success
    */
   public boolean removeFlow(String uri) throws MeandreServerException {
      return meandreFlowStore.removeFlow(uri);
   }

   public Map<String, Property> getComponentPropertyDataType(
         Component component, String flowUri) throws MeandreServerException {
      return meandreFlowStore.getComponentPropertyDataType(component, flowUri);
   }

   public RepositoryClientConnectionPool getRepositoryClientConnectionPool() {
      return repositoryClientConnectionPool;
   }

   public void setRepositoryClientConnectionPool(
         RepositoryClientConnectionPool repositoryClientConnectionPool) {
      this.repositoryClientConnectionPool = repositoryClientConnectionPool;
   }

   /**
    * Return the configuration used to instantiate this proxy.
    * 
    * @return Current proxy configuration.
    */
   public MeandreServerProxyConfig getConfig() {
      return config;
   }

   /**
    * Set the configuration for this proxy.
    * 
    * @param meandreServerProxyConfig The configuration parameters for this
    *           proxy.
    */
   public void setConfig(MeandreServerProxyConfig config) {
      this.config = config;
      host=config.getHost();
      port=config.getPort();
   }
   
   public JobStatusMonitor getJobStatusMonitor() {
      return jobStatusMonitor;
   }

   public void setJobStatusMonitor(JobStatusMonitor jobStatusMonitor) {
      this.jobStatusMonitor = jobStatusMonitor;
   }
   
   public MeandreClient getMeandreClient() {
      return meandreClient;
   }
   
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
      MeandreServerProxy other = (MeandreServerProxy) obj;
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
