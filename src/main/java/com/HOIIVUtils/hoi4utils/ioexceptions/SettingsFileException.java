package com.HOIIVUtils.hoi4utils.ioexceptions;

import java.io.IOException;

public class SettingsFileException extends RuntimeException {
	public SettingsFileException(String message, IOException e) {
		super(message, e);
	}
}
