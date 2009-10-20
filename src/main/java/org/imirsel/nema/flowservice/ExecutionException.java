package org.imirsel.nema.flowservice;

/**
 * Signals that a problem occured while attempting to execute a job.
 * 
 * @author shirk
 */
public class ExecutionException extends Exception {

	/**
	 * Version of this class.
	 */
	private static final long serialVersionUID = 8688902997652020452L;

	public ExecutionException(String message) {
		super(message);
	}
	
	public ExecutionException(String message, Throwable cause) {
		super(message,cause);
	}
}
