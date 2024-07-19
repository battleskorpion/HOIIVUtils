package main.java.com.hoi4utils.clauzewitz.exceptions;

import java.io.File;
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

    public IllegalLocalizationFileTypeException(File file) {
		super("Illegal localization file type, must be .yml. File: ");
    }
}
