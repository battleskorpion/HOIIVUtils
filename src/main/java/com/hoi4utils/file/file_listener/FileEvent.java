package com.hoi4utils.file.file_listener;

import java.io.File;
import java.util.EventObject;
/*
 * FileEvent File
 */
public class FileEvent extends EventObject {
	public FileEvent(File file) {
		super(file);
	}

	public File getFile() {
		return (File) getSource();
	}
}
