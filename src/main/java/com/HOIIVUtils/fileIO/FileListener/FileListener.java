package com.HOIIVUtils.fileIO.FileListener;

import java.util.EventListener;
/*
 * FileListener File
 */
public interface FileListener extends EventListener {

	public void onCreated(FileEvent event);

	public void onModified(FileEvent event);

	public void onDeleted(FileEvent event);
		
}
