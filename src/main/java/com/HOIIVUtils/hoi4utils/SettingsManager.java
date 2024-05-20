package com.HOIIVUtils.hoi4utils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Scanner;
import java.util.stream.Stream;

import com.HOIIVUtils.hoi4utils.ioexceptions.SettingsFileException;

/**
 * The SettingsManager class is responsible for managing the HOIIVUtils
 * properties file.
 * It provides methods to read and write settings from/to the file.
 *
 * @author battleskorpion
 */
public class SettingsManager {

	public static final String USER_DOCS_PATH = System.getProperty("user.home") + File.separator + "Documents";
	public static final String HOI4UTILS_PROPERTIES_PATH = USER_DOCS_PATH + File.separator + "HOIIVUtils";
	static {
		System.err.println(HOI4UTILS_PROPERTIES_PATH);
	}

	private static File settingsFile;
	private static FileWriter settingsWriter;
	private static BufferedWriter settingsBWriter;
	private static PrintWriter settingsPWriter;// = new PrintWriter(settingsBWriter); // for println syntax
	static HashMap<Settings, String> settingValues = new HashMap<>();
	public static SettingsManager settings;

	// These constructors are the most cancer of time.
	SettingsManager() throws IOException {
		new File(HOI4UTILS_PROPERTIES_PATH).mkdir();
		settingsFile = new File(HOI4UTILS_PROPERTIES_PATH + File.separator + "HOIIVUtils_properties.txt");

		settingsFile.createNewFile();

		readSettings();
	}

	public SettingsManager(HashMap<Settings, String> settings) throws IOException {
		String userDocsPath = System.getProperty("user.home") + File.separator + "Documents";
		String hoi4UtilsPropertiesPath = userDocsPath + File.separator + "HOIIVUtils";

		new File(hoi4UtilsPropertiesPath).mkdir();
		settingsFile = new File(hoi4UtilsPropertiesPath + File.separator + "HOIIVUtils_properties.txt");

		boolean newSettingsFileCreated = settingsFile.createNewFile();

		if (settings == null) {
			writeBlankSettings();
		} else if (newSettingsFileCreated) {
			writeBlankSettings();
			saveSettings(settings);
		} else {
			readSettings();
			saveSettings(settings);
		}
	}

	/**
	 * Reads hoi4utils.settings from hoi4utils.settings file
	 */
	public static void readSettings() {
		try {
			Scanner settingReader = new Scanner(settingsFile);

			/*
			 * if file is empty then write blank hoi4utils.settings to new
			 * hoi4utils.settings file
			 */
			if (!settingReader.hasNext()) {
				writeBlankSettings();
				settingReader.close();
				return;
			}

			/* read hoi4utils.settings */
			while (settingReader.hasNextLine()) {
				String[] readSetting = settingReader.nextLine().split(";");
				Settings setting = Settings.valueOf(readSetting[0]);

				settingValues.put(setting, readSetting[1]);
			}

			for (Settings setting : Settings.values()) {
				if (!settingValues.containsKey(setting)) {
					writeBlankSetting(setting);
				}
			}
			settingReader.close();

		} catch (IOException e) {
			throw new SettingsFileException("Error reading settings", e);
		}

	}

	/**
	 * Writes a blank setting to the hoi4utils.settings file
	 */
	private static void writeBlankSetting(Settings setting) throws IOException {
		settingsWriter = new FileWriter(settingsFile, true); // true = append
		settingsBWriter = new BufferedWriter(settingsWriter);
		settingsPWriter = new PrintWriter(settingsBWriter);

		settingsPWriter.println(setting.name() + ";" + setting.defaultProperty());

		settingsPWriter.close();
	}

	/**
	 * Writes default hoi4utils.settings to the hoi4utils.settings file
	 */
	public static void writeBlankSettings() throws IOException {
		settingsWriter = new FileWriter(settingsFile, false); // true = append
		settingsBWriter = new BufferedWriter(settingsWriter);
		settingsPWriter = new PrintWriter(settingsBWriter);

		for (Settings setting : Settings.values()) {
			settingsPWriter.println(setting.name() + ";" + setting.defaultProperty());

			settingValues.put(setting, setting.defaultProperty());
		}

		settingsPWriter.close();

	}

	/**
	 * Saves setting with specified value
	 */
	public static void saveSetting(Settings setting, String settingValue) throws IOException {
		settingsWriter = new FileWriter(settingsFile, false); // true = append
		settingsBWriter = new BufferedWriter(settingsWriter);
		settingsPWriter = new PrintWriter(settingsBWriter);

		settingValues.put(setting, settingValue);
		for (Settings s : Settings.values()) {
			settingsPWriter.println(s.name() + ";" + settingValues.get(s));
		}

		settingsPWriter.close();
	}

	/**
	 * Saves a list of hoi4utils.settings to hoi4utils.settings file, all other
	 * hoi4utils.settings remain the same.
	 * 
	 * @param newSettings list of updated hoi4utils.settings to save
	 * @throws IOException
	 */
	public static void saveSettings(HashMap<Settings, String> newSettings) throws IOException {
		if (newSettings == null) {
			return;
		}

		settingsWriter = new FileWriter(settingsFile, false); // true = append
		settingsBWriter = new BufferedWriter(settingsWriter);
		settingsPWriter = new PrintWriter(settingsBWriter);

		settingValues.putAll(newSettings);
		for (Settings s : Settings.values()) {
			settingsPWriter.println(s.name() + ";" + settingValues.get(s));
		}

		settingsPWriter.close();
	}

	/**
	 * Saves all hoi4utils.settings to hoi4utils.settings file.
	 * 
	 * @throws IOException
	 */
	public static void saveSettings() throws IOException {
		settingsWriter = new FileWriter(settingsFile, false); // true = append
		settingsBWriter = new BufferedWriter(settingsWriter);
		settingsPWriter = new PrintWriter(settingsBWriter);

		for (Settings s : Settings.values()) {
			settingsPWriter.println(s.name() + ";" + settingValues.get(s));
		}

		settingsPWriter.close();
	}

	public static void getSavedSettings() {
		if (new File(HOI4UTILS_PROPERTIES_PATH + "\\HOIIVUtils_properties.txt").exists()) {
			HOIIVUtils.firstTimeSetup = false;

			try {
				SettingsManager.settings = new SettingsManager();
			} catch (IOException e) {
				e.printStackTrace();
			}

			System.out.println("Performing standard startup cuz settings were found");
		} else {
			HOIIVUtils.firstTimeSetup = true;
		}
	}

	/**
	 * Deletes all hoi4utils.settings from the hoi4utils.settings directory.
	 * 
	 * @throws IOException
	 */
	public static void deleteAllSettings() throws IOException {
		// Get the path to the hoi4utils.settings directory
		Path dir = Paths.get(HOI4UTILS_PROPERTIES_PATH);

		// Use try-with-resources to delete the files and directories
		try (Stream<Path> paths = Files.walk(dir)) {
			// Sort the paths in reverse order so that the
			// directories are deleted first
			paths.sorted(Comparator.reverseOrder())
					.forEach(path -> {
						try {
							// If DEV_MODE is enabled, print the path to the file or
							// directory that is being deleted
							if (Settings.DEV_MODE.enabled()) {
								System.out.println("Deleting: " + path);
							}
							// Delete the file or directory
							Files.delete(path);
						} catch (IOException e) {
							// Print the exception if something goes wrong
							e.printStackTrace();
						}
					});
		}
	}

	public static String get(Settings setting) {
		return settingValues.get(setting);
	}

	public static boolean isNull(Settings setting) {
		return settingValues.get(setting).equals("null");
	}
}
