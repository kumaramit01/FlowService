package org.imirsel.nema.dao;

import java.io.Serializable;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

/**
 * An interface shared by all data access objects.
 * <p>
 * All CRUD (create, read, update, delete) basic data access operations are
 * isolated in this interface and shared across all DAO implementations. The
 * current design is for a state-management oriented persistence layer (for
 * example, there is no UDPATE statement function) that provides automatic
 * transactional dirty checking of business objects in persistent state.
 * </p>

 * @author Christian Bauer
 * @author shirk
 * @since 0.4.0
 */
public interface GenericDao<T, ID extends Serializable> {

   /**
    * Return the instance with the specified ID.
    * 
    * @param id The ID of the object to return.
    * @param lock Whether or not to obtain a pessimistic lock.
    * @return The instance with the specified ID.
    */
   T findById(ID id, boolean lock);

   /**
    * Find all instances of this type in the database.
    * 
    * @return All instances of this type in the database.
    */
   List<T> findAll();

   /**
    * Find all instances of this type in the database where the fields match
    * the supplied example.
    * 
    * @param exampleInstance Instance of this type with fields set to reflect
    * search parameters.
    * @param excludeProperty Properties to exclude from the underlying fetch.
    * @return List of instances that match the specified search criteria.
    */
   List<T> findByExample(T exampleInstance, String... excludeProperty);

   /**
    * Persist the specified entity instance. Saves or updates the entity as
    * necessary.
    * 
    * @param entity Entity to persist.
    * @return The entity in the post-persisted state. The state may change if
    * the database perform some triggers upon update, or if the DAO performs
    * changes on the properties before saving the entity. 
    */
   T makePersistent(T entity);

   /**
    * Deletes the specified entity from the underlying datastore.
    * 
    * @param entity The entity to delete.
    */
   void makeTransient(T entity);

   /**
    * Flushes the underlying session. Flushing is the process of 
    * synchronizing the underlying persistent store with persistable 
    * state held in memory. Affects every managed instance in the 
    * current persistence context!
    */
   void flush();

   /**
    * Completely clear the session. Evict all loaded instances and cancel 
    * all pending saves, updates and deletions. Do not close open iterators 
    * or instances of ScrollableResults. Affects every managed instance 
    * in the current persistence context!
    */
   void clear();

   /**
    * Set the {@link SessionFactory} to use to generate {@link Session}
    * instances.
    * 
    * @param sessionFactory The {@link SesionFactory} to use to generate
    * session instances.
    */
   void setSessionFactory(SessionFactory sessionFactory);

   /**
    * Return the {@link SessionFactory} that the DAO is currently using to
    * generate {@link Session} instances.
    * 
    * @return The {@link SessionFactory} currently being used.
    */
   SessionFactory getSessionFactory();

   /**
    * Start a managed session using the specified {@link Session}. This tells
    * the DAO that rather than participating in a automatic transactional
    * context, you the developer will be managing the transaction and the 
    * {@link Session}. This method allows you to pass multiple DAOs
    * participating in the same transaction the same {@link Session}.
    * 
    * @param session The {@link Session} to use to for the managed session
    * context.
    */
   void startManagedSession(Session session);

   /**
    * Switch back to an automatic transactional context.
    */
   void endManagedSession();

   
}
