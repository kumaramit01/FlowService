package org.imirsel.nema.flowservice;

import java.util.Set;


/**
 * Configuration instructions for a {@link MeandreJobScheduler}.
 * 
 * @author shirk
 * @since 1.0
 */
public interface MeandreJobSchedulerConfig {
	/**
	 * Return the {@link MeandreServerProxy} to use as the head node. The head
	 * will be used to field requests for cluster information. It will not be
	 * used to process jobs.
	 * 
	 * @return The head {@link MeandreServerProxy} to use.
	 */
	public MeandreServerProxy getHead();
	
	/**
	 * Return the set of {@link MeandreServerProxy} workers. These servers will be
	 * used to process jobs.
	 * 
	 * @return The set of {@link MeandreServerProxy}s that will be used to process
	 * jobs.
	 */
	public Set<MeandreServerProxy> getServers();
}
