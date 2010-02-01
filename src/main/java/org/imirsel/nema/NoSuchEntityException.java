package org.imirsel.nema;

/**
 * Signals that a request has been made for an entity that does not exist in
 * the data store.
 * 
 * @author shirk
 * @since 0.4.0
 */
public class NoSuchEntityException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	/**
	 * Creates a new instance with the specified message.
	 * 
	 * @param message Message for the exception.
	 */
	public NoSuchEntityException(String message) {
		super(message);
	}
}
