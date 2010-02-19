package org.imirsel.nema.flowservice;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Date;
import java.util.HashMap;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;

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

      FlowService flowService = (FlowService) ctx.getBean("flowService");

      Flow template = flowService.getFlow(2);

      Flow instance = new Flow();
      instance.setCreatorId(0L);
      instance.setDateCreated(new Date());
      instance.setInstanceOf(template);
      instance.setKeyWords(template.getKeyWords());
      instance.setName("Test instance");
      instance.setTemplate(false);
      instance
            .setUrl("repository\\x0\\http___test.org_datatypetest_datatypetest1266359499405_.nt");
      instance.setDescription("An instance for testing.");
      instance.setType(template.getType());

      long instanceId = flowService.storeFlowInstance(instance);

      for (int i = 0; i < 8; i++) {
         try {
            Thread.sleep(100);
         } catch (InterruptedException e) {
            e.printStackTrace();
         }

         flowService.executeJob(UUID.randomUUID().toString(), "Test: "
               + new Date().toString(), "Test job " + i, instanceId, 0L,
               "shirk@uiuc.edu");
      }

   }

}
