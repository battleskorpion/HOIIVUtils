package com.HOIIVUtils.hoi4utils.ioexceptions;

import java.io.IOException;

public class SettingsSaveException extends Exception {
	public SettingsSaveException(String message, IOException e) {
		super(message, e);
	}
}
