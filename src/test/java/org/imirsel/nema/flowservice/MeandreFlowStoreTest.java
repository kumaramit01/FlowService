package org.imirsel.nema.flowservice;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.net.URI;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.jcr.Credentials;
import javax.jcr.SimpleCredentials;

import org.imirsel.meandre.client.TransmissionException;
import org.imirsel.nema.annotations.parser.beans.DataTypeBean;
import org.imirsel.nema.flowservice.MeandreServerException;
import org.imirsel.nema.flowservice.RemoteMeandreServerProxy;
import org.imirsel.nema.flowservice.config.SimpleMeandreServerProxyConfig;
import org.imirsel.nema.model.Component;
import org.imirsel.nema.model.Property;
import org.imirsel.nema.test.BaseManagerTestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.meandre.core.repository.ExecutableComponentDescription;
import org.meandre.core.repository.FlowDescription;

import com.hp.hpl.jena.rdf.model.Resource;

/**
 * Tests repository functions
 * 
 * @author kumaramit01
 * @since 0.5.0
 */
public class MeandreFlowStoreTest extends BaseManagerTestCase {
   private MeandreServerProxy meandreServerProxy;
   private String passwordHash=null;

   @Before
   public void setUp() throws Exception {
      String host = getPropertyAsString("host");
      String password = getPropertyAsString("password");
      String username = getPropertyAsString("username");
      passwordHash = getPropertyAsString("passwordHash");
      int port = getPropertyAsInteger("port");
      int maxConcurrentJobs = 1;
      SimpleMeandreServerProxyConfig config = new SimpleMeandreServerProxyConfig(
            username, password, host, port, maxConcurrentJobs);
      meandreServerProxy = new RemoteMeandreServerProxy(config);
      meandreServerProxy.init();
     
   }

   @After
   public void tearDown() throws Exception {
   }
   @Ignore
   @Test
   public void testGetComponentUrisInRepository() {
      Set<URI> sets = null;
      try {
         sets = meandreServerProxy.getComponentUrisInRepository();
      } catch (MeandreServerException e) {
         e.printStackTrace();
      }
      assertTrue(sets != null);
      for (URI uri : sets) {
         System.out.println(uri.toString());
      }
   }
   @Ignore
   @Test(expected = MeandreServerException.class)
   public void testGetComponentDescriptionDoesNotExist()
         throws MeandreServerException {
      String componentURI = "meandre://seasr.org/components/input#invaliduri";
      ExecutableComponentDescription ecd = 
         meandreServerProxy.getComponentDescription(componentURI);
   }
   @Ignore
   @Test
   public void testGetComponentDescriptionExists()
         throws MeandreServerException {
      String componentURI = "meandre://seasr.org/components/printobject";
      ExecutableComponentDescription ecd = meandreServerProxy
            .getComponentDescription(componentURI);
      assertTrue(ecd != null);
      assertTrue(ecd.getName().length() > 0);
   }
   @Ignore
   @Test
   public void testGetFlowUrls() {
      Set<URI> flowURI = null;
      try {
         flowURI = meandreServerProxy.getFlowUris();
      } catch (MeandreServerException e) {
         fail(e.getMessage());
      }
      assertTrue(flowURI != null);

      for (URI uri : flowURI) {
         System.out.println(uri.toString());
      }
   }
   @Ignore
   @Test
   public void testGetFlowDescription() {
      String flowURI = "http://www.imirsel.org/test/testdynamiccomponentflow/";
      try {
         FlowDescription wfd = meandreServerProxy.getFlowDescription(flowURI);
         assertTrue(wfd != null);
      } catch (MeandreServerException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
   }
   @Ignore
   @Test
   public void testGetAvailableFlows() {
         Set<Resource> list = meandreServerProxy
               .getAvailableFlows();
         assertTrue(list != null);
         assertTrue(list.size() > 0);
   }
   @Ignore
   @Test(expected=MeandreServerException.class)
   public void testGetComponentDataTypeNotExist() throws TransmissionException, SQLException, MeandreServerException{
      String componentURI="meandre://seasr.org/components/datatypetestcomponent-invalid";
      String instanceURI = "http://www.imirsel.org/test/testdynamiccomponentflow/instance/datatypetestcomponent/0";
      String flowURI = "http://www.imirsel.org/test/testdynamiccomponentflow/";
      Component component= new Component();
      component.setInstanceUri(instanceURI);
      component.setUri(componentURI);
      Map<String, Property> map=meandreServerProxy.getComponentPropertyDataType(component, flowURI);
      assertTrue(map.size()==0);
   }
   @Ignore
   @Test
   public void testGetComponentDataType() throws TransmissionException, SQLException, MeandreServerException{
      String componentURI="meandre://seasr.org/components/datatypetestcomponent";
      String instanceURI = "http://www.imirsel.org/test/testdynamiccomponentflow/instance/datatypetestcomponent/0";
      String flowURI = "http://www.imirsel.org/test/testdynamiccomponentflow/";
      Component component= new Component();
      component.setInstanceUri(instanceURI);
      component.setUri(componentURI);
      Map<String, Property> map=meandreServerProxy.getComponentPropertyDataType(component, flowURI);
      assertTrue(map.size()>0);
   }
   @Ignore
   @Test
   public void testComponentDataTypeValidValue() throws TransmissionException, SQLException, MeandreServerException{
      String componentURI="meandre://seasr.org/components/datatypetestcomponent";
      String instanceURI = "http://www.imirsel.org/test/testdynamiccomponentflow/instance/datatypetestcomponent/0";
      String flowURI = "http://www.imirsel.org/test/testdynamiccomponentflow/";
      Component component= new Component();
      component.setInstanceUri(instanceURI);
      component.setUri(componentURI);
      Map<String, Property> map=meandreServerProxy.getComponentPropertyDataType(component, flowURI);
   
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
   @Ignore
   @Test
   public void testGetComponents() throws MeandreServerException{
      String flowUri="http://www.imirsel.org/test/testdynamiccomponentflow/";
      List<Component>componentList=meandreServerProxy.getComponents(flowUri);
      for(int i=0;i< componentList.size();i++){
         System.out.println("--> "+componentList.get(i).getInstanceUri() + " : " + componentList.get(i).getUri());
      }
      
   }
   
   @Ignore
   @Test 
   public void testcreateNewFlow(){
      String flowURI="http://www.imirsel.org/test/testdynamiccomponentflow/";
      HashMap<String,String> paramMap = new HashMap<String,String>();
      paramMap.put("datatypetestcomponent_0_mfcc","false");
      String fileName=null;
      try {
    	 fileName=meandreServerProxy.createFlow( paramMap, flowURI, 0l);
         assertTrue(fileName!=null);
         assertTrue(fileName.length()>0);
      } catch (MeandreServerException e) {
         fail(e.getMessage());
      }
      System.out.println("filename is: " + fileName);
   }
   
   
   @Test
   public void testGetListComponentsDataTypes() throws TransmissionException, SQLException, MeandreServerException{
      String flowURI = "http://www.imirsel.org/test/testdynamiccomponentflow/";
      Map<Component,List<Property>> componentPropertyMap=meandreServerProxy.getAllComponentsAndPropertyDataTypes(flowURI);
      assertTrue(componentPropertyMap.size()>0);
   }
   
}
