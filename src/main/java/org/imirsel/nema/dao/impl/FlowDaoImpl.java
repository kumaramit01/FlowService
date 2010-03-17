package org.imirsel.nema.dao.impl;

import java.util.List;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.imirsel.nema.dao.FlowDao;
import org.imirsel.nema.model.Flow;

/**
 * Hibernate-based implementation of a {@link Flow} DAO.
 * 
 * @author shirk
 * @since 0.4.0
 */
public class FlowDaoImpl extends GenericDaoImpl<Flow,Long> implements FlowDao {

   /**
    * @see FlowDao#getFlowTemplates()
    */
	@Override
	public List<Flow> getFlowTemplates() {
		Criterion restriction=Restrictions.eq("template", Boolean.TRUE);
		return this.findByCriteria(restriction);
	}

	/**
	 * @see FlowDao#getFlowsByCreatorId(Long)
	 */
	@Override
	public List<Flow> getFlowsByCreatorId(Long creatorId) {
		Criterion restriction=Restrictions.eq("creatorId", creatorId);
		return this.findByCriteria(restriction);
	}

}
