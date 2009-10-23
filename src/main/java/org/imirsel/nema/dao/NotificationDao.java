package org.imirsel.nema.dao;

import java.util.List;

import org.imirsel.nema.model.Notification;

public interface NotificationDao {

	void save(Notification notification);

	List<Notification> getNotificationsByRecipientId(long userId);

}