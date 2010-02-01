package org.imirsel.nema.flowmetadataservice.impl;

import static org.junit.Assert.fail;

import java.util.Set;

import org.imirsel.nema.flowservice.MeandreServerException;
import org.imirsel.nema.flowservice.MeandreServerProxy;
import org.imirsel.nema.flowservice.config.MeandreServerProxyConfig;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
/**
 * 
 * @author kumaramit01
 * @since 0.5.0
 */
public class RepositoryTest {
	private MeandreServerProxy meandreServerProxy;
	private Repository repository;

	@Before
	public void setUp() throws Exception {
		String host ="nema.lis.uiuc.edu";
		String password = "admin";
		String username ="admin";
		int port = 11709;
		int maxConcurrentJobs =1;
		MeandreServerProxyConfig config = new MeandreServerProxyConfig(
				username,password,host,port,maxConcurrentJobs);
		meandreServerProxy = new MeandreServerProxy(config);
		meandreServerProxy.init();
		repository = new Repository();
		repository.setMeandreServerProxy(meandreServerProxy);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testRetrieveComponentUrls() {
		try {
			Set<String> sets=repository.retrieveComponentUrls();
		} catch (MeandreServerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	public void testRetrieveComponentDescriptor() {
		fail("Not yet implemented");
	}

	@Test
	public void testRetrieveComponentDescriptors() {
		fail("Not yet implemented");
	}

	@Test
	public void testRetrieveFlowUrls() {
		fail("Not yet implemented");
	}

	@Test
	public void testRetrieveFlowDescriptor() {
		fail("Not yet implemented");
	}

	@Test
	public void testRetrieveFlowDescriptors() {
		fail("Not yet implemented");
	}

	@Test
	public void testUploadFlow() {
		fail("Not yet implemented");
	}

	@Test
	public void testRemoveResource() {
		fail("Not yet implemented");
	}

}
