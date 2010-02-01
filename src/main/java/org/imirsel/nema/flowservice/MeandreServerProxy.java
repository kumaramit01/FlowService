package org.imirsel.nema.flowservice;

import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;

import net.jcip.annotations.ThreadSafe;

import org.imirsel.meandre.client.ExecResponse;
import org.imirsel.meandre.client.MeandreClient;
import org.imirsel.meandre.client.TransmissionException;
import org.imirsel.nema.flowmetadataservice.ComponentMetadataService;
import org.imirsel.nema.flowmetadataservice.FlowMetadataService;
import org.imirsel.nema.flowservice.config.MeandreServerProxyConfig;
import org.imirsel.nema.flowservice.monitor.JobStatusMonitor;
import org.imirsel.nema.flowservice.monitor.JobStatusUpdateHandler;
import org.imirsel.nema.model.Job;
import org.meandre.core.repository.ExecutableComponentDescription;
import org.meandre.core.repository.FlowDescription;
import org.meandre.core.repository.QueryableRepository;
import org.meandre.core.repository.RepositoryImpl;

import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * A proxy class for a remote Meandre server.
 * 
 * @author shirk
 * @author kumaramit01
 * @since 0.4.0
 * @modified  version 0.5.0 
 *  Abstracted the server configuration parameters
 *  Added flow specific functions used by the FlowMetadataServiceImpl and
 *  ComponentMetadataServiceImpl classes
 *
 */
@ThreadSafe
public class MeandreServerProxy implements JobStatusUpdateHandler { 

	private static final Logger logger = 
		Logger.getLogger(MeandreServerProxy.class.getName());

	private MeandreServerProxyConfig meandreServerProxyConfig = null;
	private ComponentMetadataService componentMetadataService;
	private FlowMetadataService flowMetadataService;


	private final Set<Job> runningJobs = new HashSet<Job>(8);
	private final Lock runningLock = new ReentrantLock();

	private final Set<Job> abortPending = new HashSet<Job>(8);
	private final Lock abortingLock = new ReentrantLock();

	private JobStatusMonitor jobStatusMonitor;
	private MeandreClient meandreClient;

	
	/** Cached repository */
	private QueryableRepository qrCached;


	public MeandreServerProxy(MeandreServerProxyConfig config) {
		this.meandreServerProxyConfig = config;
	}

	@SuppressWarnings("unused")
	private MeandreServerProxy() {

	}

	@PostConstruct
	public void init() {
		this.meandreClient = new MeandreClient(meandreServerProxyConfig.getHost(),meandreServerProxyConfig.getPort());
		this.meandreClient.setLogger(logger);
		this.meandreClient.setCredentials(meandreServerProxyConfig.getUsername(), meandreServerProxyConfig.getPassword());
		qrCached = new RepositoryImpl(ModelFactory.createDefaultModel());
	}

	public MeandreClient getMeandreClient() {
		return meandreClient;
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
	 * Tests if the server is busy such that it cannot process any more jobs.
	 * @return busy true or false
	 */
	public boolean isBusy() {
		runningLock.lock();
		try {
			return meandreServerProxyConfig.getMaxConcurrentJobs() == runningJobs.size();
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

	public JobStatusMonitor getJobStatusMonitor() {
		return jobStatusMonitor;
	}

	public void setJobStatusMonitor(JobStatusMonitor jobStatusMonitor) {
		this.jobStatusMonitor = jobStatusMonitor;
	}

	public ExecResponse executeJob(Job job) throws MeandreServerException {

		if(isBusy()) {
			throw new IllegalStateException("Could not execute job " + 
					job.getId() + " because server " + getServerString() + " is busy.");
		}

		HashMap<String,String> probes = new HashMap<String,String>();
		// Instructs the Meandre server to use the NEMA probe while executing
		probes.put("nema","true");

		logger.fine("Attempting to execute job " + job.getId() +
				" on server " + getServerString() + ".");
		ExecResponse response = null;
		runningLock.lock();
		try {
			assert meandreClient!=null:"Meandre client null";
			meandreClient.runAsyncFlow(job.getFlow().getUrl(), job.getToken(), probes);
			response = meandreClient.getFlowExecutionInstanceId(job.getToken());

			logger.fine("Job " + job.getId() +
					" successfully submitted to server " + getServerString() + " for execution.");

			runningJobs.add(job);
			jobStatusMonitor.start(job, this);
		} catch (TransmissionException e) {
			throw new MeandreServerException("A problem occurred while " +
					"communicating with server " +  getServerString() + 
					" in order to execute job " + job.getId() + ".",e);
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
		return meandreServerProxyConfig.getHost() + ":" + meandreServerProxyConfig.getPort();
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
	 * @return the meandreServerProxyConfig
	 */
	public MeandreServerProxyConfig getMeandreServerProxyConfig() {
		return meandreServerProxyConfig;
	}

	/**
	 * @param meandreServerProxyConfig the meandreServerProxyConfig to set
	 */
	public void setMeandreServerProxyConfig(
			MeandreServerProxyConfig meandreServerProxyConfig) {
		this.meandreServerProxyConfig = meandreServerProxyConfig;
	}



	/**
	 * Returns the console from the job identified by uri
	 * @param uri
	 * @return console The console string
	 * @throws MeandreServerException 
	 */
	public String getConsole(String uri) throws MeandreServerException{
			String jc;
			try {
				jc = this.meandreClient.retrieveJobConsole(uri);
			} catch (TransmissionException e) {
				throw new MeandreServerException(e.getMessage());
			}
			return jc;
	}

	/**
	 * Removes the flow resource pointed by uri
	 * @param uri 
	 * @return success
	 */
	public boolean removeResource(String uri) {
		boolean success=Boolean.FALSE;
		try {
			success=this.meandreClient.removeResource(uri);
		} catch (TransmissionException e) {
		}
		flushRepository();
		return success;
	}

	

	/** Gets the current cached repository.
	 * 
	 * @return The cached queryable repository 
	 */
	public QueryableRepository getRepository() {
		try{
			this.qrCached = this.meandreClient.retrieveRepository();
		}catch(TransmissionException e){
		}
		return this.qrCached;
	}
	
		
	/**
	 * 
	 * @return Map<String,FlowDescription> The Map of String and FlowDescription 
	 */
	public Map<String, FlowDescription> getAvailableFlowDescriptionsMap() {
		QueryableRepository qp= getRepository();
		Map<String, FlowDescription> map=qp.getAvailableFlowDescriptionsMap();
		return map;
	}


	/** Flushes the cached repository.
	 * 
	 */
	public void flushRepository () {
		qrCached = null;
		getRepository();
	}

	/**
	 * Returns the list of Flows available as resource
	 * @return Set<Resource> The set of resource
	 */
	public Set<Resource> getAvailableFlows() {
		QueryableRepository qp = this.getRepository();
		Set<Resource> resources=qp.getAvailableFlows();
		return resources;
	}


	public ExecutableComponentDescription getExecutableComponentDescription(
			Resource flowResource) {
		QueryableRepository qp = this.getRepository();
		ExecutableComponentDescription ecd=qp.getExecutableComponentDescription(flowResource);
		return ecd;
	}
	

	public Set<URI> retrieveFlowUris() throws MeandreServerException {
		Set<URI> set=null;
		try {
			this.meandreClient.retrieveFlowUris();
		} catch (TransmissionException e) {
			throw new MeandreServerException(e);
		}
		
		return set;
	}


	public FlowDescription getFlowDescription(Resource flowResource) {
		QueryableRepository qp = this.getRepository();
		FlowDescription fd=qp.getFlowDescription(flowResource);
		return fd;
	}


	public ExecutableComponentDescription retrieveComponentDescriptor(
			String componentURI) throws MeandreServerException {
		ExecutableComponentDescription ecd=null;
		try {
			ecd=this.meandreClient.retrieveComponentDescriptor(componentURI);
		} catch (TransmissionException e) {
			throw new MeandreServerException(e);
		}
		return ecd;
	}

	
	public FlowDescription retrieveFlowDescriptor(String flowURL) throws MeandreServerException {
		FlowDescription fd=null;
		 try {
			fd= this.meandreClient.retrieveFlowDescriptor(flowURL);
		} catch (TransmissionException e) {
			throw new MeandreServerException(e);
		}
		return fd;
	}

	public boolean uploadFlow(FlowDescription flow, boolean overwrite) {
		return false;
	}

	public Set<URI> retrieveComponentUris() throws MeandreServerException {
		Set<URI> set=null;
		try {
			set=this.meandreClient.retrieveComponentUris();
		} catch (TransmissionException e) {
			throw new MeandreServerException(e);
		}
		return set;
	}


	/**
	 * @return the componentMetadataService
	 */
	public ComponentMetadataService getComponentMetadataService() {
		return componentMetadataService;
	}

	/**
	 * @param componentMetadataService the componentMetadataService to set
	 */
	public void setComponentMetadataService(
			ComponentMetadataService componentMetadataService) {
		this.componentMetadataService = componentMetadataService;
	}

	/**
	 * @return the flowMetadataService
	 */
	public FlowMetadataService getFlowMetadataService() {
		return flowMetadataService;
	}

	/**
	 * @param flowMetadataService the flowMetadataService to set
	 */
	public void setFlowMetadataService(FlowMetadataService flowMetadataService) {
		this.flowMetadataService = flowMetadataService;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
		+ ((abortPending == null) ? 0 : abortPending.hashCode());
		result = prime * result + ((meandreServerProxyConfig.getHost() == null) ? 0 : meandreServerProxyConfig.getHost().hashCode());
		result = prime * result + meandreServerProxyConfig.getMaxConcurrentJobs();
		result = prime * result + meandreServerProxyConfig.getPort();
		result = prime * result
		+ ((runningJobs == null) ? 0 : runningJobs.hashCode());
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
		if (abortPending == null) {
			if (other.abortPending != null)
				return false;
		} else if (!abortPending.equals(other.abortPending))
			return false;
		if (meandreServerProxyConfig.getHost() == null) {
			if (other.meandreServerProxyConfig.getHost() != null)
				return false;
		} else if (!meandreServerProxyConfig.getHost().equals(other.meandreServerProxyConfig.getHost()))
			return false;
		if (meandreServerProxyConfig.getMaxConcurrentJobs() != other.meandreServerProxyConfig.getMaxConcurrentJobs())
			return false;
		if (meandreServerProxyConfig.getPort() != other.meandreServerProxyConfig.getPort())
			return false;
		if (runningJobs == null) {
			if (other.runningJobs != null)
				return false;
		} else if (!runningJobs.equals(other.runningJobs))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return meandreServerProxyConfig.getHost()+":"+meandreServerProxyConfig.getPort();
	}


}
