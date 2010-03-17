package org.imirsel.nema.dao.impl;

import org.hibernate.SessionFactory;
import org.imirsel.nema.dao.DaoFactory;
import org.imirsel.nema.dao.FlowDao;
import org.imirsel.nema.dao.JobDao;
import org.imirsel.nema.dao.JobResultDao;
import org.imirsel.nema.dao.NotificationDao;

/**
 * Hibernate-based implementation of a Data Access Object factory.
 * 
 * @author shirk
 * @since 0.4.0
 */
public class DaoFactoryImpl implements DaoFactory {

	private SessionFactory sessionFactory;
	
	/**
	 * @see DaoFactory#getFlowDao()
	 */
	@Override
	public FlowDao getFlowDao() {
		FlowDao flowDao = new FlowDaoImpl();
		flowDao.setSessionFactory(sessionFactory);
		return flowDao;
	}

	/**
    * @see DaoFactory#getJobDao()
    */
	@Override
	public JobDao getJobDao() {
		JobDao jobDao = new JobDaoImpl();
		jobDao.setSessionFactory(sessionFactory);
		return jobDao;
	}

	/**
    * @see DaoFactory#getJobResultDao()
    */
	@Override
	public JobResultDao getJobResultDao() {
		JobResultDao jobResultDao = new JobResultDaoImpl();
		jobResultDao.setSessionFactory(sessionFactory);
		return jobResultDao;
	}

	/**
    * @see DaoFactory#getNotificationDao()
    */
	@Override
	public NotificationDao getNotificationDao() {
		NotificationDao notificationDao = new NotificationDaoImpl();
		notificationDao.setSessionFactory(sessionFactory);
		return notificationDao;
	}

	/**
	 * Inject the provided {@link SessionFactory} to use for creating
	 * transactional {@link Session}s.
	 * 
	 * @param sessionFactory The {@link SessionFactory} to use.
	 */
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	/**
	 * Return the {@link SessionFactory} that is currently being used.
	 * 
	 * @return The {@link SessionFactory} that is currently being used.
	 */
	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}
}

