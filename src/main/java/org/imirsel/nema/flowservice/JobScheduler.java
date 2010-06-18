package org.imirsel.nema.flowservice;

import java.util.List;
import java.util.Map;

import org.imirsel.nema.flowservice.config.MeandreServerProxyConfig;
import org.imirsel.nema.flowservice.config.MeandreServerProxyStatus;
import org.imirsel.nema.model.Job;


/**
 * Receives {@link Job} execution requests and distributes them to computational 
 * resources.
 *
 * @author shirk
 * @since 0.4.0
 */
public interface JobScheduler {

   //~ Methods -----------------------------------------------------------------

   /**
    * Receive a job execution request.
    *
    * @param job The job execution request.
    */
   public void scheduleJob(Job job);

   /**
    * Request that the processing of a currently executing job is halted.
    *
    * @param job The job to halt execution for.
    */
   public void abortJob(Job job);
   
   /**
    * Return the jobs that are scheduled.
    * 
    * @return Jobs that are scheduled for execution.
    */
   public List<Job> getScheduledJobs();
   
   /**
    * Return the execution state of all worker servers. The key in the returned
    * map uniquely identifies the server and the value is the server's current
    * state.
    * 
    * @return Mapping from a {@link MeandreServerProxyConfig} to its current
    * {@link MeandreServerProxyStatus}.
    */
   public Map<MeandreServerProxyConfig,MeandreServerProxyStatus> getWorkerStatus();
   
}
