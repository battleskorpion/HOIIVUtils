package com.HOIIVUtils.hoi4utils;

import java.io.File;

public interface FileUtils {
	File usersHome = new File(System.getProperty("user.home"));
	File usersDocuments = new File(usersHome + File.separator + "Documents");

	/**
	 * @param data
	 * @return
	 */
	static boolean usefulData(String data) {
		if (data.isEmpty()) {
			return false;
		}

		return data.trim().charAt(0) != '#';
	}
}
