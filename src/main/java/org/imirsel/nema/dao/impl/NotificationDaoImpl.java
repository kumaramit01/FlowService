package org.imirsel.nema.dao.impl;

import java.util.List;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.imirsel.nema.dao.NotificationDao;
import org.imirsel.nema.model.Notification;

/**
 * Hibernate-based implementation of a {@link Notification} DAO.
 * 
 * @author shirk
 * @since 0.4.0
 */
public class NotificationDaoImpl extends GenericDaoImpl<Notification, Long>implements NotificationDao {

   /**
    * @see NotificationDao#getNotificationsByRecipientId(Long)
    */
	@Override
	public List<Notification> getNotificationsByRecipientId(Long recipientId) {
		Criterion restriction=Restrictions.eq("recipientId", recipientId);
		return this.findByCriteria(restriction);
	}
	
	/**
	 * @see NotificationDao#getUnsentNotifications()
	 */
	public List<Notification> getUnsentNotifications() {
		Criterion restriction=Restrictions.eq("deliveryStatusCode", -1);
		return this.findByCriteria(restriction);
	}

}
