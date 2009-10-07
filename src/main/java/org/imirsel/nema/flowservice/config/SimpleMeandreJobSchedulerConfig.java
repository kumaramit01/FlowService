package org.imirsel.nema.flowservice.config;

import java.util.Set;

import org.imirsel.nema.flowservice.MeandreServer;

public class SimpleMeandreJobSchedulerConfig implements
		MeandreJobSchedulerConfig {

	MeandreServer head;
	Set<MeandreServer> servers;
	
	@Override
	public MeandreServer getHead() {
		return head;
	}

	@Override
	public Set<MeandreServer> getServers() {
		return servers;
	}

	public void setHead(MeandreServer head) {
		this.head = head;
	}
	
	public void setServers(Set<MeandreServer> servers) {
		this.servers = servers;
	}
}
