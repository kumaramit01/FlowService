package org.imirsel.nema.dao.impl;

import java.util.List;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.imirsel.nema.dao.NotificationDao;
import org.imirsel.nema.model.Notification;

public class NotificationDaoImpl extends GenericDaoImpl<Notification, Long>implements NotificationDao {
	
	public NotificationDaoImpl() {
	}

	@Override
	public List<Notification> getNotificationsByRecipientId(Long recipientId) {
		Criterion restriction=Restrictions.eq("recipientId", recipientId);
		return this.findByCriteria(restriction);
	}

}
