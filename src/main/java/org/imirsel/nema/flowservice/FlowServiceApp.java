package org.imirsel.nema.flowservice;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;


public class FlowServiceApp {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// instantiate the spring container
		try {
			ApplicationContext ctx = new ClassPathXmlApplicationContext("org/imirsel/nema/flowservice/config/applicationContext.xml");
		} catch (BeansException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
