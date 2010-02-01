package org.imirsel.nema.flowmetadataservice;

import java.util.List;

import org.imirsel.meandre.client.TransmissionException;
import org.imirsel.nema.flowmetadataservice.impl.FlowMetadataServiceImpl;
import org.imirsel.nema.flowmetadataservice.impl.Repository;
import org.imirsel.nema.flowservice.MeandreServerProxy;
import org.imirsel.nema.flowservice.config.MeandreServerProxyConfig;
import org.imirsel.nema.model.Component;
import org.junit.Before;
import org.junit.Test;

/**
 * 
 * @author kumaramit01
 * @since 0.5.0
 *
 */
public class FlowMetadataServiceTest{
	
	private final FlowMetadataServiceImpl flowMetadataService = new FlowMetadataServiceImpl();
	private MeandreServerProxy meandreServerProxy;
	private Repository repository;

	@Before
	public void setup(){
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
		flowMetadataService.setRepository(repository);
		flowMetadataService.setMeandreServerProxy(meandreServerProxy);
		
	}
	

	
	@Test
	public void testGetComponents() throws TransmissionException{
		String flowUri="http://test.org/datatypetest/";
		List<Component>componentList=flowMetadataService.getComponents(flowUri);
		for(int i=0;i< componentList.size();i++){
			System.out.println("--> "+componentList.get(i).getInstanceUri());
		}
		//componentMetadataManager.getComponentPropertyDataType(componentId);
	}
	

}
