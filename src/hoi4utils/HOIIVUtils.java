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
			System.out.println("HOIIVUtils created a stage settings and lauched it cuz it was first time setup");
			settingsController = new SettingsController();
			settingsController.launchSettingsWindow(args);
		} else {
			HOIIVFile.createHOIIVFilePaths();
			
			if (Settings.SKIP_SETTINGS.enabled()) {
				System.out.println("HOIIVUtils created a stage menu and lauched it cuz settings was set to be skipped");
				menuController = new MenuController();
				menuController.launchMenuWindow(args);
			} else {
				System.out.println("HOIIVUtils created a stage settings and lauched it cuz it was NOT first time and settings was NOT skipped");
				settingsController = new SettingsController();
				settingsController.launchSettingsWindow(args);
			}
		}
	}
}
