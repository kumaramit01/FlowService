package org.imirsel.nema.flowservice.config;

import java.util.Set;

import org.imirsel.nema.flowservice.MeandreJobScheduler;
import org.imirsel.nema.flowservice.MeandreServer;

/**
 * Configuration instructions for a {@link MeandreJobScheduler}.
 * 
 * @author shirk
 * @since 1.0
 */
public interface MeandreJobSchedulerConfig {
	/**
	 * Return the {@link MeandreServer} to use as the head node. The head
	 * will be used to field requests for cluster information. It will not be
	 * used to process jobs.
	 * 
	 * @return The head {@link MeandreServer} to use.
	 */
	public MeandreServer getHead();
	
	/**
	 * Return the set of {@link MeandreServer} workers. These servers will be
	 * used to process jobs.
	 * 
	 * @return The set of {@link MeandreServer}s that will be used to process
	 * jobs.
	 */
	public Set<MeandreServer> getServers();
}
