package org.imirsel.nema.flowservice;

import java.util.logging.Logger;

import org.springframework.beans.BeansException;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Main class for the NEMA Flow Service.
 * 
 * @author shirk
 * @since 0.4.0
 */
public class FlowServiceApp {
   private static final Logger logger = Logger.getLogger(FlowServiceApp.class
         .getName());

   /**
    * Main method for executing the application.
    * 
    * @param args Arguments for the application.
    */
   public static void main(String[] args) {
      String title = FlowServiceApp.class.getPackage().getImplementationTitle();
      String version = FlowServiceApp.class.getPackage()
            .getImplementationVersion();
      if (!(title == null || version == null)) {
         logger.config("Starting: " + title + " Implementation Version: "
               + version);
      }
      try {
         try {
            java.rmi.registry.LocateRegistry.createRegistry(1099);
            logger.config("RMI registry ready.");
         } catch (Exception e) {
            logger.config("Exception starting RMI registry:");
            e.printStackTrace();
         }

         new ClassPathXmlApplicationContext("applicationContext.xml");
         logger.info("NEMA Flow Service successfully started...");
         
      } catch (BeansException e) {
         e.printStackTrace();
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

}
