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
 * TODO: Description of class {@link PollingJobStatusMonitor}.
 *
 * @author shirk
 * @since 1.0
 */
@ThreadSafe
public class PollingJobStatusMonitor implements JobStatusMonitor {
   private static final Logger logger = 
		Logger.getLogger(PollingJobStatusMonitor.class.getName());
	
   //~ Instance fields ---------------------------------------------------------

   /** TODO: Description of field {@link PollingJobStatusMonitor#jobDao}. */
   private JobDao jobDao;

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
    * TODO: Creates a new {@link $class.name$} object.
    *
    * @param jobDao TODO: Description of parameter jobDao.
    */
   public PollingJobStatusMonitor() {  }

   //~ Methods -----------------------------------------------------------------

   /**
    * TODO: Description of method {@link $class.name$#monitor}.
    *
    * @param job TODO: Description of parameter job.
    * @param updateHandler TODO: Description of parameter updateHandler.
    */
   public void monitor(Job job, JobStatusUpdateHandler updateHandler) {
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
    * TODO: Description of method {@link $class.name$#remove}.
    *
    * @param job TODO: Description of parameter job.
    */
   public void remove(Job job) {
      jobsLock.lock();
      try {
         jobs.remove(job);
      } finally {
         jobsLock.unlock();
      }
   }

   //~ Inner Classes -----------------------------------------------------------

   private class StatusUpdateDetector implements Runnable {
      public void run() {
         logger.fine("> Checking for jobs status updates.");
         jobsLock.lock();
         try {
        	Iterator<Job> jobIterator = jobs.keySet().iterator();
            while (jobIterator.hasNext()) {
               Job cachedJob = jobIterator.next();
               Job persistedJob = jobDao.get(cachedJob.getId());
               Integer oldStatus = cachedJob.getStatusCode();
               Integer newStatus = persistedJob.getStatusCode();
               if (!oldStatus.equals(newStatus)) {
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
                	  jobIterator.remove();
                  }
               }
            }
         } finally {
            jobsLock.unlock();
         }
      }
   }

public JobDao getJobDao() {
	return jobDao;
}

public void setJobDao(JobDao jobDao) {
	this.jobDao = jobDao;
}


   
   
}
