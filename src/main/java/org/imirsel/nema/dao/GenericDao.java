package org.imirsel.nema.dao;

import java.io.Serializable;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

/**
 * An interface shared by all business data access objects.
 * <p>
 * All CRUD (create, read, update, delete) basic data access operations are
 * isolated in this interface and shared across all DAO implementations. The
 * current design is for a state-management oriented persistence layer (for
 * example, there is no UDPATE statement function) that provides automatic
 * transactional dirty checking of business objects in persistent state.
 * 
 * @author Christian Bauer
 * @since 0.4.0
 * @param <T>
 *           Type to be maintained by the DAO.
 * @param <ID>
 *           The identity type.
 */
public interface GenericDao<T, ID extends Serializable> {

   /**
    * Return the instance with the specified ID.
    * @param id The ID of the object to return.
    * @param lock Whether or not to obtain a pessimistic lock.
    * @return The instance with the specified ID.
    */
   T findById(ID id, boolean lock);

   /**
    * Find all instances of this type in the data store.
    * 
    * @return All instances.
    */
   List<T> findAll();

   List<T> findByExample(T exampleInstance, String... excludeProperty);

   T makePersistent(T entity);

   void makeTransient(T entity);

   /**
    * Affects every managed instance in the current persistence context!
    */
   void flush();

   /**
    * Affects every managed instance in the current persistence context!
    */
   void clear();

   void setSessionFactory(SessionFactory sessionFactory);

   SessionFactory getSessionFactory();

   void startManagedSession(Session session);

   void endManagedSession();

}
