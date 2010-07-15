package org.imirsel.nema.flowservice;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;

import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.imirsel.meandre.client.ExecResponse;
import org.imirsel.nema.dao.DaoFactory;
import org.imirsel.nema.dao.JobDao;
import org.imirsel.nema.flowservice.config.MeandreServerProxyConfig;
import org.imirsel.nema.flowservice.config.MeandreServerProxyStatus;
import org.imirsel.nema.model.Job;
import org.imirsel.nema.model.Job.JobStatus;
import org.springframework.dao.DataAccessException;

/**
 * Receives {@link Job} execution requests and distributes them to a set of
 * Meandre servers.
 * 
 * @author shirk
 * @since 0.4.0
 */
@ThreadSafe
public class MeandreJobScheduler implements JobScheduler {

   private static final int QUEUE_POLL_PERIOD = 5;
   private static final int DECOMMISSIONER_POLL_PERIOD = 30;
   
   private static final Logger logger = Logger
         .getLogger(MeandreJobScheduler.class.getName());

   private static final int MAX_EXECUTION_TRIES = 5;

   //~ Instance fields ---------------------------------------------------------

   /** The configuration for this job scheduler. */
   @GuardedBy("workersLock")
   private Set<MeandreServerProxyConfig> workerConfigs;

   /** Used to get references to Meandre server proxies. */
   private MeandreServerProxyFactory serverFactory;
   
   /** All jobs are placed on this queue as they come into the scheduler. */
   @GuardedBy("queueLock")
   private final Queue<Job> jobQueue = new LinkedList<Job>();

   /** Decides which server gets the next job. */
   @GuardedBy("workersLock")
   private MeandreLoadBalancer loadBalancer;

   /** Lock for the job queue. */
   private final Lock queueLock = new ReentrantLock();

   /** Meandre servers for processing jobs. */
   @GuardedBy("workersLock")
   private Set<MeandreServerProxy> workers = new HashSet<MeandreServerProxy>();

   /** 
    * Workers that have been removed as a result of a configuration change,
    * but are still processing jobs.
    */
   @GuardedBy("workersLock")
   private Set<MeandreServerProxy> removedWorkers = 
      new HashSet<MeandreServerProxy>();
   
   private DaoFactory daoFactory;

   /**
    * Lock for both the set of workers and the load balancer which contains
    * references to the workers.
    */
   private final Lock workersLock = new ReentrantLock();


   //~ Constructors ------------------------------------------------------------

   /**
    * Creates a new instance.
    */
   public MeandreJobScheduler() {
   }

   //~ Methods -----------------------------------------------------------------

   /**
    * Initializes this instance.
    */
   @PostConstruct
   public void init() {
      assert workerConfigs != null : "No worker configs were " +
      		"provided to the job scheduler.";
      for(MeandreServerProxyConfig workerConfig : workerConfigs) {
         MeandreServerProxy server;
         try {
            server = serverFactory.getServerProxyInstance(workerConfig,false);
         } catch (MeandreServerException e) {
            e.printStackTrace();
            logger.warning("Could not instantiate server " + 
                  workerConfig.getHost() + ":" + workerConfig.getPort() + 
                  ". Skipping...");
            continue;
         }
         workers.add(server);
         loadBalancer.addServer(server);
      }
      
      ScheduledExecutorService executor = Executors
            .newSingleThreadScheduledExecutor();

      executor.scheduleWithFixedDelay(new RunQueuedJobs(), 15,
          QUEUE_POLL_PERIOD, TimeUnit.SECONDS);

      executor.scheduleWithFixedDelay(new DecommissionServers(), 15,
          DECOMMISSIONER_POLL_PERIOD, TimeUnit.SECONDS);
   }

   /**
    * @see org.imirsel.nema.flowservice.JobScheduler#abortJob(org.imirsel.nema.model.Job)
    */
   public void abortJob(Job job) throws IllegalStateException,
         MeandreServerException {
      queueLock.lock();

      JobDao jobDao = daoFactory.getJobDao();

      try {
         if (jobQueue.contains(job)) {
            jobQueue.remove(job);

            job.setJobStatus(Job.JobStatus.ABORTED);
            jobDao.makePersistent(job);

         } else {
            MeandreServerProxy executingServer = findExecutingServer(job);
            if (executingServer == null) {
               throw new IllegalStateException("Job " + job.getId() + " is "
                     + "transitioning states. It is neither queued nor is it "
                     + "executing on a server. Try aborting again later.");
            }

            // The Meandre probe will change the job state once the job has
            // actually aborted. So, we won't do it manually here.
            executingServer.abortJob(job);

         }
      } finally {
         queueLock.unlock();
      }
   }

   /**
    * Find the server that is executing the given {@link Job}.
    * 
    * @param job The {@link Job} to find the executing server instance for.
    * @return The {@link MeandreServerProxy} that is currently executing the
    *         job, or <code>null</code> if no known server is executing the job.
    */
   private MeandreServerProxy findExecutingServer(Job job) {
      workersLock.lock();
      try {
         for (MeandreServerProxy server : workers) {
            if (job.getHost().equals(
                  server.getConfig().getHost())
                  && job.getPort().equals(
                        server.getConfig().getPort())) {
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
    * @see org.imirsel.nema.flowservice.JobScheduler#getScheduledJobs()
    */
   public List<Job> getScheduledJobs() {
      queueLock.lock();
      try {
         List<Job> scheduledJobs = new ArrayList<Job>();
         scheduledJobs.addAll(jobQueue);
         return scheduledJobs;
      } finally {
         queueLock.unlock();
      }
   }
   
   /**
    * Attempt to run any queued jobs.
    */
   public void runJobs() {
      Session session = null;
      JobDao jobDao = daoFactory.getJobDao();
      try {
         queueLock.lock();
         workersLock.lock();
         if (jobQueue.size() < 1) {
            logger.fine("No queued jobs.");
            return;
         }
         
         session = jobDao.getSessionFactory().openSession();
         jobDao.startManagedSession(session);
         
         while (!jobQueue.isEmpty()) {
            logger.fine("Found " + jobQueue.size() + " queued jobs.");
            MeandreServerProxy server = loadBalancer.nextAvailableServer();
            if (server == null) {
               logger.info("All servers are busy. Will try again in "
                     + QUEUE_POLL_PERIOD + " seconds.");
               return;
            }
            logger.fine("Server " + server + " is available for processing.");

            Job job = jobQueue.peek();

            job.incrementNumTries();
            job.setJobStatus(JobStatus.SUBMITTED);
            job.setSubmitTimestamp(new Date());
        
            logger.info("Preparing to update job " + job.getId()
                  + " as submitted.");

            Transaction transaction = session.beginTransaction();
            transaction.begin();
            try {
               jobDao.makePersistent(job);
               transaction.commit();
               logger.info("Job " + job.getId() + " updated.");
            } catch (HibernateException e) {
               logger.warning("Data access exception: " + e.getMessage());
               rollback(transaction);
               return;
            } catch (DataAccessException e) {
               logger.warning("Data access exception: " + e.getMessage());
               rollback(transaction);
               return;
            } catch (Exception e) {
               logger.warning(e.getMessage());
               rollback(transaction);
               return;
            }

            try {
               logger.info("Attempting to contact server " + server
                     + " to execute job.");

               ExecResponse response = server.executeJob(job);
               logger.info("Execution response received.");
               
               // If the executeJob() method above succeeded, the Meandre
               // server will have (most likely) changed the job status to
               // "started". If the status changes, the NEMA probe running
               // on the Meandre server will have written the new status to
               // the same database the flow service uses. Therefore, we
               // want to refresh the state of the job here to pick up the
               // status change. Otherwise, the old status (submitted) will
               // be rewritten to the database, and the "started" state
               // will be lost.
               session.refresh(job);
               
               logger.info("Attempting to record job execution response.");
               job.setHost(server.getConfig().getHost());
               job.setPort(server.getConfig().getPort());
               job.setExecPort(response.getPort());
               job.setExecutionInstanceId(response.getUri());
               
               transaction = session.beginTransaction();
               transaction.begin();
               try {
                  jobDao.makePersistent(job);
                  transaction.commit();
                  logger.fine("Job execution response recorded in database.");
                  jobQueue.remove();
               } catch (HibernateException e) {
                  logger.warning("Data access exception: " + e.getMessage());
                  rollback(transaction);
                  return;
               } catch (DataAccessException e) {
                  logger.warning("Data access exception: " + e.getMessage());
                  rollback(transaction);
                  return;
               } catch (Exception e) {
                  logger.warning(e.getMessage());
                  rollback(transaction);
                  return;
               }
            } catch (MeandreServerException serverException) {
               logger.warning(serverException.getMessage());
               job.setSubmitTimestamp(null);
               job.setJobStatus(JobStatus.SCHEDULED);

               if (job.getNumTries() == MAX_EXECUTION_TRIES) {
                  logger.info("Unsuccessfully tried " + MAX_EXECUTION_TRIES
                        + " times to execute job " + job.getId()
                        + ". Will mark job as failed.");
                  job.setJobStatus(JobStatus.FAILED);
                  job.setEndTimestamp(new Date());
                  job.setUpdateTimestamp(new Date());
                  jobQueue.remove();
               }

               transaction = session.beginTransaction();
               transaction.begin();
               try {
                  jobDao.makePersistent(job);
                  transaction.commit();
               } catch (HibernateException e) {
                  logger.warning("Data access exception: " + e.getMessage());
                  rollback(transaction);
                  return;
               } catch (DataAccessException e) {
                  logger.warning("Data access exception: " + e.getMessage());
                  rollback(transaction);
                  return;
               } catch (Exception e) {
                  logger.warning(e.getMessage());
                  rollback(transaction);
                  return;
               }
            }
         }
      } catch (Exception e) {
         e.printStackTrace();
      } finally {
         workersLock.unlock();
         queueLock.unlock();
         if (session != null) {
            try {
               jobDao.endManagedSession();
               session.close();
            } catch (HibernateException e) {
               logger.warning(e.getMessage());
            }
         }
      }
   }

   /**
    * Rollback the specified transaction.
    * 
    * @param transaction The transaction to rollback.
    */
   private void rollback(Transaction transaction) {
      try {
         logger.fine("Rolling back.");
         transaction.rollback();
      } catch (HibernateException e2) {
         logger.warning(e2.getMessage());
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
    * @see JobScheduler#getWorkerStatus()
    */
   public Map<MeandreServerProxyConfig,MeandreServerProxyStatus> getWorkerStatus() {
      Map<MeandreServerProxyConfig,MeandreServerProxyStatus> statusMap =
         new HashMap<MeandreServerProxyConfig,MeandreServerProxyStatus>();
      workersLock.lock();
      try {
         for (MeandreServerProxy server : workers) {
            statusMap.put(server.getConfig(), server.getStatus());
         }
      } finally {
         workersLock.unlock();
      }
      return statusMap;
   }
   
   /**
    * Return the {@link MeandreLoadBalancer} currently in use.
    * 
    * @return The load balancer currently in use.
    */
   public MeandreLoadBalancer getLoadBalancer() {
      return loadBalancer;
   }

   /**
    * Set the {@link MeandreLoadBalancer} to use.
    * 
    * @param loadBalancer The {@link MeandreLoadBalancer} to use.
    */
   public void setLoadBalancer(MeandreLoadBalancer loadBalancer) {
      this.loadBalancer = loadBalancer;
   }

   /**
    * @see MeandreJobScheduler#getWorkerConfigs()
    */
   public Set<MeandreServerProxyConfig> getWorkerConfigs() {
      return workerConfigs;
   }

   /**
    * @see MeandreJobScheduler#setWorkerConfigs(Set)
    */
   public void setWorkerConfigs(Set<MeandreServerProxyConfig> newWorkerConfigs) {
      workersLock.lock();
      try {
         if (workerConfigs == null) {
            workerConfigs = newWorkerConfigs;
         } else { // Handle the updating of configs.
            
            logger.info("Received new worker configuration.");
            
            Set<MeandreServerProxyConfig> added = 
               new HashSet<MeandreServerProxyConfig>();
            Set<MeandreServerProxyConfig> removed =
               new HashSet<MeandreServerProxyConfig>();
            Set<MeandreServerProxyConfig> changed =
               new HashSet<MeandreServerProxyConfig>();
            Set<MeandreServerProxyConfig> unchanged =
               new HashSet<MeandreServerProxyConfig>();
            
            // Find servers that have been added
            for (MeandreServerProxyConfig config : newWorkerConfigs) {
               if (!workerConfigs.contains(config)) {
                  added.add(config);
               }
            }

            // Find servers that have been removed
            for (MeandreServerProxyConfig config : workerConfigs) {
               if (!newWorkerConfigs.contains(config)) {
                  removed.add(config);
               }
            }
            
            // Determine which servers have had their configurations changed, and
            // which remain the same. (e.g. username, password, maxConcurrentJobs)
            for (MeandreServerProxyConfig existingConfig : workerConfigs) {
               if (newWorkerConfigs.contains(existingConfig)) {
                  MeandreServerProxyConfig newConfig = null;
                  // Find the new config
                  Iterator<MeandreServerProxyConfig> configIterator = 
                     newWorkerConfigs.iterator();
                  while(configIterator.hasNext()) {
                     newConfig = configIterator.next();
                     if(newConfig.equals(existingConfig)) {
                        break;
                     }
                  }
                  // New config is found, now compare properties
                  if(existingConfig.getUsername().equals(newConfig.getUsername()) &&
                     existingConfig.getPassword().equals(newConfig.getPassword()) &&
                     existingConfig.getMaxConcurrentJobs()==
                        newConfig.getMaxConcurrentJobs()) {
                     // Nothing has changed...add to unchanged
                     unchanged.add(newConfig);
                  } else {
                     // Something has changed...add to changed
                     changed.add(newConfig);
                  }
               }
            }
            
            if(added.size()==0 && removed.size()==0 && changed.size()==0) {
               logger.info("No changes found in worker configuration.");   
            }
            
            if(added.size()>0) {
               String serverStr = added.size()==1?"server was":"servers were";
               logger.info(added.size() + " " + 
                     serverStr + " added.");
            }
            
            if(removed.size()>0) {
               String serverStr = removed.size()==1?"server was":"servers were";
               logger.info(removed.size() + " " + serverStr + 
                     " removed.");
            }
            
            if(changed.size()>0) {
               String serverStr = changed.size()==1?"server was":"servers were";
               logger.info(changed.size() + " " + serverStr + 
                     " changed.");
            }
            
            if((added.size()!=0 || removed.size()!=0 || changed.size()!=0) 
                  && unchanged.size()>0) {
               String serverStr = unchanged.size()==1?"server was":"servers were";
               logger.info(unchanged.size() + " " + serverStr + 
                     " left unchanged.");
            }
            
            // Remove servers
            for (MeandreServerProxyConfig remove : removed) {
               MeandreServerProxy proxyToRemove = null;
               try {
                  proxyToRemove = serverFactory
                        .getServerProxyInstance(remove,false);
               } catch (MeandreServerException e) {
                  // this should never happen
               }
               proxyToRemove.stopAcceptingJobs();
               // If the server is still processing jobs
               if (!proxyToRemove.isIdle()) {
                  removedWorkers.add(proxyToRemove);
               } else {
                  serverFactory.release(proxyToRemove);
               }
               workers.remove(proxyToRemove);
               loadBalancer.removeServer(proxyToRemove);
            }
            
            // Add servers
            for (MeandreServerProxyConfig add : added) {
               MeandreServerProxy proxyToAdd = null;
               try {
                  proxyToAdd = serverFactory
                        .getServerProxyInstance(add,false);
               } catch (MeandreServerException e) {
                  e.printStackTrace();
                  logger.warning("Could not instantiate server " + 
                        add.getHost() + ":" + add.getPort() + 
                        ". Skipping...");
                  continue;
               }
               // If the server was removed while jobs were still processing,
               // but has now been added back.
               if(removedWorkers.contains(proxyToAdd)) {
                  proxyToAdd.syncJobsWithServer();
                  removedWorkers.remove(proxyToAdd);
               }
               workers.add(proxyToAdd);
               loadBalancer.addServer(proxyToAdd);
               proxyToAdd.startAcceptingJobs();
            }

            // Sync changed servers
            for (MeandreServerProxyConfig change : changed) {
               // Find the server
               Iterator<MeandreServerProxy> proxyIterator = 
                  workers.iterator();
               while(proxyIterator.hasNext()) {
                  MeandreServerProxy proxyToChange = proxyIterator.next();
                  if(proxyToChange.getConfig().equals(change)) {
                     proxyToChange.setConfig(change);
                     break;
                  }
               }
            }
            
            workerConfigs = newWorkerConfigs;
         }
      } finally {
         workersLock.unlock();
      }
   }

   /**
    * Set the {@link DaoFactory} to use.
    * 
    * @param daoFactory The {@link DaoFactory} implementation to use.
    */
   public void setDaoFactory(DaoFactory daoFactory) {
      this.daoFactory = daoFactory;
   }

   /**
    * Return the {@link DaoFactory} implementation currently in use.
    * 
    * @return {@link DaoFactory} implementation currently in use.
    */
   public DaoFactory getDaoFactory() {
      return daoFactory;
   }

   /**
    * Return the {@link MeandreServerProxyFactory} currently in use.
    * 
    * @return {@link MeandreServerProxyFactory} currently in use.
    */
   public MeandreServerProxyFactory getMeandreServerProxyFactory() {
      return serverFactory;
   }

   /**
    * Set the {@link MeandreServerProxyFactory} to use.
    * 
    * @param daoFactory The {@link MeandreServerProxyFactory} implementation to use.
    */
   public void setMeandreServerProxyFactory(MeandreServerProxyFactory serverFactory) {
      this.serverFactory = serverFactory;
   }
   
   //~ Inner Classes -----------------------------------------------------------

   private class RunQueuedJobs implements Runnable {
      public void run() {
         logger.fine("Checking for queued jobs...");
         runJobs();
      }
   }
   
   private class DecommissionServers implements Runnable {
      public void run() {
         workersLock.lock();
         try {
            logger.finest("Decommissioning servers...");
            for (MeandreServerProxy proxy : removedWorkers) {
               if (proxy.isIdle()) {
                  removedWorkers.remove(proxy);
                  serverFactory.release(proxy);
               }
            }
         } finally {
            workersLock.unlock();
         }
      }
   }
}
