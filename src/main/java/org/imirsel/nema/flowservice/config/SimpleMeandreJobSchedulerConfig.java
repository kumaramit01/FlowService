package org.imirsel.nema.flowservice.config;

import java.util.Set;

import org.imirsel.nema.flowservice.MeandreServer;

/**
 * A {@link MeandreJobSchedulerConfig} intended to work in concert with a
 * Spring configuration file to inject the requisite properties.
 * 
 * @author shirk
 * @since 1.0
 *
 */
public class SimpleMeandreJobSchedulerConfig implements
		MeandreJobSchedulerConfig {

	MeandreServer head;
	Set<MeandreServer> servers;
	
	/**
	 * @see org.imirsel.nema.flowservice.config.MeandreJobSchedulerConfig#getHead()
	 */
	@Override
	public MeandreServer getHead() {
		return head;
	}

	/**
	 * @see org.imirsel.nema.flowservice.config.MeandreJobSchedulerConfig#getServers()
	 */
	@Override
	public Set<MeandreServer> getServers() {
		return servers;
	}

	/**
	 * Set the head server to use.
	 * 
	 * @param head The head server to use.
	 */
	public void setHead(MeandreServer head) {
		this.head = head;
	}
	
	/**
	 * Assign the workers to be used to process jobs.
	 * 
	 * @param servers The workers to be used to process jobs.
	 */
	public void setServers(Set<MeandreServer> servers) {
		this.servers = servers;
	}
}
