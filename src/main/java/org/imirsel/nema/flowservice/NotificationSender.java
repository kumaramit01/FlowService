package org.imirsel.nema.flowservice;

import org.imirsel.nema.model.Notification;

public interface NotificationSender {
	public void send(Notification notification);
}
