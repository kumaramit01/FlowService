package org.imirsel.nema.dao.impl;

import java.util.List;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.imirsel.nema.dao.JobDao;
import org.imirsel.nema.model.Job;

public class JobDaoImpl extends GenericDaoImpl<Job, Long>implements JobDao {

	public JobDaoImpl() {
	}

	@Override
	public List<Job> getJobsByOwnerId(long userId) {
		// TODO Auto-generated method stub
		Criterion restriction=Restrictions.eq("userId", userId);
		return this.findByCriteria(restriction);
	}
	
}
