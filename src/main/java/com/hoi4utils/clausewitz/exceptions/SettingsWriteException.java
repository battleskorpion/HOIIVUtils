package com.hoi4utils.clausewitz.exceptions;

import java.io.IOException;

public class SettingsWriteException extends RuntimeException {
	public SettingsWriteException(String message, IOException e) {
		super(message, e);
	}
}
