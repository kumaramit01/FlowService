package org.imirsel.nema.dao.impl;

import java.util.List;

import org.imirsel.nema.dao.JobDao;
import org.imirsel.nema.model.Job;

public class JobDaoImpl extends GenericDaoImpl<Job, Long>implements JobDao {

	public JobDaoImpl() {
		super(Job.class);
	}

	@Override
	public List<Job> getJobsByOwnerId(long userId) {
		// TODO Auto-generated method stub
		return null;
	}
	
}
