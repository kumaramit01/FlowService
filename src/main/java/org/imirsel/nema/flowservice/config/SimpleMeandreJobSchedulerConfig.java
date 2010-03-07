package org.imirsel.nema.flowservice.config;

import java.io.Serializable;
import java.util.Set;

import org.imirsel.nema.flowservice.MeandreJobSchedulerConfig;
import org.imirsel.nema.flowservice.MeandreServerProxy;

/**
 * A {@link MeandreJobSchedulerConfig} intended to work in concert with a
 * Spring configuration file to inject the requisite properties.
 * 
 * @author shirk
 * @since 0.4.0
 */
public class SimpleMeandreJobSchedulerConfig implements
		MeandreJobSchedulerConfig{

	MeandreServerProxy head;
	Set<MeandreServerProxy> servers;
	
	/**
	 * @see org.imirsel.nema.flowservice.MeandreJobSchedulerConfig#getHead()
	 */
	@Override
	public MeandreServerProxy getHead() {
		return head;
	}

	/**
	 * @see org.imirsel.nema.flowservice.MeandreJobSchedulerConfig#getServers()
	 */
	@Override
	public Set<MeandreServerProxy> getServers() {
		return servers;
	}

	/**
	 * Set the head server to use.
	 * 
	 * @param head The head server to use.
	 */
	public void setHead(MeandreServerProxy head) {
		this.head = head;
	}
	
	/**
	 * Assign the workers to be used to process jobs.
	 * 
	 * @param servers The workers to be used to process jobs.
	 */
	public void setServers(Set<MeandreServerProxy> servers) {
		this.servers = servers;
	}
}
