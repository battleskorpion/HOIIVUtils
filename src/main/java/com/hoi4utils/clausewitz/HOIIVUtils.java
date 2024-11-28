package com.hoi4utils.clausewitz;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import com.hoi4utils.Settings;
import com.hoi4utils.SettingsManager;
import com.hoi4utils.clausewitz.code.effect.EffectDatabase;
import com.hoi4utils.clausewitz.code.modifier.ModifierDatabase;
import com.hoi4utils.ui.menu.MenuController;
import com.hoi4utils.ui.settings.SettingsController;

/**
 * HOIIVUtils.java main method is here
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
	public static final String DARK_MODE_STYLESHEETURL = "com/hoi4utils/ui/javafx_dark.css";
	public static SettingsController settingsController;
	public static MenuController menuController;

	/**
	 * Main method for HOIIVUtils.
	 * <p> 
	 * If firstTimeSetup is true, then launch the settings window. If firstTimeSetup is false, then
	 * create the HOIIVUtils directory and launch the settings window if SKIP_SETTINGS is false,
	 * otherwise launch the menu window.
	 *
     */
	public static void main(String[] args) {
		System.out.println(HOIIVUTILS_NAME + " " + HOIIVUTILS_VERSION + " launched");

		SettingsManager.convertOldPropertiesFile();

		// Start log
		// TODO Redo all of logging to fit with new save location
		// TODO make all the logs go to a console window that can be opened or closed
		// (including stack traces)
		// HOIIVUtilsLog.startLog();

		// Load modifiers and effects
		/* preprocessing which doesn't require settings */
		// TODO Fix module or pom.xml to compile the code with the database (.db) files
		@SuppressWarnings("unused")
		ModifierDatabase mdb = new ModifierDatabase(); // load modifiers
		@SuppressWarnings("unused")
		EffectDatabase edb = new EffectDatabase(); // load effects

		// Check if this is the first time the program is run
		if (Boolean.TRUE.equals(!new File(SettingsManager.NEW_PROPERTIES_PATH).exists())) {
			System.out.println("HOIIVUtils launched the settings window");
			settingsController = new SettingsController();
			settingsController.launchSettingsWindow(args);
		} else {
			HOIIVFile.createHOIIVFilePaths();
			if (Settings.SKIP_SETTINGS.enabled()) {
				System.out.println("HOIIVUtils launched the menu window");
				menuController = new MenuController();
				menuController.launchMenuWindow(args);
			} else {
				System.out.println("HOIIVUtils launched the settings window and properties files exists");
				settingsController = new SettingsController();
				settingsController.launchSettingsWindow(args);
			}
		}
	}

}
