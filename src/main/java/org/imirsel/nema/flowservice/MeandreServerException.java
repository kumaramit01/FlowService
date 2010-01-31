package org.imirsel.nema.flowservice;


/**
 * Signals that a problem occurred while the server was performing some
 * operation.
 * 
 * @author shirk
 * @since 1.0
 */
public class MeandreServerException extends Exception {

	/**
	 * Version of this class.
	 */
	private static final long serialVersionUID = 8688902997652020452L;

	/**
	 * Create a new instance with the specified message.
	 * 
	 * @param message Message describing what went wrong.
	 */
	public MeandreServerException(String message) {
		super(message);
	}
	
	/**
	 * Create a new instance with the specified message and cause.
	 * 
	 * @param message Message describing what went wrong.
	 * @param cause Underlying cause of this exception.
	 */
	public MeandreServerException(String message, Throwable cause) {
		super(message,cause);
	}

	/**
	 * Create a new instance with the wrapped exception
	 * 
	 * @param exception wrap an external exception
	 */
	public MeandreServerException(Exception exception) {
		super(exception);
	}
}
