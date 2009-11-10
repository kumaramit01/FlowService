package org.imirsel.nema.dao.impl;

import org.hibernate.SessionFactory;
import org.imirsel.nema.dao.DaoFactory;
import org.imirsel.nema.dao.FlowDao;
import org.imirsel.nema.dao.JobDao;
import org.imirsel.nema.dao.JobResultDao;
import org.imirsel.nema.dao.NotificationDao;

public class DaoFactoryImpl implements DaoFactory {

	private SessionFactory sessionFactory;
	
	@Override
	public FlowDao getFlowDao() {
		FlowDao flowDao = new FlowDaoImpl();
		flowDao.setSessionFactory(sessionFactory);
		return flowDao;
	}

	@Override
	public JobDao getJobDao() {
		JobDao jobDao = new JobDaoImpl();
		jobDao.setSessionFactory(sessionFactory);
		return jobDao;
	}

	@Override
	public JobResultDao getJobResultDao() {
		JobResultDao jobResultDao = new JobResultDaoImpl();
		jobResultDao.setSessionFactory(sessionFactory);
		return jobResultDao;
	}

	@Override
	public NotificationDao getNotificationDao() {
		NotificationDao notificationDao = new NotificationDaoImpl();
		notificationDao.setSessionFactory(sessionFactory);
		return notificationDao;
	}

	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}
}

