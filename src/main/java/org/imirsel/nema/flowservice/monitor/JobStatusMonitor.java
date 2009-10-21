package org.imirsel.nema.flowservice.monitor;

import org.imirsel.nema.model.Job;

public interface JobStatusMonitor {

	   public void monitor(Job job, JobStatusUpdateHandler updateHandler);

	   public void remove(Job job);
}
