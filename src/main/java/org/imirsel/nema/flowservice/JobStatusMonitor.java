package org.imirsel.nema.flowservice;

import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;

import org.imirsel.nema.dao.JobDao;
import org.imirsel.nema.model.Job;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


/**
 * TODO: Description of class {@link JobStatusMonitor}.
 *
 * @author shirk
 * @since 1.0
 */
@ThreadSafe
public class JobStatusMonitor {

   //~ Instance fields ---------------------------------------------------------

   /** TODO: Description of field {@link JobStatusMonitor#jobDao}. */
   private final JobDao jobDao;

   /** TODO: Description of field {@link JobStatusMonitor#jobs}. */
   @GuardedBy("jobsLock")
   private final Map<Job, JobStatusUpdateHandler> jobs =
      new HashMap<Job, JobStatusUpdateHandler>();

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
         new UpdateDetector(), 10, 5, TimeUnit.SECONDS);
   }

   //~ Constructors ------------------------------------------------------------

   /**
    * TODO: Creates a new {@link $class.name$} object.
    *
    * @param jobDao TODO: Description of parameter jobDao.
    */
   public JobStatusMonitor(JobDao jobDao) { this.jobDao = jobDao; }

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
         jobs.put(job, updateHandler);
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

   private class UpdateDetector implements Runnable {
      public void run() {
         System.out.println("> Checking for jobs status updates.");
         jobsLock.lock();
         try {
            for (Job job : jobs.keySet()) {
               Job dbJob = jobDao.get(job.getId());
               Integer oldStatus = job.getStatusCode();
               Integer newStatus = dbJob.getStatusCode();
               if (!oldStatus.equals(newStatus)) {
                  job.setStatusCode(dbJob.getStatusCode());
                  jobs.get(job)
                  .jobStatusUpdate(job);
               }
            }
         } finally {
            jobsLock.unlock();
         }
      }
   }
}
