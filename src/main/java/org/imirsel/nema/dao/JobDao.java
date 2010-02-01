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

	public abstract List<Job> getJobsByOwnerId(long userId);

}