package org.imirsel.nema.flowservice.notification;

import org.imirsel.nema.model.Notification;

/**
 * Interface for classes that provide services for sending notifications.
 * 
 * @author shirk
 * @since 0.5.0
 *
 */
public interface NotificationSender {
   
   /**
    * Send a notification. The destination of the notification will be
    * implementation specific.
    * 
    * @param notification Notification to send.
    */
	public void send(Notification notification);
}
