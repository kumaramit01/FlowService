package org.imirsel.nema.dao.impl;

import java.util.List;

import org.imirsel.nema.dao.FlowDao;
import org.imirsel.nema.model.Flow;

public class FlowDaoImpl extends GenericDaoImpl<Flow,Long> implements FlowDao {
	
	public FlowDaoImpl() {
		super(Flow.class);
	}

	@Override
	public List<Flow> getFlowTemplates() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Flow> getFlowsByCreatorId(Long userId) {
		// TODO Auto-generated method stub
		return null;
	}

}
