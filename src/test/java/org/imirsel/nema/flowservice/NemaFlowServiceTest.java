package org.imirsel.nema.flowservice;


import java.util.Date;
import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Logger;

import javax.jcr.SimpleCredentials;

import org.imirsel.nema.model.Flow;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Main class for the NEMA Flow Service.
 * 
 * @author shirk
 */
public class NemaFlowServiceTest {

   private static final Logger logger = Logger.getLogger(FlowServiceApp.class
         .getName());

   /**
    * Main method for executing the application.
    * 
    * @param args Arguments for the application.
    */
   public static void main(String[] args) {
      ApplicationContext ctx = null;
      ctx = new ClassPathXmlApplicationContext(
            "applicationContext-flowservice-test.xml");
      String username="admin";
      String passwordHash="b2cebd873228d3e6753d9b39195730694e3d1bbc";

 /*     FlowService flowService = (FlowService) ctx.getBean("flowService");

      Flow template = flowService.getFlow(10);

      Flow instance = new Flow();
      instance.setCreatorId(0L);
      instance.setDateCreated(new Date());
      instance.setInstanceOf(template);
      instance.setKeyWords(template.getKeyWords());
      instance.setName("Test instance");
      instance.setTemplate(false);
      instance.setDescription("An instance for testing.");
      instance.setType(template.getType());

      long userId=0l;
      SimpleCredentials credentials = new SimpleCredentials(username,passwordHash.toCharArray());
      String flowUri ="http://www.imirsel.org/test/testdynamiccomponentflow/";
      HashMap<String,String> paramMap = new HashMap<String,String>();
      paramMap.put("propertytostring_0_propertyvalue","hello world");
    
      flowService.createNewFlow(credentials, instance, paramMap, flowUri, userId);
      
      long instanceId = instance.getId();
      
      System.out.println("URI IS: ===> " + instance.getUri());

      for (int i = 0; i < 8; i++) {
         try {
            Thread.sleep(100);
         } catch (InterruptedException e) {
            e.printStackTrace();
         }
         flowService.executeJob(credentials,UUID.randomUUID().toString(), "Test: "
               + new Date().toString(), "Test job " + i, instanceId, 0L,
               "shirk@uiuc.edu");
      }
*/
   }

}
