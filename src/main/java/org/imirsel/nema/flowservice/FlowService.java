package org.imirsel.nema.flowservice;

import org.imirsel.nema.model.Flow;
import org.imirsel.nema.model.Job;
import org.imirsel.nema.model.Notification;

import java.util.List;
import java.util.Set;


/**
 * Provides operations for executing {@link Flow}s and monitoring {@link Job}s.
 *
 * @author shirk
 * @since 1.0
 */
public interface FlowService {

   //~ Methods -----------------------------------------------------------------

	/**
	 * Kill a running {@link Job}.
	 * 
	 * @param jobId Unique ID of the {@link Job} to kill.
	 * @throws IllegalStateException if the {@link Job} is not running.
	 */
   public void abortJob(long jobId) throws IllegalStateException;

	/**
	 * Delete a {@link Job}, the flow instance that was executed, 
	 * and the results the {@link Job} produced.
	 * 
	 * @param jobId Unique ID of the {@link Job} to delete.
	 * @throws IllegalStateException if the {@link Job} is not finished.
	 */
   public void deleteJob(long jobId) throws IllegalStateException;

	/**
	 * Execute a {@link Job} using the specified {@link Flow} instance on 
	 * behalf of the specified user.
	 * 
	 * @param token A unique identifier for the {@link Job}.
	 * @param name User supplied name for the job.
	 * @param description User supplied description for the job.
	 * @param flowInstanceId Unique ID of the {@link Flow} instance to execute.
	 * @param userId Unique ID of the user on whose behalf the {@link Job} 
	 * will be executed.
	 * @param userEmail Email address for the user where notifications about 
	 * {@link Job} status changes should be sent.
	 * @return 
	 */
   public Job executeJob(
      String token, String name, String description, long flowInstanceId,
      long userId,
      String userEmail);

	/**
	 * Return all {@link Flow}s that are templates. Template {@link Flow}s are
	 * never changed.
	 * 
	 * @return All {@link Flow}s that are templates.
	 */
   public Set<Flow> getFlowTemplates();

	/**
	 * Return the details of a job with the specified ID.
	 * 
	 * @param jobId The ID of the job to get details for.
	 */
   public Job getJob(long jobId);

	/**
	 * Return all {@link Job}s owned by the specified user.
	 * 
	 * @param userId Unique ID of the user whose {@link Job}s will be returned.
	 */
   public List<Job> getUserJobs(long userId);

	/**
	 * Return {@link Notification}s that have been sent to the specified user.
	 * 
	 * @param userId Unique ID of the user for whom {@link Notification}s 
	 * should be returned.
	 */
   public List<Notification> getUserNotifications(long userId);

	/**
	 * Persist the specified {@link Flow} instance.
	 * 
	 * @param instance The {@link Flow} instance that should be stored.
	 * @return The flow instance ID.
	 */
   public Long storeFlowInstance(Flow instance);

}
