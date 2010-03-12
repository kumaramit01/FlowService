package org.imirsel.nema.flowmetadataservice;

import static org.junit.Assert.*;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;

import org.imirsel.meandre.client.TransmissionException;
import org.imirsel.nema.annotations.parser.beans.DataTypeBean;
import org.imirsel.nema.flowmetadataservice.impl.ComponentMetadataServiceImpl;
import org.imirsel.nema.flowservice.MeandreServerException;
import org.imirsel.nema.flowservice.MeandreServerProxy;
import org.imirsel.nema.flowservice.config.SimpleMeandreServerProxyConfig;
import org.imirsel.nema.model.Component;
import org.imirsel.nema.model.Property;
import org.imirsel.nema.repository.RepositoryClientConnectionPool;
import org.imirsel.nema.test.BaseManagerTestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * 
 * @author kumaramit01
 * @since 0.5.0
 *
 */
public class ComponentMetadataServiceTest extends BaseManagerTestCase{
	
	private final ComponentMetadataServiceImpl componentMetadataManager = new ComponentMetadataServiceImpl();
	private MeandreServerProxy meandreServerProxy;
	private RepositoryClientConnectionPool repositoryClientConnectionPool;
	
	@Before
	public void beforeClass() throws MeandreServerException{
		this.getLogger().setLevel(Level.SEVERE);
		String host = getPropertyAsString("host");//"nema.lis.uiuc.edu";
		String password = getPropertyAsString("password");
		String username = getPropertyAsString("username");
		int port = getPropertyAsInteger("port");
		int maxConcurrentJobs =1;
		SimpleMeandreServerProxyConfig config = new SimpleMeandreServerProxyConfig(
				username,password,host,port,maxConcurrentJobs);
		meandreServerProxy = new MeandreServerProxy(config);
		meandreServerProxy.init();
		repositoryClientConnectionPool = RepositoryClientConnectionPool.getInstance();
		componentMetadataManager.setMeandreServerProxy(meandreServerProxy);
		componentMetadataManager.setRepositoryClientConnectionPool(repositoryClientConnectionPool);
		
	}
	
	@After
	public void afterClass(){
		repositoryClientConnectionPool.destroy();
	}
	

	@Test(expected=MeandreServerException.class)
	public void testGetComponentDataTypeNotExist() throws TransmissionException, SQLException, MeandreServerException{
		String componentURI="meandre://seasr.org/components/datatypetestcomponent-invalid";
		String instanceURI = "http://test.org/datatypetest/instance/datatypetestcomponent/0";
		String flowURI = "http://test.org/datatypetest/";
		Component component= new Component();
		component.setInstanceUri(instanceURI);
		component.setUri(componentURI);
		Map<String, Property> map=componentMetadataManager.getComponentPropertyDataType(component, flowURI);
		assertTrue(map.size()==0);
	}
	
	


	@Test
	public void testGetComponentDataType() throws TransmissionException, SQLException, MeandreServerException{
		String componentURI="meandre://seasr.org/components/datatypetestcomponent";
		String instanceURI = "http://test.org/datatypetest/instance/datatypetestcomponent/0";
		String flowURI = "http://test.org/datatypetest/";
		Component component= new Component();
		component.setInstanceUri(instanceURI);
		component.setUri(componentURI);
		Map<String, Property> map=componentMetadataManager.getComponentPropertyDataType(component, flowURI);
		assertTrue(map.size()>0);
	}
	
	@Test
	public void testComponentDataTypeValidValue() throws TransmissionException, SQLException, MeandreServerException{
		String componentURI="meandre://seasr.org/components/datatypetestcomponent";
		String instanceURI = "http://test.org/datatypetest/instance/datatypetestcomponent/0";
		String flowURI = "http://test.org/datatypetest/";
		Component component= new Component();
		component.setInstanceUri(instanceURI);
		component.setUri(componentURI);
		Map<String, Property> map=componentMetadataManager.getComponentPropertyDataType(component, flowURI);
	
		Iterator<Entry<String,Property>> its = map.entrySet().iterator();
		while(its.hasNext()){
			Entry<String,Property> tmp = its.next();
			Property property =tmp.getValue();
			List<DataTypeBean> ltb = property.getDataTypeBeanList();
			System.out.println(property.getName() + "  " + property.getDefaultValue());
			Iterator<DataTypeBean> itb = ltb.iterator();
			while(itb.hasNext()){
				DataTypeBean dt = itb.next();
				assertTrue(dt.getClass().getName()!=null &&dt.getRenderer()!=null );
			}
			this.getLogger().info("---------------------");
		}
		 

	
	}
	
	

}
