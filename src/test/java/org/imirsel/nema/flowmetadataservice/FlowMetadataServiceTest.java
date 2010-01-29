package org.imirsel.nema.flowmetadataservice;

import java.util.List;

import org.imirsel.meandre.client.TransmissionException;
import org.imirsel.nema.flowmetadataservice.impl.FlowMetadataServiceImpl;
import org.imirsel.nema.flowservice.MeandreServerProxy;
import org.imirsel.nema.flowservice.config.MeandreServerProxyConfig;
import org.imirsel.nema.model.Component;


public class FlowMetadataServiceTest extends BaseManagerTestCase{
	
	private FlowMetadataServiceImpl flowMetadataService = new FlowMetadataServiceImpl();
	private MeandreServerProxy meandreProxyWrapper;
	
	public FlowMetadataServiceTest(){
		String host ="128.174.154.145";
		String password = "admin";
		String username ="admin";
		int port = 1714;
		int maxConcurrentJobs =1;
		
		MeandreServerProxyConfig config = new MeandreServerProxyConfig(
				username,password,host,port,maxConcurrentJobs);
		
		meandreProxyWrapper = new MeandreServerProxy(config);
		meandreProxyWrapper.init();
		flowMetadataService.setMeandreServerProxy(meandreProxyWrapper);
	}
	

	
	public void testGetComponents() throws TransmissionException{
		String flowUri="http://test.org/datatypetest/";
		List<Component>componentList=flowMetadataService.getComponents(flowUri);
		for(int i=0;i< componentList.size();i++){
			System.out.println("--> "+componentList.get(i).getInstanceUri());
		}
		//componentMetadataManager.getComponentPropertyDataType(componentId);
	}
	

}
