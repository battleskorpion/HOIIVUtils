package hoi4utils;

import java.io.File;

public class FirstTime {
	private static Boolean isFirstTime = true;
	static Boolean configExist = new File(HOIIVUtilsProp.configFilePath).exists();

	private FirstTime() {
		
	}
	/**
	 * this is to make sure that when the user deletes his settings it is not perma stuck on false
	 */
	public static Boolean getIsFirstTime() {
		if (Boolean.TRUE.equals(configExist)) {
			isFirstTime = false;
		} else {
			isFirstTime = true;
		}
		return isFirstTime;
	}
}
