package com.HOIIVUtils.hoi4utils.clausewitz_code.effect;

public class InvalidEffectParameterException extends Exception {
	public InvalidEffectParameterException() {
		super();
	}

	public InvalidEffectParameterException(String message) {
		super(message);
	}

	public InvalidEffectParameterException(String message, Throwable cause) {
		super(message, cause);
	}

	public InvalidEffectParameterException(Throwable cause) {
		super(cause);
	}
}
