package com.HOIIVUtils.hoi4utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

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
	public static final String VERSION;
	static {
		Properties properties = new Properties();
		try (InputStream inputStream = HOIIVUtils.class.getResourceAsStream("/HOIIVUtils.properties")) {
			properties.load(inputStream);
			VERSION = properties.getProperty("version");
		} catch (IOException e) {
			throw new ExceptionInInitializerError(e);
		}
	}
	public static final String HOIIVUTILS_VERSION = "Version " + VERSION;
	public static final String DARK_MODE_STYLESHEETURL = "/com/HOIIVUtils/ui/javafx_dark.css";
	public static Boolean firstTimeSetup;
	public static SettingsController settingsController;
	public static MenuController menuController;

	public static void main(String[] args) {
		System.out.println(HOIIVUTILS_NAME + " " + HOIIVUTILS_VERSION + " launched");
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
