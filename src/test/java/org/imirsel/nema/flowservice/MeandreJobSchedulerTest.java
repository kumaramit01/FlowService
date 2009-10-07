package org.imirsel.nema.flowservice;


import java.util.HashSet;
import java.util.Set;

import org.imirsel.nema.flowservice.config.MeandreJobSchedulerConfig;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class MeandreJobSchedulerTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	      MeandreJobSchedulerConfig config =
	          new MeandreJobSchedulerConfig() {
	             public MeandreServer getHead() {
	                return new MeandreServer("localhost", 10000, 1);
	             }

	             public Set<MeandreServer> getServers() {
	                Set<MeandreServer> servers = new HashSet<MeandreServer>();
	                servers.add(new MeandreServer("192.168.0.1", 10000, 1));
	                servers.add(new MeandreServer("192.168.0.2", 10000, 1));
	                servers.add(new MeandreServer("192.168.0.3", 10000, 1));
	                servers.add(new MeandreServer("192.168.0.4", 10000, 1));
	                return servers;
	             }
	          };
	}

	@After
	public void tearDown() throws Exception {
	}
	
	@Test
	public void emptyText(){}

}
