package hoi4utils;

import ui.main_menu.SettingsWindow;
import ui.main_menu.MenuWindow;
import hoi4utils.FirstTime;

import java.io.IOException;

/*
* HOIIVUtils File
*/
public class HOIIVUtils {

	public static String[] args;
	public static final String hoi4utilsVersion = "Version 0.4.1";
	public static boolean firstTimeSetup;
	public static SettingsWindow settingsWindow;
	public static MenuWindow menuWindow;
	public static SettingsManager settings;
	public static void main(String[] args) throws RuntimeException,IOException {
		try {
			SettingsManager.getSavedSettings();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
		if (Boolean.TRUE.equals(FirstTime.getIsFirstTime()) || firstTimeSetup) {
			settingsWindow = new SettingsWindow();
			settingsWindow.launchSettingsWindow(args);
		} else {
			HOIIVFile.createHOIIVFilePaths();
			if (Settings.SKIP_SETTINGS.enabled()) {
				menuWindow = new MenuWindow();
				menuWindow.launchMenuWindow(args);
			} else {
				settingsWindow = new SettingsWindow();
				settingsWindow.launchSettingsWindow(args);
			}
		}
	}
}
