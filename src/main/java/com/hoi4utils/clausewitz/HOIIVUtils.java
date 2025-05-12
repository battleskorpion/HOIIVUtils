package com.hoi4utils.clausewitz;

import com.hoi4utils.ui.menu.MenuController;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.hoi4utils.Updater;

import javax.swing.*;
import java.io.File;

/**
 * HOIIVUtils.java main method is here
 * <p>
 * HOIIVUTILS Directory Layout:
 * <p>
 * HOIIVUtils\\target\\HOIIVUtils.jar
 * <p>
 * HOIIVUtils\\demo_mod\\*
 * <p>
 * HOIIVUtils\\HOIIVUtils.bat
 * <p>
 * HOIIVUtils\\HOIIVUtils.properties
 * <p>
 * HOIIVUtils\\HOIIVUtils.sh
 */
public class HOIIVUtils {
	public static final Logger LOGGER = LogManager.getLogger(HOIIVUtils.class);

	// Static references to application configuration
	public static File HOIIVUTILS_DIR;
	public static String HOIIVUTILS_VERSION;
	public static MenuController menuController;
	private static HOIIVUtilsConfig config;

	public static void main(String[] args) {
		Updater upr = new Updater();
		try {
			// Initialize application using the new initializer
			HOIIVUtilsInitializer initializer = new HOIIVUtilsInitializer();
			config = initializer.initialize();

			// Set static references for backwards compatibility
			HOIIVUTILS_DIR = config.getHoi4UtilsDir();
			HOIIVUTILS_VERSION = config.getVersion();

			if (upr.updateCheck(HOIIVUTILS_VERSION)) {
				// show pop up
				int response = JOptionPane.showConfirmDialog(
						null,
						"Do you want to update to the latest version?",
						"Update Available",
						JOptionPane.YES_NO_OPTION,
						JOptionPane.QUESTION_MESSAGE
				);
				if (response == JOptionPane.YES_OPTION) {
					// run the updater
					upr.update(HOIIVUTILS_DIR);
					// run the updater updater
				}
			}
			
			initializer.loadMod();
			
			LOGGER.info("HOIIVUtils {} launched successfully", HOIIVUTILS_VERSION);
			menuController = new MenuController();
			menuController.launchMenuWindow(args);
		} catch (Exception e) {
			LOGGER.fatal("Failed to initialize HOIIVUtils", e);
			JOptionPane.showMessageDialog(null,
				"Failed to initialize HOIIVUtils: " + e.getMessage(),
				"Critical Error", JOptionPane.ERROR_MESSAGE);
			System.exit(1);
		}
	}

	/**
	 * Gets a property from the configuration.
	 *
	 * @param key Property key
	 * @return Property value or null if not found
	 */
	public static String get(String key) {
		if (config == null) {
			LOGGER.debug("Configuration not initialized!");
			return "";
		}
		var property = config.getProperty(key);
		if (property != null) return property;
		else return ""; 
	}

	/**
	 * Sets a property in the configuration.
	 *
	 * @param key Property key
	 * @param value Property value
	 */
	public static void set(String key, String value) {
		config.setProperty(key, value);
	}

	/**
	 * Load mod data from configured paths.
	 */
	public static void loadMod() {
		// Delegate to HOIIVModLoader to reload mod data
		new HOIIVModLoader(config).loadMod();
	}

	/**
	 * Save the current configuration to disk.
	 */
	public static void save() {
		// Delegate to HOIIVConfigManager to save configuration
		new HOIIVConfigManager(config).saveConfiguration();
	}
}