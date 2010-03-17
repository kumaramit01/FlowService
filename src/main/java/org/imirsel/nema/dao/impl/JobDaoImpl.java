package org.imirsel.nema.dao.impl;

import java.util.List;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.imirsel.nema.dao.JobDao;
import org.imirsel.nema.model.Job;

/**
 * Hibernate-based implementation of a {@link Job} DAO.
 * 
 * @author shirk
 * @since 0.4.0
 */
public class JobDaoImpl extends GenericDaoImpl<Job, Long>implements JobDao {

   /**
    * @see JobDao#getJobsByOwnerId(long)
    */
	@Override
	public List<Job> getJobsByOwnerId(long ownerId) {
		Criterion userId=Restrictions.eq("ownerId", ownerId);
		return this.findByCriteriaDistinct(userId);
	}
	
}
