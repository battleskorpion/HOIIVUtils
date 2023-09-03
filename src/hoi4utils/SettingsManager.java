package hoi4utils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Scanner;
/*
 * HOI 4 Util Properties File
 */
public class SettingsManager {

	public static final String USER_DOCS_PATH = System.getProperty("user.home") + File.separator + "Documents";
	public static final String HOI4UTILS_PROPERTIES_PATH = USER_DOCS_PATH + File.separator + "HOIIVUtils";

	private static File settings_file;
	private static FileWriter settingsWriter;
	private static BufferedWriter settingsBWriter;
	private static PrintWriter settingsPWriter;// = new PrintWriter(settingsBWriter); 				// for println syntax
	static HashMap<Settings, String> settingValues = new HashMap<>();

	public SettingsManager() throws IOException {
		new File(HOI4UTILS_PROPERTIES_PATH).mkdir();
		settings_file = new File(HOI4UTILS_PROPERTIES_PATH + File.separator + "HOIIVUtils_properties.txt");
		settings_file.createNewFile();

		readSettings();
	}

	public SettingsManager(HashMap<Settings, String> settings) throws IOException {
		String user_docs_path = System.getProperty("user.home") + File.separator + "Documents";
		String hoi4UtilsPropertiesPath = user_docs_path + File.separator + "HOIIVUtils";
		new File(hoi4UtilsPropertiesPath).mkdir();
		settings_file = new File(hoi4UtilsPropertiesPath + File.separator + "HOIIVUtils_properties.txt");
		boolean newSettingsFileCreated = settings_file.createNewFile();

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
			Scanner settingReader = new Scanner(settings_file);
//			System.out.println(settingReader.nextLine());

			/* if file is empty then write blank hoi4utils.settings to new hoi4utils.settings file */
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
			throw new RuntimeException(e);
		}

	}

	/**
	 * Writes a blank setting to the hoi4utils.settings file
	 */
	private static void writeBlankSetting(Settings setting) throws IOException {
		settingsWriter = new FileWriter(settings_file, true);		// true = append
		settingsBWriter = new BufferedWriter(settingsWriter);
		settingsPWriter = new PrintWriter(settingsBWriter);

		settingsPWriter.println(setting.name() + ";" + setting.defaultProperty());

		settingsPWriter.close();
	}

	/**
	 * Writes default hoi4utils.settings to the hoi4utils.settings file
	 */
	public static void writeBlankSettings() throws IOException {
		settingsWriter = new FileWriter(settings_file, false);		// true = append
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
		settingsWriter = new FileWriter(settings_file, false);		// true = append
		settingsBWriter = new BufferedWriter(settingsWriter);
		settingsPWriter = new PrintWriter(settingsBWriter);

		settingValues.put(setting, settingValue);
		for (Settings s : Settings.values()) {
			settingsPWriter.println(s.name() + ";" + settingValues.get(s));
		}

		settingsPWriter.close();
	}

	/**
	 * Saves a list of hoi4utils.settings to hoi4utils.settings file, all other hoi4utils.settings remain the same.
	 * @param newSettings list of updated hoi4utils.settings to save
	 * @throws IOException
	 */
	public static void saveSettings(HashMap<Settings, String> newSettings) throws IOException {
		if (newSettings == null) {
			return;
		}

		settingsWriter = new FileWriter(settings_file, false);		// true = append
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
	 * @throws IOException
	 */
	public static void saveSettings() throws IOException {
		settingsWriter = new FileWriter(settings_file, false);		// true = append
		settingsBWriter = new BufferedWriter(settingsWriter);
		settingsPWriter = new PrintWriter(settingsBWriter);

		for (Settings s : Settings.values()) {
			settingsPWriter.println(s.name() + ";" + settingValues.get(s));
		}

		settingsPWriter.close();
	}

	public static String get(Settings setting) { return settingValues.get(setting); }

	public static boolean isNull(Settings setting) {
		return settingValues.get(setting).equals("null");
	}

	public static void deleteAllSettings() throws IOException {
        Path dir = Paths.get(HOI4UTILS_PROPERTIES_PATH); //path to the directory  
        Files
            .walk(dir) // Traverse the file tree in depth-first order
            .sorted(Comparator.reverseOrder())
            .forEach(path -> {
                try {
					if (Settings.DEV_MODE.enabled()) {
						System.out.println("Deleting: " + path);
					}
                    Files.delete(path);  //delete each file or directory
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
	}
}
