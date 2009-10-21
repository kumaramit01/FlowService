package org.imirsel.nema.flowservice.monitor;

import org.imirsel.nema.model.Job;

public interface JobStatusUpdateHandler {

	// this should not  block
	public void jobStatusUpdate(Job job);
}
