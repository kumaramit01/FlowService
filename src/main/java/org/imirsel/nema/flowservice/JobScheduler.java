package org.imirsel.nema.flowservice;

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
    * Halt the processing of a currently executing job.
    *
    * @param job The job to halt execution for.
    */
   public void abortJob(Job job);
   
}
