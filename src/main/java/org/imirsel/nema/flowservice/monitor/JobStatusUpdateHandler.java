package org.imirsel.nema.flowservice.monitor;

import org.imirsel.nema.model.Job;

/**
 * Interface for objects that want to be notified of {@link Job} status updates.
 * 
 * @author shirk
 * @since 0.4.0
 */
public interface JobStatusUpdateHandler {

	/**
	 * Receive a status update and handle it accordingly. The implementation
	 * of this method should not block or perform time consuming tasks.
	 * 
	 * @param job The {@link Job} whose status was just updated.
	 */
	public void jobStatusUpdate(Job job);
}
