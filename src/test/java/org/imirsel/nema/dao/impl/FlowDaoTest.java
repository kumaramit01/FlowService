package org.imirsel.nema.dao.impl;

import java.util.List;

import org.imirsel.nema.dao.FlowDao;
import org.imirsel.nema.model.Flow;
import org.junit.Test;


public class FlowDaoTest extends BaseDaoTestCase {

	private FlowDao flowDao;
	
	@Test
	public void testFlowDaoGet(){
		List<Flow> flowList=this.getFlowDao().getFlowsByCreatorId(100l);
		assertTrue(flowList.size()==1);
	}
	
	@Test
	public void testFlowDaoTemplateGet(){
		List<Flow> flowList=this.getFlowDao().getFlowTemplates();
		
		for(Flow flow: flowList){
			System.out.println(flow.getTypeName());
		}
		assertTrue(flowList.size()==1);
	}
	
	
	
	

	public void setFlowDao(FlowDao flowDao) {
		this.flowDao = flowDao;
	}

	public FlowDao getFlowDao() {
		return flowDao;
	}
	
}
