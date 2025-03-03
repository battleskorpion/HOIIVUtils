package com.hoi4utils.clausewitz.exceptions;

public class NotPermittedInScopeException extends Exception {
	public NotPermittedInScopeException() {
		super();
	}

	public NotPermittedInScopeException(String message) {
		super(message);
	}

	public NotPermittedInScopeException(String message, Throwable cause) {
		super(message, cause);
	}

	public NotPermittedInScopeException(Throwable cause) {
		super(cause);
	}
}
