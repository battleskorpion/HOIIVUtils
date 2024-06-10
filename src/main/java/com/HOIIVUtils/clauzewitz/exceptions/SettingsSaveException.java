package com.HOIIVUtils.clauzewitz.exceptions;

import java.io.IOException;

public class SettingsSaveException extends Exception {
	public SettingsSaveException(String message, IOException e) {
		super(message, e);
	}
}
