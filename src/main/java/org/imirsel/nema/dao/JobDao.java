package org.imirsel.nema.dao;

import java.util.List;

import org.imirsel.nema.model.Job;

public interface JobDao {

	public abstract Job get(long jobId);

	public abstract void remove(long jobId);

	public abstract void save(Job job);

	public abstract List<Job> getJobsByOwnerId(long userId);

}