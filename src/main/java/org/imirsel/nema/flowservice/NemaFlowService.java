package org.imirsel.nema.flowservice;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.imirsel.nema.NoSuchEntityException;
import org.imirsel.nema.model.Flow;
import org.imirsel.nema.model.Job;
import org.imirsel.nema.model.JobResult;
import org.imirsel.nema.model.Notification;
import org.imirsel.nema.dao.FlowDao;
import org.imirsel.nema.dao.JobDao;
import org.imirsel.nema.dao.JobResultDao;
import org.imirsel.nema.dao.NotificationDao;
import org.springframework.orm.ObjectRetrievalFailureException;

/**
 * A {@link FlowService} implementation for the NEMA project.
 * 
 * @author shirk
 * @since 1.0
 */
public class NemaFlowService implements FlowService, JobStatusUpdateHandler {

	private JobScheduler jobScheduler;
	
	private JobStatusMonitor jobStatusMonitor;
	
	private FlowDao flowDao;
	
	private JobDao jobDao;
	
	private JobResultDao resultDao;
	
	private NotificationDao notificationDao;
	
	public NemaFlowService() {
		
	}
	
	@PostConstruct
	public void init() {
		assert jobDao!=null:"Job DAO is null!";
		jobStatusMonitor = new JobStatusMonitor(jobDao);
	}
	
	/**
	 * @see org.imirsel.nema.flowservice.FlowService#abortJob(long)
	 */
	@Override
	public void abortJob(long jobId) throws IllegalStateException {
		Job job;
		try {
			job = jobDao.get(jobId);
		} catch (ObjectRetrievalFailureException e) {
			throw new NoSuchEntityException("Job " + jobId + " does not exist.");
		}
        // Job must be running to be aborted.
		if(job.isEnded()) {
			throw new IllegalStateException("Cannot abort job " + jobId + 
					" because it has already ended.");
		}
		jobScheduler.abortJob(job);
	}

	/**
	 * @see org.imirsel.nema.flowservice.FlowService#deleteJob(long)
	 */
	@Override
	public void deleteJob(long jobId) throws IllegalStateException {
		Job job;
		try {
			job = jobDao.get(jobId);
		} catch (ObjectRetrievalFailureException e) {
			throw new NoSuchEntityException("Job " + jobId + " does not exist.");
		}
		// Job must be finished to be deleted.
		if(!job.isEnded()) {
			throw new IllegalArgumentException("Cannot delete job " + jobId + 
					" because it is still running.");
		}
		for(JobResult result : job.getResults()) {
			resultDao.remove(result.getId());
		}
		jobDao.remove(jobId);
		flowDao.remove(job.getFlow().getId());
		// delete the results from disk?
	}

	/**
	 * @see org.imirsel.nema.flowservice.FlowService#executeJob(String, String, String, long, long, String)
	 */
	@Override
	public Job executeJob(String token, String name, String description, long flowInstanceId, long userId,
			String userEmail) {
		Flow flowInstance;
		try {
			flowInstance = flowDao.get(flowInstanceId);
		} catch (Exception e) {
			throw new NoSuchEntityException("Flow instance " + 
					flowInstanceId + " does not exist.");
		}
		
		Job job = new Job();
		job.setToken(token);
		job.setName(name);
		job.setDescription(description);
		job.setFlow(flowInstance);
		job.setOwnerId(userId);
		job.setOwnerEmail(userEmail);
		job.setSubmitTimestamp(new Date());
		
		jobDao.save(job);
		jobScheduler.scheduleJob(job);
		jobStatusMonitor.monitor(job,this);
		
		return job;
	}

	/**
	 * @see org.imirsel.nema.flowservice.FlowService#getFlowTemplates()
	 */
	@Override
	public Set<Flow> getFlowTemplates() {
		Set<Flow> flowSet = new HashSet<Flow>();
		flowSet.addAll(flowDao.getFlowTemplates());
		return flowSet;
	}

	/**
	 * @see org.imirsel.nema.flowservice.FlowService#getJob(long)
	 */
	@Override
	public Job getJob(long jobId) {
		Job job;
		try {
			job = jobDao.get(jobId);
		} catch (ObjectRetrievalFailureException e) {
			throw new NoSuchEntityException("Job " + jobId + " does not exist.");
		}
		return job;
	}

	/**
	 * @see org.imirsel.nema.flowservice.FlowService#getUserJobs(long)
	 */
	@Override
	public List<Job> getUserJobs(long userId) {
		return jobDao.getJobsByOwnerId(userId);
	}

	/**
	 * @see org.imirsel.nema.flowservice.FlowService#getUserNotifications(long)
	 */
	@Override
	public List<Notification> getUserNotifications(long userId) {
		return notificationDao.getNotificationsByRecipientId(userId);
	}

	/**
	 * @see org.imirsel.nema.flowservice.FlowService#storeFlowInstance(Flow)
	 */
	@Override
	public Long storeFlowInstance(Flow instance) {
		flowDao.save(instance);
		// store the flow on disk
		return instance.getId();
	}

	
	/**
	 * Return the {@link JobScheduler} instance currently being used.
	 * 
	 * @return Current {@link JobScheduler} instance.
	 */
	public JobScheduler getJobScheduler() {
		return jobScheduler;
	}

	/**
	 * Set the {@link JobScheduler} instance to use.
	 * 
	 * @param jobScheduler The {@link JobScheduler} to use.
	 */
	public void setJobScheduler(JobScheduler jobScheduler) {
		this.jobScheduler = jobScheduler;
	}
	
	public FlowDao getFlowDao() {
		return flowDao;
	}
	
	public void setFlowDao(FlowDao flowDao) {
		this.flowDao = flowDao;
	}
	
	public JobDao getJobDao() {
		return jobDao;
	}
	
	public void setJobDao(JobDao jobDao) {
		this.jobDao = jobDao;
	}
	
	public JobResultDao getJobResultDao() {
		return resultDao;
	}
	
	public void setJobResultDao(JobResultDao resultDao) {
		this.resultDao = resultDao;
	}
	
	public NotificationDao getNotificationDao() {
		return notificationDao;
	}
	
	public void setNotificationDao(NotificationDao notificationDao) {
		this.notificationDao = notificationDao;
	}

	@Override
	public void jobStatusUpdate(Job job) {
		// handle the status update
		// create notification
		// send if needed
		// if job is done, remove from the monitor
	}

}
