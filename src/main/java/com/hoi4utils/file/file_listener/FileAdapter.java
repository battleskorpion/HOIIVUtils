package com.hoi4utils.file.file_listener;
/*
 * FileListner File
 */
public abstract class FileAdapter implements FileListener {
	@Override
	public void onCreated(FileEvent event) {
		// default
	}

	@Override
	public void onModified(FileEvent event) {
		// default
	}
		
	@Override
	public void onDeleted(FileEvent event) {
		// default
	}
		
}