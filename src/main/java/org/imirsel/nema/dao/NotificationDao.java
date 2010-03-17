package org.imirsel.nema.dao;

import java.util.List;

import org.imirsel.nema.model.Notification;

/**
 * Interface for {@link Notification} Data Access Objects.
 * 
 * @author shirk
 * @since 0.4.0
 */
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
    
    /**
     * Return the {@link Notification}s that have not yet been sent to the
     * recipient.
     * 
     * @return List of unsent {@link Notification}s.
     */
    List<Notification> getUnsentNotifications();
}