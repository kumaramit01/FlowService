package org.imirsel.nema.flowservice;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class NemaFlowServiceClientTest {

	FlowService flowService;

	public FlowService getFlowService() {
		return flowService;
	}

	public void setFlowService(FlowService flowService) {
		this.flowService = flowService;
	}
	
	public static void main(String[] args) {
		ApplicationContext ctx  =  null;
	    ctx = new ClassPathXmlApplicationContext("applicationContext.xml");
	    
		FlowService flowService = (FlowService)ctx.getBean("flowService");
	}
}
