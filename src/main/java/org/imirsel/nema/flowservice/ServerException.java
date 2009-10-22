package org.imirsel.nema.flowservice;

/**
 * Signals that a problem occured while attempting to execute a job.
 * 
 * @author shirk
 */
public class ServerException extends Exception {

	/**
	 * Version of this class.
	 */
	private static final long serialVersionUID = 8688902997652020452L;

	public ServerException(String message) {
		super(message);
	}
	
	public ServerException(String message, Throwable cause) {
		super(message,cause);
	}
}
