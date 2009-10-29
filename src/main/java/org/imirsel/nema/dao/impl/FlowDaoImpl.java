package org.imirsel.nema.dao.impl;

import java.util.Collection;

import org.imirsel.nema.dao.FlowDao;
import org.imirsel.nema.model.Flow;
import org.springframework.orm.toplink.SessionFactory;

public class FlowDaoImpl implements FlowDao {
	
	 private SessionFactory sessionFactory;

	

	/* (non-Javadoc)
	 * @see org.imirsel.nema.dao.impl.FlowDao#remove(java.lang.Long)
	 */
	public void remove(Long id) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.imirsel.nema.dao.impl.FlowDao#get(long)
	 */
	public Flow get(long flowInstanceId) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.imirsel.nema.dao.impl.FlowDao#getFlowTemplates()
	 */
	public Collection<? extends Flow> getFlowTemplates() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.imirsel.nema.dao.impl.FlowDao#save(org.imirsel.nema.model.Flow)
	 */
	public void save(Flow instance) {
		// TODO Auto-generated method stub
		
	}

	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}

}
