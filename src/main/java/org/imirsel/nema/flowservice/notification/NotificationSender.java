package org.imirsel.nema.flowservice.notification;

import org.imirsel.nema.model.Notification;

public interface NotificationSender {
	public void send(Notification notification);
}
