package org.imirsel.nema;

public class NoSuchEntityException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public NoSuchEntityException(String message) {
		super(message);
	}
}
