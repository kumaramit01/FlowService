package org.imirsel.nema.flowmetadataservice.impl;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Set;

import org.imirsel.nema.client.beans.repository.WBExecutableComponentDescription;
import org.imirsel.nema.client.beans.repository.WBFlowDescription;
import org.imirsel.nema.flowservice.LocalMeandreFlowRepository;
import org.imirsel.nema.flowservice.MeandreServerException;
import org.imirsel.nema.flowservice.MeandreServerProxy;
import org.imirsel.nema.flowservice.config.SimpleMeandreServerProxyConfig;
import org.imirsel.nema.test.BaseManagerTestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
/**
 * Tests repository functions
 * 
 * @author kumaramit01
 * @since 0.5.0
 */
public class RepositoryTest extends BaseManagerTestCase {
	private MeandreServerProxy meandreServerProxy;
	private LocalMeandreFlowRepository repository;


	

	@Before
	public void setUp() throws Exception {
		String host =getPropertyAsString("host");
		String password=getPropertyAsString("password");
		String username=getPropertyAsString("username");
		int port = getPropertyAsInteger("port");
		int maxConcurrentJobs =1;
		SimpleMeandreServerProxyConfig config = new SimpleMeandreServerProxyConfig(
				username,password,host,port,maxConcurrentJobs);
		meandreServerProxy = new MeandreServerProxy(config);
		meandreServerProxy.init();
		repository = new LocalMeandreFlowRepository();
		repository.setMeandreServerProxy(meandreServerProxy);
	}

	
	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testRetrieveComponentUrls() {
		Set<String> sets=null;
		try {
			sets=repository.retrieveComponentUrls();
		} catch (MeandreServerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		assertTrue(sets!=null);
		for(String val:sets){
			System.out.println(val);
		}
	}

	@Test(expected=MeandreServerException.class)
	public void testRetrieveComponentDescriptorDoesNotExist() throws MeandreServerException {
		String componentURI="meandre://seasr.org/components/input";
			WBExecutableComponentDescription ecd=repository.getComponentDescription(componentURI);
			assertTrue(ecd!=null);
			assertTrue(ecd.getName().length()>0);
	}
	
	@Test
	public void testRetrieveComponentDescriptorExists() throws MeandreServerException {
		String componentURI="meandre://seasr.org/components/input";
			WBExecutableComponentDescription ecd=repository.getComponentDescription(componentURI);
			assertTrue(ecd!=null);
			assertTrue(ecd.getName().length()>0);
	}


	@Test
	public void testRetrieveFlowUrls() {
		Set<String> flowURI=null;
		try {
			flowURI=this.repository.retrieveFlowUrls();
		} catch (MeandreServerException e) {
			fail(e.getMessage());
		}
		assertTrue(flowURI!=null);
		
		for(String uri:flowURI){
			System.out.println(uri);
		}
	}

	@Test
	public void testRetrieveFlowDescriptor() {
		String flowURI="http://test.org/datatypetest/";
		try {
			WBFlowDescription wfd=this.repository.getFlowDescription(flowURI);
			assertTrue(wfd!=null);
		} catch (MeandreServerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	@Test
	public void testRetrieveFlowDescriptors() {
		try {
			Set<WBFlowDescription> list=this.repository.retrieveFlowDescriptors();
			assertTrue(list!=null);
			assertTrue(list.size()>0);
		} catch (MeandreServerException e) {
			fail(e.getMessage());
		}
	}


}
