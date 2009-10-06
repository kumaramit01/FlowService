package org.imirsel.nema.flowservice;

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.imirsel.nema.model.Flow;
import org.imirsel.nema.model.Job;
import org.imirsel.nema.model.Notification;

/**
 * A {@link FlowService} implementation for the NEMA project.
 * 
 * @author shirk
 * @since 1.0
 */
public class NemaFlowService implements FlowService {

	private JobScheduler jobScheduler;
	
	/**
	 * @see org.imirsel.nema.flowservice.FlowService#abortJob(long)
	 */
	@Override
	public void abortJob(long jobId) throws IllegalStateException {
		// lookup the job to get current status
		// if finished, throw illegal state exception
		// if running call jobscheduler abortjob
	}

	/**
	 * @see org.imirsel.nema.flowservice.FlowService#deleteJob(long)
	 */
	@Override
	public void deleteJob(long jobId) throws IllegalStateException {
		// validate that the job has finished executing
		// throw illegal state exception if still running
		// delete the job from the database
		// delete the results from disk?
		// delete the flow instance used for the job
	}

	/**
	 * @see org.imirsel.nema.flowservice.FlowService#executeJob(String, String, String, long, long, String)
	 */
	@Override
	public Job executeJob(String token, String name, String description, long flowInstanceId, long userId,
			String userEmail) {
		Job job = new Job();
		job.setToken(token);
		job.setName(name);
		job.setDescription(description);
		// Lookup flow with given instance id
		// Set flow from previous operation
		job.setOwnerId(userId);
		job.setOwnerEmail(userEmail);
		job.setSubmitTimestamp(new Date());
		// persist job object
		jobScheduler.scheduleJob(job);
		return job;
	}

	/**
	 * @see org.imirsel.nema.flowservice.FlowService#getFlowTemplates()
	 */
	@Override
	public Set<Flow> getFlowTemplates() {
		// return all flows where isTemplate = true
		return null;
	}

	/**
	 * @see org.imirsel.nema.flowservice.FlowService#getJob(long)
	 */
	@Override
	public Job getJob(long jobId) {
		// return job with the specified id
		return null;
	}

	/**
	 * @see org.imirsel.nema.flowservice.FlowService#getUserJobs(long)
	 */
	@Override
	public List<Job> getUserJobs(long userId) {
		// return all jobs for the specified user
		return null;
	}

	/**
	 * @see org.imirsel.nema.flowservice.FlowService#getUserNotifications(long)
	 */
	@Override
	public List<Notification> getUserNotifications(long userId) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see org.imirsel.nema.flowservice.FlowService#storeFlowInstance(Flow)
	 */
	@Override
	public Long storeFlowInstance(Flow instance) {
		// TODO Auto-generated method stub
		return null;
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
	
	
}
