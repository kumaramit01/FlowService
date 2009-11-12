package org.imirsel.nema.flowservice.monitor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.imirsel.nema.dao.DaoFactory;
import org.imirsel.nema.dao.JobDao;
import org.imirsel.nema.model.Job;
import org.springframework.dao.DataAccessException;


/**
 * A job monitor that polls the database for status updates.
 *
 * @author shirk
 * @since 1.0
 */
@ThreadSafe
public class PollingJobStatusMonitor implements JobStatusMonitor {
	
   private static final Logger logger = 
		Logger.getLogger(PollingJobStatusMonitor.class.getName());
	
   //~ Instance fields ---------------------------------------------------------

   /** Used to access jobs in the data store. */
   private DaoFactory daoFactory;

   /**
    * Maps jobs that are being monitored to objects that are waiting to be
    * notified of status changes.
    */
   @GuardedBy("jobsLock")
   private final Map<Job, Set<JobStatusUpdateHandler>> jobs =
      new HashMap<Job, Set<JobStatusUpdateHandler>>();

   /** Concurrency lock for the jobs list. */
   private final Lock jobsLock = new ReentrantLock();

   /** Periodically checks for jobs status changes. */
   @SuppressWarnings("unused")
   private ScheduledFuture<?> updateDetectorFuture;

   //~ Instance initializers ---------------------------------------------------

   {
      ScheduledExecutorService executor = Executors
         .newSingleThreadScheduledExecutor();

      updateDetectorFuture = executor.scheduleAtFixedRate(
         new StatusUpdateDetector(), 10, 10, TimeUnit.SECONDS);
   }

   //~ Constructors ------------------------------------------------------------

   /**
    * Create a new instance.
    */
   public PollingJobStatusMonitor() {  }

   //~ Methods -----------------------------------------------------------------

   /**
    * @see JobStatusMonitor#start(Job, JobStatusUpdateHandler)
    */
   public void start(Job job, JobStatusUpdateHandler updateHandler) {
	  logger.fine("Starting to monitor job " + job.getId() + " for " + 
			  updateHandler + ".");
      jobsLock.lock();
      try {
    	 if(jobs.containsKey(job)) {
             jobs.get(job).add(updateHandler);
             logger.fine("Adding a handler for job " + job.getId() + " to an existing handler set.");
    	 } else {
    		 Set<JobStatusUpdateHandler> handlerSet = 
    			 new HashSet<JobStatusUpdateHandler>();
    		 handlerSet.add(updateHandler);
    		 logger.fine("Created a handler set for job " + job.getId() + ".");
    		 jobs.put(job,handlerSet);
    	 }
      } finally {
         jobsLock.unlock();
      }
   }

   /**
   * @see JobStatusMonitor#stop(Job, JobStatusUpdateHandler)
    */
   public void stop(Job job, JobStatusUpdateHandler updateHandler) {
	  logger.fine("Stopping the monitoring of job " + job.getId() + " for " + 
				  updateHandler + ".");
      jobsLock.lock();
      try {
    	 Set<JobStatusUpdateHandler> handlers = jobs.get(job);
    	 if(handlers!=null) {
    		 handlers.remove(updateHandler);
    		 if(handlers.isEmpty()) {
    			 jobs.remove(job);
    		 }
    	 }
      } finally {
         jobsLock.unlock();
      }
   }

   //~ Inner Classes -----------------------------------------------------------

   /**
    * Runs each time the monitor wakes up to get updated job statuses.
    */
   private class StatusUpdateDetector implements Runnable {
      public void run() {
         if (jobs.size() == 0) {
            return;
         }
         logger.fine(
            "Checking for job status updates for " + jobs.size() + " jobs.");
         jobsLock.lock();

         JobDao jobDao = null;
         try {

            Iterator<Job> jobIterator = jobs.keySet()
               .iterator();

            jobDao = daoFactory.getJobDao();

            Session session = jobDao.getSessionFactory()
               .openSession();
            jobDao.startManagedSession(session);

            while (jobIterator.hasNext()) {
               Job cachedJob = jobIterator.next();
               Job persistedJob = null;
               try {
                  persistedJob = jobDao.findById(cachedJob.getId(), false);
               } catch (HibernateException e) {
                  logger.warning("Data access exception: " + e.getMessage());
               } catch (DataAccessException e) {
                  logger.warning("Data access exception: " + e.getMessage());
               } catch (Exception e) {
                  logger.warning(e.getMessage());
               }

               if (persistedJob == null) {
                  return;
               }

               Integer oldStatus = cachedJob.getStatusCode();
               Integer newStatus = persistedJob.getStatusCode();
               if (!oldStatus.equals(newStatus)) {
                  try {
                     logger.fine(
                        "Status update for job " + cachedJob.getId() +
                        " occurred: was " + cachedJob.getJobStatus() +
                        ", now " + persistedJob.getJobStatus() + ".");
                     cachedJob.setStatusCode(persistedJob.getStatusCode());
                     cachedJob.setUpdateTimestamp(
                        persistedJob.getUpdateTimestamp());
                     cachedJob.setSubmitTimestamp(
                        persistedJob.getStartTimestamp());
                     cachedJob.setStartTimestamp(
                        persistedJob.getStartTimestamp());
                     cachedJob.setEndTimestamp(persistedJob.getEndTimestamp());

                     // Invoke the update handlers for this job
                     Set<JobStatusUpdateHandler> handlers = jobs.get(cachedJob);
                     if (handlers == null) {
                        logger.fine(
                           "No handlers registered for job " +
                           cachedJob.getId() + ".");
                        return;
                     } else {
                        for (JobStatusUpdateHandler handler : handlers) {
                           logger.fine(
                              "Dispatching a job status update for job " +
                              cachedJob.getId() + " to handler " + handler +
                              ".");
                           handler.jobStatusUpdate(cachedJob);
                        }
                     }

                     // Stop monitoring this job if it is finished
                     if (!cachedJob.isRunning()) {
                        logger.fine(
                           "Job " + cachedJob.getId() +
                           " has ended. Removing it from the status monitor.");
                        jobIterator.remove();
                     }
                  } catch (Exception e) {
                     e.printStackTrace();
                  } // end try-catch
               } // end if
            } // end while
         } finally {
            jobsLock.unlock();
            jobDao.endManagedSession();
         } // end try-finally
      } // end method run
   }

   /**
    * Return the {@link DaoFactory} currently in use.
    * @return The {@link DaoFactory} currently in use.
    */
public DaoFactory getDaoFactory() {
	return daoFactory;
}

/**
 * Set the {@link DaoFactory} to use.
 * @param jobDao The {@link DaoFactory} to use.
 */
public void setDaoFactory(DaoFactory daoFactory) {
	this.daoFactory = daoFactory;
}


   
   
}
