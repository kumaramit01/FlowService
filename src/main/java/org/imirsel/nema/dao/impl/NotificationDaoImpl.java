package org.imirsel.nema.dao.impl;

import java.util.List;

import org.imirsel.nema.dao.NotificationDao;
import org.imirsel.nema.model.Notification;
import org.springframework.orm.toplink.SessionFactory;

public class NotificationDaoImpl implements NotificationDao {
	
	
	private SessionFactory sessionFactory;

	public List<Notification> getNotificationsByRecipientId(long userId) {
		// TODO Auto-generated method stub
		return null;
	}

	public void save(Notification notification) {
		// TODO Auto-generated method stub
		
	}

	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}

}
