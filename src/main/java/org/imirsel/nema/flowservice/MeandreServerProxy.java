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

import org.imirsel.nema.flowservice.config.MeandreServerProxyConfig;
import org.imirsel.nema.flowservice.monitor.JobStatusMonitor;
import org.imirsel.nema.flowservice.monitor.JobStatusUpdateHandler;
import org.imirsel.nema.model.Job;
import org.imirsel.meandre.client.ExecResponse;
import org.imirsel.meandre.client.MeandreClient;
import org.imirsel.meandre.client.TransmissionException;
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
 * @since 1.0
 * @modified -amitku version 0.5.0 
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

	private final Set<Job> runningJobs = new HashSet<Job>(8);
	private Lock runningLock = new ReentrantLock();

	private final Set<Job> abortPending = new HashSet<Job>(8);
	private Lock abortingLock = new ReentrantLock();

	private JobStatusMonitor jobStatusMonitor;
	private MeandreClient meandreClient;

	/** Cached roles */
	private Set<String> mapRoles;

	/** Cached repository */
	private QueryableRepository qrCached;


	public MeandreServerProxy(MeandreServerProxyConfig config) {
		this.meandreServerProxyConfig = config;
		this.meandreClient = new MeandreClient(config.getHost(),config.getPort());
		this.meandreClient.setLogger(logger);
		this.meandreClient.setCredentials(config.getUsername(), config.getPassword());
		qrCached = new RepositoryImpl(ModelFactory.createDefaultModel());
	}

	@SuppressWarnings("unused")
	private MeandreServerProxy() {

	}

	@PostConstruct
	public void init() {
		this.meandreClient = new MeandreClient(meandreServerProxyConfig.getHost(),meandreServerProxyConfig.getPort());
		this.meandreClient.setLogger(logger);
		this.meandreClient.setCredentials(meandreServerProxyConfig.getUsername(), meandreServerProxyConfig.getPassword());

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
	 * @return
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
	 * Returns the list of Flows available as resource
	 */
	public Set<Resource> getAvailableFlows() {
		// TODO Auto-generated method stub
		return null;
	}


	

	public ExecutableComponentDescription getExecutableComponentDescription(
			Resource createResource) {
		// TODO Auto-generated method stub
		return null;
	}
	

	public FlowDescription getFlowDescription(Resource createResource) {
		// TODO Auto-generated method stub
		return null;
	}

	
	
	/**
	 * 
	 * @returns the flow description
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

	/** Return the roles for the user of this proxy.
	 * 
	 * @return The set of granted role for the proxy user
	 */
	private Set<String> getRoles() {
		if ( mapRoles==null ) {
			try{
				//Set<String> roles = this.client.retrieveUserRoles();
				this.mapRoles=this.meandreClient.retrieveUserRoles();
			}catch(TransmissionException e){
			}           
		}
		return mapRoles;
	}
	

	public ExecutableComponentDescription retrieveComponentDescriptor(
			String componentURL) {
		// TODO Auto-generated method stub
		return null;
	}

	public Set<URI> retrieveFlowUris() {
		// TODO Auto-generated method stub
		return null;
	}

	public FlowDescription retrieveFlowDescriptor(String flowURL) {
		// TODO Auto-generated method stub
		return null;
	}

	public void uploadFlow(FlowDescription flow, boolean overwrite) {
		// TODO Auto-generated method stub
		
	}

	public Set<URI> retrieveComponentUris() {
		// TODO Auto-generated method stub
		return null;
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
