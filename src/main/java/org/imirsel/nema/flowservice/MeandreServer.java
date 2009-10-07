package org.imirsel.nema.flowservice;

import java.util.HashSet;
import java.util.Set;

import org.imirsel.nema.model.Job;
import org.meandre.client.MeandreClient;

// Make thread safe
public class MeandreServer {

	private String host;
	private int port;
	private int maxConcurrentJobs = 1;
	private final Set<Job> runningJobs = new HashSet<Job>(8);
	private final Set<Job> abortPending = new HashSet<Job>(8);
	
	private MeandreClient meandreClient;
	
	public MeandreServer(String host, int port, int maxConcurrentJobs) {
		this.host = host;
		this.port = port;
		this.maxConcurrentJobs = maxConcurrentJobs;
		// create meandre client
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
	
	public void executeJob(Job job) {
		System.out.println("Server " + host + ":" + port + " executing job.");
		// check that num running is < max
		// submit job to server
		// increment counter
		runningJobs.add(job);
	}
	public void abortJob(Job job) {
		// make sure an abort isn't already pending
		// submit abort request to server via client
		// then executor thread needs to run to check database for status change (which is made by the  probe) and decrement the running counter.
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
