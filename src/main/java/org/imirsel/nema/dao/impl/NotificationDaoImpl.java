package org.imirsel.nema.dao.impl;

import java.util.List;

import org.imirsel.nema.dao.NotificationDao;
import org.imirsel.nema.model.Notification;

public class NotificationDaoImpl extends GenericDaoImpl<Notification, Long>implements NotificationDao {
	
	public NotificationDaoImpl() {
	}

	@Override
	public List<Notification> getNotificationsByRecipientId(Long userId) {
		// TODO Auto-generated method stub
		return null;
	}

}
