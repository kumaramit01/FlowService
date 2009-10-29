package org.imirsel.nema.dao;

import java.util.List;

import org.imirsel.nema.model.Notification;

public interface NotificationDao extends GenericDao<Notification, Long>{

    /**
     * Return the {@link Notifications}s that the specified user was the
     * recipient.
     *
     * @param userId The unique ID of the user for whom 
     * {@link Notification}s will be fetched.
     * @return A list of {@link Notifications}s.
     */
    List<Notification> getNotificationsByRecipientId(Long userId);
}