package com.HOIIVUtils.hoi4utils;

import com.HOIIVUtils.hoi4utils.clausewitz_code.effect.EffectDatabase;
import com.HOIIVUtils.hoi4utils.clausewitz_code.modifier.ModifierDatabase;
import com.HOIIVUtils.hoi4utils.clausewitz_data.gfx.Interface;
import com.HOIIVUtils.ui.menu.MenuController;
import com.HOIIVUtils.ui.settings.SettingsController;

/**
 * HOIIVUtils.java
 * main method is here
 */
public class HOIIVUtils {

	public static final String HOIIVUTILS_NAME = "HOIIVUtils";
	public static final String HOIIVUTILS_VERSION = "Version 9.0.0";
	public static final String DARK_MODE_STYLESHEETURL = "/com/HOIIVUtils/ui/javafx_dark.css";
	public static Boolean firstTimeSetup;
	public static SettingsController settingsController;
	public static MenuController menuController;

	public static void main(String[] args) {
		SettingsManager.getSavedSettings();

		HOIIVUtilsLog.startLog();

		/* preprocessing which doesn't require settings */
		ModifierDatabase mdb = new ModifierDatabase(); // load modifiers
		EffectDatabase edb = new EffectDatabase(); // load effects

		if (Boolean.TRUE.equals(firstTimeSetup)) {
			System.out.println("HOIIVUtils launched stage settings cuz it was first time setup");
			settingsController = new SettingsController();
			settingsController.launchSettingsWindow(args);
		} else {
			HOIIVFile.createHOIIVFilePaths();

			if (Settings.SKIP_SETTINGS.enabled()) {
				System.out.println("HOIIVUtils launched stage menu cuz settings was set to be skipped");
				menuController = new MenuController();
				menuController.launchMenuWindow(args);
			} else {
				System.out.println(
						"HOIIVUtils created launched settings cuz it was NOT first time and settings was NOT skipped");
				settingsController = new SettingsController();
				settingsController.launchSettingsWindow(args);
			}
		}
	}
}
