package org.imirsel.nema.flowservice;

import org.imirsel.nema.model.Job;

public interface JobStatusUpdateHandler {

	public void jobStatusUpdate(Job job);
}
