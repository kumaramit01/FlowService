package org.imirsel.nema.dao.impl;

import java.util.List;

import org.imirsel.nema.dao.JobDao;
import org.imirsel.nema.model.Job;
import org.springframework.orm.toplink.SessionFactory;

public class JobDaoImpl implements JobDao {
	
	private SessionFactory sessionFactory;

	/* (non-Javadoc)
	 * @see org.imirsel.nema.dao.impl.JobDao#get(long)
	 */
	public Job get(long jobId) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.imirsel.nema.dao.impl.JobDao#remove(long)
	 */
	public void remove(long jobId) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.imirsel.nema.dao.impl.JobDao#save(org.imirsel.nema.model.Job)
	 */
	public void save(Job job) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.imirsel.nema.dao.impl.JobDao#getJobsByOwnerId(long)
	 */
	public List<Job> getJobsByOwnerId(long userId) {
		// TODO Auto-generated method stub
		return null;
	}

	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}

}
