package com.HOIIVUtils.hoi4utils.ioexceptions;

public class NullParameterTypeException extends Exception {
	public NullParameterTypeException() {
		super();
	}

	public NullParameterTypeException(String message) {
		super(message);
	}

	public NullParameterTypeException(String message, Throwable cause) {
		super(message, cause);
	}

	public NullParameterTypeException(Throwable cause) {
		super(cause);
	}
}
