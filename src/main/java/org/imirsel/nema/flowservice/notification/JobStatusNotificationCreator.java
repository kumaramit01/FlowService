package org.imirsel.nema.flowservice.notification;

import java.util.logging.Logger;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.imirsel.nema.dao.DaoFactory;
import org.imirsel.nema.dao.NotificationDao;
import org.imirsel.nema.flowservice.monitor.JobStatusUpdateListener;
import org.imirsel.nema.model.Job;
import org.imirsel.nema.model.Notification;

/**
 * Creates {@link Notification}s for users as {@link Job} status updates occur.
 * 
 * @author shirk
 * @since 0.5.0
 */
public class JobStatusNotificationCreator implements JobStatusUpdateListener {

	private static final Logger logger = 
		Logger.getLogger(JobStatusNotificationCreator.class.getName());
	private final DaoFactory daoFactory;
	
	/**
	 * Create a new instance with the specified {@link NotificationDao}.
	 * @param daoFactory Sets the daoFactory
	 * 
	 * @param notificaitonDao The {@link NotificationDao} to use to store
	 * {@link Notification}s persistently.
	 */
	public JobStatusNotificationCreator(DaoFactory daoFactory) {
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
		StringBuilder subjectBuilder = new StringBuilder();
		subjectBuilder.append("Your NEMA job ");
		StringBuilder messageBuilder = new StringBuilder();
		messageBuilder.append("Job " + job.getId() + " (" + job.getName() + ")" + " ");
		switch(job.getJobStatus()) {
		  case UNKNOWN:
			  messageBuilder.append("was scheduled: " + job.getSubmitTimestamp().toString() + ".");
			  subjectBuilder.append("has been scheduled.");
			  break;
		  case SCHEDULED:
		     messageBuilder.append("was scheduled: " + job.getScheduleTimestamp().toString() + ".");
           subjectBuilder.append("has been scheduled.");
		     break;
		  case SUBMITTED:
			  messageBuilder.append("was submitted: " + job.getSubmitTimestamp().toString() + ".");
           subjectBuilder.append("has been submitted.");
			  break;
		  case STARTED:
			  messageBuilder.append("has started: " + job.getStartTimestamp().toString() + ".");
           subjectBuilder.append("started.");
			  break;
		  case FINISHED:
			  messageBuilder.append("has finished: " + job.getEndTimestamp().toString() + ".");
           subjectBuilder.append("finished.");
			  break;
		  case FAILED:
			  messageBuilder.append("has failed: " + job.getEndTimestamp().toString() + ".");
           subjectBuilder.append("failed.");
			  break;
		  case ABORTED:
			  messageBuilder.append("was aborted: " + job.getEndTimestamp().toString() + ".");
           subjectBuilder.append("was aborted.");
			  break;
		}
		messageBuilder.append("\n\nFor more details, see http://nema.lis.uiuc.edu/diy/get/JobManager.jobDetail?id=" + job.getId());
		notification.setMessage(messageBuilder.toString());
		notification.setSubject(subjectBuilder.toString());
		NotificationDao notificationDao = daoFactory.getNotificationDao();
        Session session = notificationDao.getSessionFactory().openSession();
        notificationDao.startManagedSession(session);
        Transaction transaction = session.beginTransaction();

		logger.fine("Preparing to persist a status change notification for job " + job.getId() + ".");
        notificationDao.makePersistent(notification);
        
        transaction.commit();
		logger.fine("Job status update notification for job " + job.getId() + " was successfully persisted.");
        notificationDao.endManagedSession();
        session.close();
	}

}
