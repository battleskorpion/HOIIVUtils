package com.HOIIVUtils.clauzewitz.exceptions;

import java.io.IOException;

public class SettingsFileException extends RuntimeException {
	public SettingsFileException(String message, IOException e) {
		super(message, e);
	}
}
