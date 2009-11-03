package org.imirsel.nema.flowservice.monitor;

import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;

import org.imirsel.nema.dao.JobDao;
import org.imirsel.nema.model.Job;

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
   private JobDao jobDao;

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
    * Create a new instance with the specified {@link JobDao}.
    *
    * @param jobDao The {@link JobDao} to use to access job info.
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
    	 } else {
    		 Set<JobStatusUpdateHandler> handlerSet = 
    			 new HashSet<JobStatusUpdateHandler>();
    		 handlerSet.add(updateHandler);
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
         logger.fine("> Checking for job status updates.");
         jobsLock.lock();
         try {
        	Iterator<Job> jobIterator = jobs.keySet().iterator();
            while (jobIterator.hasNext()) {
               Job cachedJob = jobIterator.next();
               Job persistedJob = jobDao.findById(cachedJob.getId(),false);
               Integer oldStatus = cachedJob.getStatusCode();
               Integer newStatus = persistedJob.getStatusCode();
               if (!oldStatus.equals(newStatus)) {
            	  logger.fine("Status update for job " + cachedJob.getId() + 
            			  "occurred: was " + cachedJob.getJobStatus() + 
            			  ", now " + persistedJob.getJobStatus() + ".");
                  cachedJob.setStatusCode(persistedJob.getStatusCode());
                  cachedJob.setUpdateTimestamp(persistedJob.getUpdateTimestamp());
                  cachedJob.setSubmitTimestamp(persistedJob.getStartTimestamp());
                  cachedJob.setStartTimestamp(persistedJob.getStartTimestamp());
                  cachedJob.setEndTimestamp(persistedJob.getEndTimestamp());
                  // Invoke the update handlers for this job
                  Set<JobStatusUpdateHandler> handlers = jobs.get(cachedJob);
                  for(JobStatusUpdateHandler handler:handlers) {
                	  handler.jobStatusUpdate(cachedJob);
                  }
                  // Stop monitoring this job if it is finished
                  if(!cachedJob.isRunning()) {
                	  logger.fine("Job " + cachedJob.getId() + 
                			  " has ended. Removing it from the status monitor.");
                	  jobIterator.remove();
                  }
               }
            }
         } finally {
            jobsLock.unlock();
         }
      }
   }

   /**
    * Return the {@link JobDao} currently in use.
    * @return The {@link JobDao} currently in use.
    */
public JobDao getJobDao() {
	return jobDao;
}

/**
 * Set the {@link JobDao} to use.
 * @param jobDao The {@link JobDao} to use.
 */
public void setJobDao(JobDao jobDao) {
	this.jobDao = jobDao;
}


   
   
}
