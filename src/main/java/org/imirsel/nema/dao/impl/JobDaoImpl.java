package org.imirsel.nema.dao.impl;

import java.util.List;
import org.hibernate.Query;
import org.hibernate.Session;

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
	
   /**
    * @see JobDao#getJobsByStatus(Job.JobStatus)
    */
   public List<Job> getJobsByStatus(Job.JobStatus status) {
      Criterion statusCode=Restrictions.eq("statusCode", status.getCode());
      return this.findByCriteriaDistinct(statusCode);
   }

    /**
     * @see JobDao#getJobsByOwnerIdAndTaskId(long, long)
     */
    @Override
    public List<Job> getJobsByOwnerIdAndTaskId(long userId, long taskId) {
        //Criterion crit = Restrictions.eq("flow.taskId", taskId);
        //return this.findByCriteriaDistinct(crit);
        Session session=getSession();
        String hql="from Job where ownerId=:userId AND flow.taskId= :taskId";
        Query query=session.createQuery(hql);
        query.setLong("userId", userId).setLong("taskId", taskId);
        return query.list();

    }

    /**
     * @see JobDao#getJobsByOwnerIdAndKeyword(long, String)
     */
    @Override
    public List<Job> getJobsByOwnerIdAndKeyword(long userId, String keyword) {
//        Criterion crit1 = Restrictions.like("name", "%" + keyword + "%");
//        Criterion crit2 = Restrictions.like("description", "%" + keyword + "%");
//        Criterion crit3 = Restrictions.like("flow.name", "%" + keyword + "%");
//        Criterion crit4 = Restrictions.like("flow.description", "%" + keyword + "%");
//        Criterion crit5 = Restrictions.like("flow.typeName", "%" + keyword + "%");
//        Criterion crit6 = Restrictions.like("flow.keyWords", "%" + keyword + "%");
//        Criterion crit  = Restrictions.or
//                (Restrictions.or(Restrictions.or(crit1,crit2),Restrictions.or(crit3, crit4)),
//                 Restrictions.or(crit5,crit6));
//        return this.findByCriteriaDistinct(crit);
        Session session=getSession();
        String hql="from Job where (ownerId=:userId) AND (name LIKE '%:keyword%' OR description LIKE '%:keyword%' "
                +" OR flow.name LIKE '%:keyword%' OR flow.description LIKE '%:keyword%'"
                +"OR flow.typeName LIKE '%:keyword%' OR flow.keyword LIKE '%:keyword%')";
        Query query=session.createQuery(hql);
        query.setLong("userId", userId).setString("keyword", keyword);
        return query.list();
    }
}
