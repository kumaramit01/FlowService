package org.imirsel.nema.dao.impl;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.LockMode;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Example;
import org.imirsel.nema.dao.GenericDao;
import org.springframework.dao.DataAccessException;
import org.springframework.orm.hibernate3.SessionFactoryUtils;


/**
 * Implements the generic CRUD data access operations using Hibernate APIs.
 *
 * <p>To write a DAO, subclass and parameterize this class with your persistent
 * class. Of course, assuming that you have a traditional 1:1 approach for
 * Entity:DAO design.</p>
 *
 *
 * @author Christian Bauer
 * @author shirk
 * @since 0.4.0
 */
abstract public class GenericDaoImpl<T, ID extends Serializable>
      implements GenericDao<T, ID> {

   private final Class<T> persistentClass;

   private SessionFactory sessionFactory;
   
   private Session managedSession;

   //~ Constructors ------------------------------------------------------------

   /**
    * Create a new instance.
    */
   @SuppressWarnings("unchecked")
   public GenericDaoImpl() {
      this.persistentClass =
         (Class<T>)
         ((ParameterizedType) getClass().getGenericSuperclass())
         .getActualTypeArguments()[0];
   }

   //~ Methods -----------------------------------------------------------------

   /**
    * @see GenericDao#setSessionFactory(SessionFactory)
    */
   public void setSessionFactory(SessionFactory s) { this.sessionFactory = s; }
   
   /**
    * @see GenericDao#getSessionFactory()
    */
   public SessionFactory getSessionFactory() { return sessionFactory;}

   /**
    * Return the {@link Session} currently in use.
    */
   protected Session getSession() { 
	   if(managedSession!=null){
		   return managedSession;
	   }  else {
		   return sessionFactory.getCurrentSession();
	   }
   }

   /**
    * Return the class type that this DAO manages.
    * 
    * @return Class whose instances are managed by this DAO.
    */
   public Class<T> getPersistentClass() { return persistentClass; }

   /**
    * @see GenericDao#findById(Serializable, boolean)
    */
   @SuppressWarnings("unchecked")
   public T findById(ID id, boolean lock) throws DataAccessException {
      T entity;

      try {
         if (lock) {
            entity =
               (T) getSession().load(
                  getPersistentClass(), id, LockMode.UPGRADE);
         } else {
            entity = (T) getSession().load(getPersistentClass(), id);
         }
      } catch (HibernateException e) {
         throw SessionFactoryUtils.convertHibernateAccessException(e);
      }

      return entity;
   }

   /**
    * @see GenericDao#findAll()
    */
   public List<T> findAll() throws DataAccessException {
      return findByCriteria();
   }

   /**
    * @see GenericDao#findByExample(Object, String...)
    */
   @SuppressWarnings("unchecked")
   public List<T> findByExample(T exampleInstance, String... excludeProperty)
         throws DataAccessException {
      try {
         Criteria crit = getSession().createCriteria(getPersistentClass());
         Example example = Example.create(exampleInstance);
         for (String exclude : excludeProperty) {
            example.excludeProperty(exclude);
         }
         crit.add(example);
         return crit.list();
      } catch (HibernateException e) {
         throw SessionFactoryUtils.convertHibernateAccessException(e);
      }
   }

   /**
    * @see GenericDao#makePersistent(Object)
    */
   public T makePersistent(T entity) throws DataAccessException {
      try {
         getSession().saveOrUpdate(entity);
      } catch (HibernateException e) {
         throw SessionFactoryUtils.convertHibernateAccessException(e);
      }
      return entity;
   }

   /**
    * @see GenericDao#makeTransient(Object)
    */
   public void makeTransient(T entity) throws DataAccessException {
      getSession().delete(entity);
   }

   /**
    * @see GenericDao#flush()
    */
   public void flush() { getSession().flush(); }

   /**
    * @see GenericDao#clear()
    */
   public void clear() { getSession().clear(); }

   /**
    * Finds entities based on the given search criteria. Duplicates may exist
    * in the resulting list.
    *
    * @param criterion Search criteria.
    * @return Search results given the specified criteria
    * @throws DataAccessException if a problem occurs while performing the
    * search.
    */
   @SuppressWarnings("unchecked")
   protected List<T> findByCriteria(Criterion... criterion)
         throws DataAccessException {
      try {
         Criteria crit = getSession().createCriteria(getPersistentClass());
         for (Criterion c : criterion) {
            crit.add(c);
         }
         return crit.list();
      } catch (HibernateException e) {
         throw SessionFactoryUtils.convertHibernateAccessException(e);
      }
   }
   
   /**
    * Finds a distinct set of results given the specified criteria.
    *
    * @param criterion Search criteria.
    * @return Search results given the specified criteria
    * @throws DataAccessException if a problem occurs while performing the
    * search.
    */
   @SuppressWarnings("unchecked")
   protected List<T> findByCriteriaDistinct(Criterion... criterion)
         throws DataAccessException {
      try {
         Criteria crit = getSession().createCriteria(getPersistentClass());
         for (Criterion c : criterion) {
            crit.add(c);
         }
         crit.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
         return crit.list();
      } catch (HibernateException e) {
         throw SessionFactoryUtils.convertHibernateAccessException(e);
      }
   }
   
   /**
    * @see GenericDao#startManagedSession(Session)
    */
   public void startManagedSession(Session session) {
	   this.managedSession = session;
   }
   
   /**
    * @see GenericDao#endManagedSession()
    */
   public void endManagedSession() {
	   managedSession=null;
   }
}
