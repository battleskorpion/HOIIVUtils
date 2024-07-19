package main.java.com.hoi4utils.clauzewitz.exceptions;

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
