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

import javax.jcr.Repository;
import javax.jcr.SimpleCredentials;

import org.apache.jackrabbit.rmi.client.ClientRepositoryFactory;
import org.imirsel.meandre.client.TransmissionException;
import org.imirsel.nema.annotations.parser.beans.DataTypeBean;
import org.imirsel.nema.contentrepository.client.ContentRepositoryService;
import org.imirsel.nema.flowservice.MeandreServerException;
import org.imirsel.nema.flowservice.RemoteMeandreServerProxy;
import org.imirsel.nema.flowservice.config.SimpleMeandreServerProxyConfig;
import org.imirsel.nema.model.Component;
import org.imirsel.nema.model.Property;
import org.imirsel.nema.repository.RepositoryClientConnectionPool;
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
   private SimpleCredentials credentials = null;
   private String flowURI = null;
   private String componentURI = null;
   private String componentInstance = null;
   
   private String componentProperty=null;
   private String componentPropertyValue = null;
   private String jcrFlowUri=null;
   private ClientRepositoryFactory factory =null;
   private RepositoryClientConnectionPool repositoryClientConnectionPool=null;
   private String rmiContentServiceUrl;
   
   
   @Before
   public void setUp() throws Exception {
      String host = getPropertyAsString("host");
      String password = getPropertyAsString("password");
      String username = getPropertyAsString("username");
      passwordHash = getPropertyAsString("passwordHash");
      flowURI = getPropertyAsString("testFlow1");
      componentURI = getPropertyAsString("componentURI1");
      componentInstance = getPropertyAsString("componentInstance1");
      componentProperty = getPropertyAsString("componentProperty");
      componentPropertyValue = getPropertyAsString("componentPropertyValue");
      rmiContentServiceUrl = getPropertyAsString("rmiContentServiceUrl");
      jcrFlowUri =  getPropertyAsString("jcrFlowUri");
      
      factory = new ClientRepositoryFactory();
      Repository repository=factory.getRepository(rmiContentServiceUrl);
      repositoryClientConnectionPool = RepositoryClientConnectionPool.getInstance();
      
      
      int port = getPropertyAsInteger("port");
      credentials = new SimpleCredentials(username,passwordHash.toCharArray());
      int maxConcurrentJobs = 1;
      SimpleMeandreServerProxyConfig config = new SimpleMeandreServerProxyConfig(
            username, password, host, port, maxConcurrentJobs);
      meandreServerProxy = new RemoteMeandreServerProxy(config);
      meandreServerProxy.setHead(true);
      ContentRepositoryService crs = new ContentRepositoryService();
      crs.setRepository(repository);
      meandreServerProxy.setArtifactService(crs);
      meandreServerProxy.setRepositoryClientConnectionPool(repositoryClientConnectionPool);
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
      String componentURIInvalid = "meandre://seasr.org/components/input#invaliduri";
      ExecutableComponentDescription ecd = 
         meandreServerProxy.getComponentDescription(componentURIInvalid);
   }
   @Ignore
   @Test
   public void testGetComponentDescriptionExists()
         throws MeandreServerException {
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
      Component component= new Component();
      component.setInstanceUri(componentInstance);
      component.setUri(componentURI);
      Map<String, Property> map=meandreServerProxy.getComponentPropertyDataType(credentials,component, flowURI);
      assertTrue(map.size()==0);
   }
   @Ignore
   @Test
   public void testGetComponentDataType() throws TransmissionException, SQLException, MeandreServerException{
      Component component= new Component();
      component.setInstanceUri(componentInstance);
      component.setUri(componentURI);
      Map<String, Property> map=meandreServerProxy.getComponentPropertyDataType(credentials,component, flowURI);
      assertTrue(map.size()>0);
   }
   @Ignore
   @Test
   public void testComponentDataTypeValidValue() throws TransmissionException, SQLException, MeandreServerException{
      Component component= new Component();
      component.setInstanceUri(componentInstance);
      component.setUri(componentURI);
      Map<String, Property> map=meandreServerProxy.getComponentPropertyDataType(credentials,component, flowURI);
   
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
      List<Component>componentList=meandreServerProxy.getComponents(credentials,flowURI);
      for(int i=0;i< componentList.size();i++){
         System.out.println("--> "+componentList.get(i).getInstanceUri() + " : " + componentList.get(i).getUri());
      }
      
   }
   
   @Ignore
   @Test 
   public void testcreateNewFlow(){
      HashMap<String,String> paramMap = new HashMap<String,String>();
      paramMap.put(componentProperty,componentPropertyValue);
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
      Map<Component,List<Property>> componentPropertyMap=meandreServerProxy.getAllComponentsAndPropertyDataTypes(credentials,flowURI);
      assertTrue(componentPropertyMap.size()>0);
   }
   
   
   @Test 
   public void testGetComponentsFromJCR()throws MeandreServerException{
	   List<Component>componentList=meandreServerProxy.getComponents(credentials,jcrFlowUri);
	      for(int i=0;i< componentList.size();i++){
	         System.out.println("--> "+componentList.get(i).getInstanceUri() + " : " + componentList.get(i).getUri());
	      }
	      
   }
   
   @Test 
   public void testGetAllComponentsAndPropertyDataTypesFromJCR()throws MeandreServerException{
	   Map<Component, List<Property>>componentMap=meandreServerProxy.getAllComponentsAndPropertyDataTypes(credentials, jcrFlowUri);
	    
	   for(Entry<Component, List<Property>> entry:componentMap.entrySet()){
		   System.out.println(" Property Name: "+entry.getKey().getName() + "  Property Value: " + entry.getValue().size());
		   if(entry.getValue().size()>0){
			   for(Property prop:entry.getValue()){
				   System.out.println("Property Value: " +prop.getValue());
			   }
		   }
	   }
   }
   
}
