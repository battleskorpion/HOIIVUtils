package hoi4utils;

import ui.menu.MenuController;
import ui.settings.SettingsController;

/*
* HOIIVUtils File
*/
public class HOIIVUtils {

	public static final String HOIIVUTILS_NAME = "HOIIVUtils";
	public static final String HOIIVUTILS_VERSION = "Version 0.4.1";
	public static final String DARK_MODE_STYLESHEETURL = "resources/javafx_dark.css";
	public static Boolean firstTimeSetup;
	public static SettingsController settingsController;
	public static MenuController menuController;
	public static void main(String[] args) {
		SettingsManager.getSavedSettings();
		
		if (firstTimeSetup) {
			settingsController = new SettingsController();
			settingsController.launchSettingsWindow(args);
		} else {
			HOIIVFile.createHOIIVFilePaths();
			
			if (Settings.SKIP_SETTINGS.enabled()) {
				menuController = new MenuController();
				menuController.launchMenuWindow(args);
			} else {
				settingsController = new SettingsController();
				settingsController.launchSettingsWindow(args);
			}
		}
	}
}
