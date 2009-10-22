package org.imirsel.nema.flowservice;

import org.imirsel.nema.dao.NotificationDao;
import org.imirsel.nema.flowservice.monitor.JobStatusUpdateHandler;
import org.imirsel.nema.model.Job;
import org.imirsel.nema.model.Notification;

public class NotificationCreator implements JobStatusUpdateHandler {

	private final NotificationDao notificationDao;
	
	public NotificationCreator(NotificationDao notificaitonDao) {
		this.notificationDao = notificaitonDao;
	}
	
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
