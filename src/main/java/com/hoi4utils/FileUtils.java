package main.java.com.hoi4utils;

import java.io.File;

public interface FileUtils {
	File usersHome = new File(System.getProperty("user.home"));
	File usersDocuments = new File(usersHome + File.separator + "Documents");
	File ProgramFilesX86 = System.getenv("ProgramFiles(x86)") != null
			? new File(System.getenv("ProgramFiles(x86)")) : null;
	String steamHOI4LocalPath = "Steam" + File.separator + "steamapps" + File.separator + "common" + File.separator + "Hearts of Iron IV";

	/**
	 * @param data
	 * @return
	 */
	static boolean usefulData(String data) {
		if (data.isBlank()) {
			return false;
		}

		return data.trim().charAt(0) != '#';
	}
}
