package org.imirsel.nema.flowservice.config;

import java.util.Set;

import org.imirsel.nema.flowservice.MeandreServerProxy;


/**
 * Models a configuration for the {@link FlowService}.
 * 
 * @author shirk
 * @since 0.4.0
 */
public interface FlowServiceConfig {
	/**
	 * Return the {@link MeandreServerProxyConfig} to use as the head node. The head
	 * will be used to field requests for cluster information. It will not be
	 * used to process jobs.
	 * 
	 * @return The head {@link MeandreServerProxy} to use.
	 */
	public MeandreServerProxyConfig getHeadConfig();
	
	/**
	 * Return the set of {@link MeandreServerProxyConfig} workers. These servers will be
	 * used to process jobs.
	 * 
	 * @return The set of {@link MeandreServerProxyConfig}s that will be used to process
	 * jobs.
	 */
	public Set<MeandreServerProxyConfig> getWorkerConfigs();
	
	/**
	 * Register an object to listen for changes to the configuration.
	 * 
	 * @param listener The listener to register.
	 */
	public void addChangeListener(ConfigChangeListener listener);
	
   /**
    * Unregister an object from listening for changes to the configuration.
    * 
    * @param listener The listener to unregister.
    */
   public void removeChangeListener(ConfigChangeListener listener);
   
   
}
