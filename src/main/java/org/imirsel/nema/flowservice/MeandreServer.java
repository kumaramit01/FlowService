package org.imirsel.nema.flowservice;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import org.imirsel.nema.flowservice.monitor.JobStatusMonitor;
import org.imirsel.nema.flowservice.monitor.JobStatusUpdateHandler;
import org.imirsel.nema.model.Job;
import org.meandre.client.MeandreClient;
import org.meandre.client.TransmissionException;

// Make thread safe
public class MeandreServer implements JobStatusUpdateHandler { 

	private static final Logger logger = 
		Logger.getLogger(MeandreServer.class.getName());
	
	private String host;
	private int port;
	private JobStatusMonitor jobStatusMonitor;
	private int maxConcurrentJobs = 1;
	private final Set<Job> runningJobs = new HashSet<Job>(8);
	private final Set<Job> abortPending = new HashSet<Job>(8);
	
	private MeandreClient meandreClient;
	
	public MeandreServer(String host, int port, int maxConcurrentJobs) {
		this.host = host;
		this.port = port;
		this.maxConcurrentJobs = maxConcurrentJobs;
		this.meandreClient = new MeandreClient(host,port);
		this.meandreClient.setLogger(logger);
		this.meandreClient.setCredentials("admin", "admin");
	}

	public MeandreServer() {
		
	}
	
	public MeandreClient getMeandreClient() {
		return null;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}
	
	public int getPort() {
		return port;
	}
	
	public void setPort(int port) {
		this.port = port;
	}
	public int getMaxConcurrentJobs() {
		return maxConcurrentJobs;
	}
	
	public int getNumJobsRunning() {
		return runningJobs.size();
	}

	public boolean isBusy() {
	    return maxConcurrentJobs == runningJobs.size();
	}
	
	
	public JobStatusMonitor getJobStatusMonitor() {
		return jobStatusMonitor;
	}

	public void setJobStatusMonitor(JobStatusMonitor jobStatusMonitor) {
		this.jobStatusMonitor = jobStatusMonitor;
	}

	public void executeJob(Job job) throws ServerException {
		if(isBusy()) {
			throw new IllegalStateException("Could not execute job " + 
					job.getId() + " because server " + getServerString() + " is busy.");
		}
		logger.fine("Server " + getServerString() + " executing job " + job.getId() + ".");
		try {
			meandreClient.runAsyncFlow(job.getFlow().getUrl(), false);
		} catch (TransmissionException e) {
			throw new ServerException("A problem occurred while " +
					"communicating with server " +  getServerString() + 
					" in order to execute job " + job.getId() + ".",e);
		}
		runningJobs.add(job);
		jobStatusMonitor.start(job, this);
	}
	
	public void abortJob(Job job) throws ServerException {
		if(abortPending.contains(job)) {
			throw new IllegalStateException("An abort request has already " +
					"been made for job " + job.getId() + ".");
		}
		// THE INT ARGUMENT NEEDS TO BE A PORT NUMBER
		try {
			meandreClient.abortFlow(0);
		} catch (TransmissionException e) {
			throw new ServerException("Could not abort job " + 
					job.getId() + ".",e);
		}
		abortPending.add(job);
	}

	public String getServerString() {
		return host + ":" + port;
	}
	
	@Override
	public void jobStatusUpdate(Job job) {
		if(!job.isRunning()) {
	       runningJobs.remove(job);
	       abortPending.remove(job);
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((abortPending == null) ? 0 : abortPending.hashCode());
		result = prime * result + ((host == null) ? 0 : host.hashCode());
		result = prime * result + maxConcurrentJobs;
		result = prime * result + port;
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
		MeandreServer other = (MeandreServer) obj;
		if (abortPending == null) {
			if (other.abortPending != null)
				return false;
		} else if (!abortPending.equals(other.abortPending))
			return false;
		if (host == null) {
			if (other.host != null)
				return false;
		} else if (!host.equals(other.host))
			return false;
		if (maxConcurrentJobs != other.maxConcurrentJobs)
			return false;
		if (port != other.port)
			return false;
		if (runningJobs == null) {
			if (other.runningJobs != null)
				return false;
		} else if (!runningJobs.equals(other.runningJobs))
			return false;
		return true;
	}
	


}
