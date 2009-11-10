package org.imirsel.nema.flowservice;

import java.util.logging.Logger;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.imirsel.nema.dao.DaoFactory;
import org.imirsel.nema.dao.NotificationDao;
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

	private static final Logger logger = 
		Logger.getLogger(NotificationCreator.class.getName());
	private final DaoFactory daoFactory;
	
	/**
	 * Create a new instance with the specified {@link NotificationDao}.
	 * 
	 * @param notificaitonDao The {@link NotificationDao} to use to store
	 * {@link Notification}s persistently.
	 */
	public NotificationCreator(DaoFactory daoFactory) {
		this.daoFactory = daoFactory;
	}
	
	/**
	 * Receive a job update and create a {@link Notification} for it.
	 */
	@Override
	public void jobStatusUpdate(Job job) {
		logger.fine("Job status update received for job " + job.getId() + ".");
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
		NotificationDao notificationDao = daoFactory.getNotificationDao();
        Session session = notificationDao.getSessionFactory().openSession();
        notificationDao.startManagedSession(session);
        Transaction transaction = session.beginTransaction();

		logger.fine("Preparing to persist a status update for job " + job.getId() + ".");
        notificationDao.makePersistent(notification);
        
        transaction.commit();
		logger.fine("Job status update for job " + job.getId() + " was successfully persisted.");
        notificationDao.endManagedSession();
        session.close();
	}

}
