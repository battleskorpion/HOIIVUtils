package com.HOIIVUtils.hoi4utils.exceptions;

import java.io.IOException;

public class IllegalLocalizationFileTypeException extends RuntimeException {
	public IllegalLocalizationFileTypeException(String message, IOException e) {
		super(message, e);
	}

	public IllegalLocalizationFileTypeException(String message, IllegalLocalizationFileTypeException e) {
		super(message, e);
	}

	public IllegalLocalizationFileTypeException(String message) {
		super(message);
	}
}
