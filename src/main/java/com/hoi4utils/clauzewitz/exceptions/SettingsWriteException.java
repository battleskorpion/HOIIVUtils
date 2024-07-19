package main.java.com.hoi4utils.clauzewitz.exceptions;

import java.io.IOException;

public class SettingsWriteException extends RuntimeException {
	public SettingsWriteException(String message, IOException e) {
		super(message, e);
	}
}
