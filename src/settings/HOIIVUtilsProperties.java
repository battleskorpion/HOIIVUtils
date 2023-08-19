package settings;

import java.io.*;
import java.util.HashMap;
import java.util.Scanner;
/*
 * HOI 4 Util Properties File
 */
public class HOIIVUtilsProperties {

	public static final String USER_DOCS_PATH = System.getProperty("user.home") + File.separator + "Documents";
	public static final String HOI4UTILS_PROPERTIES_PATH = USER_DOCS_PATH + File.separator + "HOIIVUtils";

	public enum Settings {
		MOD_PATH {
			public String toString() { return (String) getSetting(); }
		},
		CURRENT_MOD,		// todo not in use
		CIVILIAN_MILITARY_FACTORY_MAX_RATIO,			// ratio for civ/mil factories highlight in buildings view
		DARK_MODE {
			public Object getSetting() {
				return settingValues.get(this).equals("true");
			}
			public String defaultProperty() { return "false"; }
		},
		DEV_MODE {
			public Object getSetting() {
				return settingValues.get(this).equals("true");
			}
			public String defaultProperty() { return "false"; }
		},
		PREFERRED_SCREEN {
			public Object getSetting() {
				try {
					return Integer.parseInt(settingValues.get(this));
				} catch (NumberFormatException exc) {
					if (enabled(DEV_MODE)) {
						System.err.print(exc);
					}
					return 0;
				}
			}
			public String defaultProperty() { return "0"; }
		},
		;

		/**
		 * Returns if setting is enabled (setting is true).
		 * @param setting
		 * @return
		 */
		public static boolean enabled(Settings setting) {
			if (settingValues.get(setting) == null) {
				return setting.defaultProperty().equals("true");
			}
			return settingValues.get(setting).equals("true");
		}

		/**
		 * Sets the value of the setting
		 * @param value
		 */
		public void setSetting(String value) {
			settingValues.put(this, String.valueOf(value));
			try {
				saveSettings();
				if ((boolean)DEV_MODE.getSetting()) {
					System.out.println("Updated setting " + this.name() + ": " + settingValues.get(this));
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			return;
		}

		public String defaultProperty() {
			return "null";
		}

		public Object getSetting() {
			return settingValues.get(this);
		}

		public String toString() { return null; };
	}

	private static File settings_file;
	private static FileWriter settingsWriter;
	private static BufferedWriter settingsBWriter;
	private static PrintWriter settingsPWriter;// = new PrintWriter(settingsBWriter); 				// for println syntax
	private static HashMap<Settings, String> settingValues = new HashMap<>();

	public HOIIVUtilsProperties() throws IOException {
		new File(HOI4UTILS_PROPERTIES_PATH).mkdir();
		settings_file = new File(HOI4UTILS_PROPERTIES_PATH + File.separator + "HOIIVUtils_properties.txt");
		settings_file.createNewFile();

		readSettings();
	}

	public HOIIVUtilsProperties(HashMap<Settings, String> settings) throws IOException {
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
	 * Reads settings from settings file
	 */
	public static void readSettings() {
		try {
			Scanner settingReader = new Scanner(settings_file);
//			System.out.println(settingReader.nextLine());

			/* if file is empty then write blank settings to new settings file */
			if (!settingReader.hasNext()) {
				writeBlankSettings();
				settingReader.close();
				return;
			}

			/* read settings */
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
	 * Writes a blank setting to the settings file
	 */
	private static void writeBlankSetting(Settings setting) throws IOException {
		settingsWriter = new FileWriter(settings_file, true);		// true = append
		settingsBWriter = new BufferedWriter(settingsWriter);
		settingsPWriter = new PrintWriter(settingsBWriter);

		settingsPWriter.println(setting.name() + ";" + setting.defaultProperty());

		settingsPWriter.close();
	}

	/**
	 * Writes default settings to the settings file
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
	 * Saves a list of settings to settings file, all other settings remain the same.
	 * @param newSettings list of updated settings to save
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
	 * Saves all settings to settings file.
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
}
