package org.imirsel.nema.flowservice;

import java.util.logging.Logger;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Main class for the NEMA Flow Service.
 * 
 * @author shirk
 */
public class FlowServiceApp {
	private static final Logger logger = 
		Logger.getLogger(NemaFlowServiceTest.class.getName());
	
	/**
	 * Main method for executing the application.
	 * 
	 * @param args Arguments for the application.
	 */
	public static void main(String[] args) {
		try {
			ApplicationContext ctx = 
				new ClassPathXmlApplicationContext("org/imirsel/nema/flowservice/config/applicationContext.xml");
		} catch (BeansException e) {
			e.printStackTrace();
		}
	}

}
