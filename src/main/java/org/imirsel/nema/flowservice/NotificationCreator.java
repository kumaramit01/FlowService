package org.imirsel.nema.flowservice;

import org.imirsel.nema.dao.impl.NotificationDaoImpl;
import org.imirsel.nema.flowservice.monitor.JobStatusUpdateHandler;
import org.imirsel.nema.model.Job;
import org.imirsel.nema.model.Notification;

/**
 * Creates {@link Notification}s for users as {@link Job} status updates occur.
 * 
 * @author shirk
 * @since 1.0
 */
public class NotificationCreator implements JobStatusUpdateHandler {

	private final NotificationDaoImpl notificationDao;
	
	/**
	 * Create a new instance with the specified {@link NotificationDaoImpl}.
	 * 
	 * @param notificaitonDao The {@link NotificationDaoImpl} to use to store
	 * {@link Notification}s persistently.
	 */
	public NotificationCreator(NotificationDaoImpl notificaitonDao) {
		this.notificationDao = notificaitonDao;
	}
	
	/**
	 * Receive a job update and create a {@link Notification} for it.
	 */
	@Override
	public void jobStatusUpdate(Job job) {
		Notification notification = new Notification();
		notification.setRecipientId(job.getOwnerId());
		notification.setRecipientEmail(job.getOwnerEmail());
		StringBuilder messageBuilder = new StringBuilder();
		messageBuilder.append("Job " + job.getId() + " (" + job.getName() + ")" + " ");
		switch(job.getJobStatus()) {
		  case SUBMITTED:
			  messageBuilder.append("was submitted: " + job.getSubmitTimestamp().toString() + ".");
			  break;
		  case STARTED:
			  messageBuilder.append("was started: " + job.getStartTimestamp().toString() + ".");
			  break;
		  case FINISHED:
			  messageBuilder.append("has finished: " + job.getEndTimestamp().toString() + ".");
			  break;
		  case FAILED:
			  messageBuilder.append("has failed: " + job.getEndTimestamp().toString() + ".");
			  break;
		  case ABORTED:
			  messageBuilder.append("was aborted: " + job.getEndTimestamp().toString() + ".");
			  break;
		}
		notification.setMessage(messageBuilder.toString());
		notificationDao.save(notification);
	}

}
