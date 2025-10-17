package com.hoi4utils.file.file_listener;

import java.util.EventListener;
/*
 * FileListener File
 */
public interface FileListener extends EventListener {

	void onCreated(FileEvent event);

	void onModified(FileEvent event);

	void onDeleted(FileEvent event);
		
}
