package org.imirsel.nema.flowmetadataservice;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.imirsel.meandre.client.TransmissionException;
import org.imirsel.nema.annotatons.parser.beans.DataTypeBean;
import org.imirsel.nema.flowmetadataservice.impl.ComponentMetadataServiceImpl;
import org.imirsel.nema.flowservice.MeandreServerProxy;
import org.imirsel.nema.flowservice.config.MeandreServerProxyConfig;
import org.imirsel.nema.model.Component;
import org.imirsel.nema.model.Property;


public class ComponentMetadataServiceTest extends BaseManagerTestCase{
	
	private ComponentMetadataServiceImpl componentMetadataManager = new ComponentMetadataServiceImpl();
	private MeandreServerProxy meandreServerProxy;
	
	public ComponentMetadataServiceTest(){
		String host ="128.174.154.145";
		String password = "admin";
		String username ="admin";
		int port = 1714;
		int maxConcurrentJobs =1;
		
		MeandreServerProxyConfig config = new MeandreServerProxyConfig(
				username,password,host,port,maxConcurrentJobs);
		
		meandreServerProxy = new MeandreServerProxy(config);
		meandreServerProxy.init();
		componentMetadataManager.setMeandreServerProxy(meandreServerProxy);
	}
	


	
	public void testGetComponentDataType() throws TransmissionException, SQLException{
		String componentUri="meandre://seasr.org/components/datatypetestcomponent";
		String instanceUri = "http://test.org/datatypetest/instance/datatypetestcomponent/1";
		Component component= new Component();
		component.setInstanceUri(instanceUri);
		component.setUri(componentUri);
		Map<String, Property> map=componentMetadataManager.getComponentPropertyDataType(component, instanceUri);
		System.out.println("--> here "+ map.size());
		Iterator<String> its = map.keySet().iterator();
		while(its.hasNext()){
			String key = its.next();
			Property property = map.get(key);
			List<DataTypeBean> ltb = property.getDataTypeBeanList();
			System.out.println(property.getName() + "  " + property.getDefaultValue());
			 Iterator<DataTypeBean> itb = ltb.iterator();
			while(itb.hasNext()){
				DataTypeBean dt = itb.next();
				System.out.println("class Name:"+ dt.getClass().getName());
				System.out.println("Renderer: "+dt.getRenderer());
			}
			System.out.println("---------------------");
		}
		 

	
	}
	
	

}
