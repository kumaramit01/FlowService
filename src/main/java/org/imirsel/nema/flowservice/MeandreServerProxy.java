package org.imirsel.nema.flowservice;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


import org.imirsel.meandre.client.ExecResponse;
import org.imirsel.meandre.client.MeandreClient;
import org.imirsel.nema.contentrepository.client.ArtifactService;
import org.imirsel.nema.flowservice.config.MeandreServerProxyConfig;
import org.imirsel.nema.flowservice.config.MeandreServerProxyStatus;
import org.imirsel.nema.flowservice.monitor.JobStatusMonitor;
import org.imirsel.nema.model.Component;
import org.imirsel.nema.model.Job;
import org.imirsel.nema.model.Property;
import org.imirsel.nema.repository.RepositoryClientConnectionPool;
import org.meandre.core.repository.ExecutableComponentDescription;
import org.meandre.core.repository.FlowDescription;

import com.hp.hpl.jena.rdf.model.Resource;

/**
 * Provides indirect access to the services provided by a Meandre server.
 * 
 * @author shirk
 * @since 0.4.0
 */
public interface MeandreServerProxy {

   /**
    * Initialize this server proxy instance. This method must be called before
    * an instance is used.
    */
   public void init();

   /**
    * Return the number of jobs currently running on this server.
    * 
    * @return Number of jobs currently running on this server.
    */
   public int getNumJobsRunning();

   /**
    * Return the number of jobs that are pending abort.
    * 
    * @return Number of jobs that are pending abort.
    */
   public int getNumJobsAborting();

   /**
    * Tests if the server is busy such that it cannot process any more jobs.
    * 
    * @return True if the server cannot accept any more jobs.
    */
   public boolean isBusy();

   /**
    * Tests if specified job is currently pending abort on this server.
    * 
    * @param job Job to check to see if it is pending abort.
    * @return True if the specified job is pending abort.
    */
   public boolean isAborting(Job job);

   /**
    * Execute the specified job.
    * 
    * @param job Job to execute on this server.
    * @return The execution response from the server, which contains details
    * only known after the job has been accepted, like the port that the job
    * will be running on.
    * @throws MeandreServerException if a problem occurs while attempting
    * to execute the job.
    */
   public ExecResponse executeJob(Job job)
         throws MeandreServerException;

   /**
    * Request that the specified job be aborted.
    * 
    * @param job Job for which the abort request will be made.
    * @throws MeandreServerException if a problem occurs while requesting the
    * job be aborted.
    */
   public void abortJob(Job job) throws MeandreServerException;

   /**
    * Return a <code>String</code> uniquely identifying this server. Returned
    * <code>String</code> will be in the form of server:port.
    * @return <code>String</code> uniquely identifying this server.
    */
   public String getServerString();

   /**
    * Return the job console for the job identified with the specified URI.
    * 
    * @param uri URI of the job for which the console should be returned.
    * @return console Console output in the form of a <code>String</code>.
    * @throws MeandreServerException
    */
   public String getConsole(String uri) throws MeandreServerException;


   /**
    * Return a map of string and flowdescription
    * 
    * @return a Map of String and {@link FlowDescription}
    */
   public Map<String, FlowDescription> getAvailableFlowDescriptionsMap();

   /**
    * Returns the list of Flows available as resource
    * 
    * @return Set<Resource> The set of resource
    */
   public Set<Resource> getAvailableFlows();

   /** Return the ExecutableComponentDescription
    * 
    * @param flowResource
    * @return {@link ExecutableComponentDescription}
    */
   public ExecutableComponentDescription getComponentDescription(
         Resource flowResource);

   /**Return the flow uri
    * 
    * @return Set of {@link URI}
    * @throws MeandreServerException
    */
   public Set<URI> getFlowUris() throws MeandreServerException;

   /**
    * Return the component description
    * 
    * @param componentUri
    * @return ExecutableComponentDescription 
    * @throws MeandreServerException
    */
   public ExecutableComponentDescription getComponentDescription(
         String componentUri) throws MeandreServerException;

   /**
    * Return the flow description
    * @param flowUri
    * @return flow description
    * @throws MeandreServerException
    */
   public FlowDescription getFlowDescription(String flowUri)
         throws MeandreServerException;

   /**
    * Return the URIs of all the components in the Meandre server repository.
    * 
    * @return URIs of all the components in the Meandre server repository.
    * 
    * @throws MeandreServerException if a problem occurs while communicating
    *            with the remote Meandre server.
    */
   public Set<URI> getComponentUrisInRepository()
         throws MeandreServerException;

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
         throws MeandreServerException;

   /**
    * Create a new flow and save it in the local repository. The the supplied
    * parameter map containing custom properties that a user has set based on a
    * template flow and creates a new flow.
    * @param paramMap
    * @param flowUri
    * @param userId
    * 
    * @returns URI of the new flow.
    */
   public String createFlow( HashMap<String, String> paramMap,
         String flowUri, long userId) throws MeandreServerException;

   /**
    * Remove flow
    * 
    * @param uri
    * @return true/false
    * @throws MeandreServerException
    */
   public boolean removeFlow(String uri) throws MeandreServerException;

   /**
    * Return the component property data type
    * 
    * @param component
    * @param flowUri
    * @return Map of String and {@link Property}
    * @throws MeandreServerException
    */
   public Map<String, Property> getComponentPropertyDataType(
         Component component, String flowUri) throws MeandreServerException;

   /**Return the client connection pool
    * 
    * @return the repository client connection pool
    */
   public RepositoryClientConnectionPool getRepositoryClientConnectionPool();

   /**
    *  Set the repository client connection pool
    * @param repositoryClientConnectionPool
    */
   public void setRepositoryClientConnectionPool(
         RepositoryClientConnectionPool repositoryClientConnectionPool);

   /**
    * Return the configuration used to instantiate this proxy.
    * 
    * @return Current proxy configuration.
    */
   public MeandreServerProxyConfig getConfig();

   /**
    * Set the configuration for this proxy.
    * 
    * @param meandreServerProxyConfig The configuration parameters for this
    *           proxy.
    */
   public void setConfig(MeandreServerProxyConfig config);

   /**
    * Return the {@link JobStatusMonitor} currently in use.
    * 
    * @return The {@link JobStatusMonitor} currently in use.
    */
   public JobStatusMonitor getJobStatusMonitor();

   /**
    * Set the {@link JobStatusMonitor} to use.
    * 
    * @param jobStatusMonitor The {@link JobStatusMonitor} currently in use.
    */
   public void setJobStatusMonitor(JobStatusMonitor jobStatusMonitor);

   /**
    * Return the underlying {@link MeandreClient} currently in use by this
    * proxy to communicate with the actual Meandre server.
    * 
    * @return The {@link MeandreClient} currently being used to communicate with
    * the actual Meandre server.
    */
   public MeandreClient getMeandreClient();

   /**
    * Return the current server status, such as the number of jobs running and
    * the number aborting.
    * 
    * @return Current server status.
    */
   public MeandreServerProxyStatus getStatus();

   /**
    * Set the artifact service, which is used to retrieve flow bytes
    * @param artifactService
    */
   public void setArtifactService(ArtifactService artifactService);

   /**
    * Returns the component property data types.
    * @param flowUri
    * @return The Map of {@link Component} and List of {@link Property}
 * @throws MeandreServerException 
    */
   public Map<Component, List<Property>> getAllComponentsAndPropertyDataTypes(String flowUri) throws MeandreServerException;
		

}