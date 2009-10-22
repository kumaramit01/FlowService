package org.imirsel.nema.flowservice;

import static org.imirsel.nema.model.Job.JobStatus;

import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;

import org.imirsel.nema.dao.JobDao;
import org.imirsel.nema.flowservice.config.MeandreJobSchedulerConfig;
import org.imirsel.nema.model.Job;

import javax.annotation.PostConstruct;

import java.util.Date;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


/**
 * Receives {@link Job} execution requests and distributes them to a set of
 * Meandre servers.
 *
 * @author shirk
 * @since 1.0
 */
@ThreadSafe
public class MeandreJobScheduler implements JobScheduler {

   public static final int MAX_EXECUTION_TRIES = 5;
	
   //~ Instance fields ---------------------------------------------------------

   /** The configuration for this job scheduler. */
   private MeandreJobSchedulerConfig config;

   /** All jobs are placed on this queue as they come into the scheduler. */
   @GuardedBy("queueLock")
   private final Queue<Job> jobQueue = new LinkedList<Job>();

   /** Decides which server gets the next job. */
   @GuardedBy("workersLock")
   private MeandreLoadBalancer loadBalancer;

   /** Lock for the job queue. */
   private final Lock queueLock = new ReentrantLock();

   /** Periodically checks for jobs in the queue and runs them. */
   @SuppressWarnings("unused")
   private ScheduledFuture<?> runJobsFuture;

   /** Meandre servers for processing jobs. */
   @GuardedBy("workersLock")
   private Set<MeandreServer> workers;

   private JobDao jobDao;
   
   /**
    * Lock for both the set of workers and the load balancer which contains
    * references to the workers.
    */
   private final Lock workersLock = new ReentrantLock();

   //~ Instance initializers ---------------------------------------------------

   {
      ScheduledExecutorService executor = Executors
         .newSingleThreadScheduledExecutor();

      runJobsFuture = executor.scheduleAtFixedRate(
         new RunQueuedJobs(), 10, 5, TimeUnit.SECONDS);
   }

   //~ Constructors ------------------------------------------------------------

   /**
    * Creates a new instance.
    */
   public MeandreJobScheduler() {
   }

   /**
    * Creates a new instance given a configuration and a load balancer.
    *
    * @param config The configuration details for this job scheduler.
    * @param balancer The load balancer to use for distributing jobs.
    */
   public MeandreJobScheduler(
      MeandreJobSchedulerConfig config, MeandreLoadBalancer balancer) {
      loadBalancer = balancer;
      workers = config.getServers();
      for (MeandreServer server : workers) {
         loadBalancer.addServer(server);
      }
   }

   //~ Methods -----------------------------------------------------------------

   /**
    * Initializes this instance.
    */
   @PostConstruct
   public void init() {
	  assert config!=null:"No configuration was provided to the job scheduler.";
      workers = config.getServers();
      for (MeandreServer server : workers) {
         loadBalancer.addServer(server);
      }
   }

   /**
    * @see org.imirsel.nema.flowservice.JobScheduler#abortJob(org.imirsel.nema.model.Job)
    */
   public void abortJob(Job job) {
      MeandreServer executingServer = findExecutingServer(job);
      try {
		executingServer.abortJob(job);
	} catch (ServerException e) {
		e.printStackTrace();
	}
   }

   /**
    * Find the server that is executing the given {@link Job}.
    *
    * @param job The {@link Job} to find the executing server instance for.
    * @return The {@link MeandreServer} that is currently executing the job, or
    * <code>null</code> if no known server is executing the job.
    */
   private MeandreServer findExecutingServer(Job job) {
      workersLock.lock();
      try {
         for (MeandreServer server : workers) {
            if (
               job.getHost().equals(server.getHost()) &&
               job.getPort().equals(server.getPort())) {
               return server;
            }
         }
      } finally {
         workersLock.unlock();
      }
      return null;
   }

   /**
    * @see org.imirsel.nema.flowservice.JobScheduler#scheduleJob(org.imirsel.nema.model.Job)
    */
   public void scheduleJob(Job job) {
      queueLock.lock();
      try {
         jobQueue.offer(job);
      } finally {
         queueLock.unlock();
      }
   }

   /**
    * Attempt to run any queued jobs.
    */
   private void runJobs() {
      queueLock.lock();
      workersLock.lock();
      try {
         if (jobQueue.size() < 1) {
            System.out.println("> No queued jobs.");
            return;
         }
         while (!jobQueue.isEmpty()) {
            MeandreServer server = loadBalancer.nextAvailableServer();
            if (server == null) {
               System.out.println(
                  "> " + jobQueue.size() +
                  " jobs are queued but all servers are busy.");
               return;
            }

            Job job = jobQueue.peek();
            
            // MOVE TO CATCH BLOCK BELOW??
            if(job.getNumTries()==MAX_EXECUTION_TRIES) {
            	job.setJobStatus(JobStatus.FAILED);
            	jobDao.save(job);
            	jobQueue.remove();
            	continue;
            }
            
            job.incrementNumTries();
            job.setJobStatus(JobStatus.SUBMITTED);
            job.setSubmitTimestamp(new Date());
            jobDao.save(job);
            
            try {
               server.executeJob(job);
               
               job.setHost(server.getHost());
               job.setPort(server.getPort());
               
               jobDao.save(job);
               jobQueue.remove();
            } catch (ServerException e) {
               job.setSubmitTimestamp(null);
               job.setJobStatus(JobStatus.UNKNOWN);
               
               e.printStackTrace();
            }
            
         }
      } finally {
         queueLock.unlock();
         workersLock.unlock();
      }
   }

   /**
    * Add a server to the job scheduler.
    *
    * @param server Server to add.
    */
   public void addServer(MeandreServer server) {
      workersLock.lock();
      try {
         workers.add(server);
         loadBalancer.addServer(server);
      } finally {
         workersLock.unlock();
      }
   }

   /**
    * Remove a server from the job scheduler.
    *
    * @param server Server to remove.
    */
   public void removeServer(MeandreServer server) {
      workersLock.lock();
      try {
         workers.remove(server);
         loadBalancer.removeServer(server);
      } finally {
         workersLock.unlock();
      }
   }

   /**
    * Return the number of servers the scheduler is managing.
    *
    * @return Number of servers the scheduler is managing.
    */
   public int numServers() {
      workersLock.lock();
      try {
         return workers.size();
      } finally {
         workersLock.unlock();
      }
   }

   /**
    * Return the {@link MeandreLoadBalancer} currently in use.
    *
    * @return The load balancer currently in use.
    */
   public MeandreLoadBalancer getLoadBalancer() { return loadBalancer; }

   /**
    * Set the {@link MeandreLoadBalancer} to use.
    *
    * @param loadBalancer The {@link MeandreLoadBalancer} to use.
    */
   public void setLoadBalancer(MeandreLoadBalancer loadBalancer) {
      this.loadBalancer = loadBalancer;
   }

   /**
    * Return the {@link MeandreJobSchedulerConfig} currently in use.
    *
    * @return The {@link MeandreJobSchedulerConfig} currently in use.
    */
   public MeandreJobSchedulerConfig getMeandreJobSchedulerConfig() {
      return config;
   }

   /**
    * Set the {@link MeandreJobSchedulerConfig} to use.
    *
    * @param config The {@link MeandreJobSchedulerConfig} to use.
    */
   public void setMeandreJobSchedulerConfig(MeandreJobSchedulerConfig config) {
      this.config = config;
   }

   /**
    * Set the {@link JobDao} to use.
    * 
    * @param jobDao The {@link JobDao} implementation to use.
    */
   public void setJobDao(JobDao jobDao) {
	   this.jobDao = jobDao;
   }
   
   /**
    * Return the {@link JobDao} implementation currently in use.
    * 
    * @return {@link JobDao} implementation currently in use.
    */
   public JobDao getJobDao() {
	   return jobDao;
   }
   
   //~ Inner Classes -----------------------------------------------------------

   private class RunQueuedJobs implements Runnable {
      public void run() {
         System.out.println("> Checking for queued jobs.");
         runJobs();
      }
   }
}
