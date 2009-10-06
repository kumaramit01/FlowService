package org.imirsel.nema.flowservice.config;

import java.util.Set;

import org.imirsel.nema.flowservice.MeandreServer;

public interface MeandreJobSchedulerConfig {
	public MeandreServer getHeadNode();
	public Set<MeandreServer> getServers();
}
