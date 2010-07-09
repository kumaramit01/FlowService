package org.imirsel.nema.flowservice.config;

import java.util.Set;

/**
 * A {@link FlowServiceConfig} intended to work in concert with a
 * Spring configuration file to inject the requisite properties.
 * 
 * @author shirk
 * @since 0.4.0
 */
public class SimpleFlowServiceConfig extends FlowServiceConfigBase {

	private MeandreServerProxyConfig head;
	private Set<MeandreServerProxyConfig> servers;
	
	/**
	 * @see org.imirsel.nema.flowservice.config.FlowServiceConfig#getHeadConfig()
	 */
	@Override
	public MeandreServerProxyConfig getHeadConfig() {
		return head;
	}

	/**
	 * @see org.imirsel.nema.flowservice.config.FlowServiceConfig#getWorkerConfigs()
	 */
	@Override
	public Set<MeandreServerProxyConfig> getWorkerConfigs() {
		return servers;
	}

	/**
	 * Set the head server to use.
	 * 
	 * @param head The head server to use.
	 */
	public void setHead(MeandreServerProxyConfig head) {
		this.head = head;
	}
	
	/**
	 * Assign the workers to be used to process jobs.
	 * 
	 * @param servers The workers to be used to process jobs.
	 */
	public void setServers(Set<MeandreServerProxyConfig> servers) {
		this.servers = servers;
	}
}
