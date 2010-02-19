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
	private static final Logger logger = 
		Logger.getLogger(FlowServiceApp.class.getName());
	
	/**
	 * Main method for executing the application.
	 * 
	 * @param args Arguments for the application.
	 */
	public static void main(String[] args) {
		String title=FlowServiceApp.class.getPackage().getImplementationTitle();
		String version = FlowServiceApp.class.getPackage().getImplementationVersion();
		if(!(title==null || version==null)){
		System.out.println("Starting: " +  title  + " Implementation Version: " + version);
		}
		try {
			try {
		            java.rmi.registry.LocateRegistry.createRegistry(1099);
			 	   System.out.println("RMI registry ready.");
		    } catch (Exception e) {
				   System.out.println("Exception starting RMI registry:");
				   e.printStackTrace();
		    } 
				   
			logger.info("Nema Flow Service starting up...");
			new ClassPathXmlApplicationContext("applicationContext.xml");
		} catch (BeansException e) {
			e.printStackTrace();
		}
	}

}
