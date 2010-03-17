package org.imirsel.nema.dao;

import java.util.List;

import org.imirsel.nema.model.Job;

/**
 * Interface for {@link Job} Data Access Objects.
 * 
 * @author shirk
 * @since 0.4.0
 */
public interface JobDao extends GenericDao<Job, Long>{

   /**
    * Return all jobs that are owned by a particular user.
    * 
    * @param userId Unique ID of the user for whom jobs should be returned.
    * @return List of {@link Job}s that are owned by the specified user.
    */
	public abstract List<Job> getJobsByOwnerId(long userId);

}