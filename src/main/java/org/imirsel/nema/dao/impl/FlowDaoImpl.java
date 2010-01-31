package org.imirsel.nema.dao.impl;

import java.util.List;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.imirsel.nema.dao.FlowDao;
import org.imirsel.nema.model.Flow;

public class FlowDaoImpl extends GenericDaoImpl<Flow,Long> implements FlowDao {
	 
	   
	public FlowDaoImpl() {
	}

	@Override
	public List<Flow> getFlowTemplates() {
		Criterion restriction=Restrictions.eq("template", Boolean.TRUE);
		return this.findByCriteria(restriction);
	}

	@Override
	public List<Flow> getFlowsByCreatorId(Long creatorId) {
		Criterion restriction=Restrictions.eq("creatorId", creatorId);
		return this.findByCriteria(restriction);
	}

}
