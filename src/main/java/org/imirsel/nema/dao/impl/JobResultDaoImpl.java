package org.imirsel.nema.dao.impl;

import org.imirsel.nema.dao.JobResultDao;
import org.springframework.orm.toplink.SessionFactory;

public class JobResultDaoImpl implements JobResultDao {
	
	private SessionFactory sessionFactory;

	public void remove(Long id) {
		// TODO Auto-generated method stub
		
	}

	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}

}
