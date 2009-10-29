package org.imirsel.nema.dao;

import java.util.List;

import org.imirsel.nema.model.Job;

public interface JobDao extends GenericDao<Job, Long>{

	public abstract List<Job> getJobsByOwnerId(long userId);

}