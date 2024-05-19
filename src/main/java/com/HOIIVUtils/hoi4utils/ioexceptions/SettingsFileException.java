package com.HOIIVUtils.hoi4utils.ioexceptions;

import java.io.IOException;

public class SettingsFileException extends RuntimeException {
	public SettingsFileException(IOException e) {
		super(e);
	}
}
