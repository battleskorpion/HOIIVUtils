package com.hoi4utils.clausewitz;

import java.io.*;
import java.util.Properties;

import com.hoi4utils.clausewitz.code.effect.EffectDatabase;
import com.hoi4utils.clausewitz.code.modifier.ModifierDatabase;
import com.hoi4utils.ui.menu.MenuController;

/**
 * HOIIVUtils.java main method is here
 */
public class HOIIVUtils {
	// TODO: consider making a portable app where the save path is in the same directory with the jar
	public static final String DEFAULT_PROPERTIES = "HOIIVUtils.properties"; // In JAR
	public static final String PROPERTIES_FILE = System.getProperty("os.name").startsWith("Windows") // Outside JAR
			? System.getenv("APPDATA") + File.separator + "HOIIVUtils" + File.separator + "hoi4utils.properties"
			: System.getProperty("user.home") + File.separator + ".hoi4utils" + File.separator + "hoi4utils.properties";
	private static Properties properties = new Properties();
	static {
		// Attempt to load external properties
		if (loadExternalProperties()) {
			try {
				loadDefaultProperties(); // Warning, this overites saves!!!!
				loadExternalProperties();
			} catch (IOException ex) {
				throw new RuntimeException("Failed to initialize configuration: " + ex.getMessage(), ex);
			}
		}
	}
	public static final String HOIIVUTILS_VERSION = get("version");
	public static final String DARK_MODE_STYLESHEETURL = "com/hoi4utils/ui/javafx_dark.css";
	public static MenuController menuController;

	public static void main(String[] args) {
		System.out.println("HOIIVUtils" + " " + HOIIVUTILS_VERSION + " launched");

		/**
		 * Load modifiers and effects
		 * preprocessing which doesn't require settings
		 * TODO Fix module or pom.xml to compile the code with the database (.db) files
		 */
		ModifierDatabase mdb = new ModifierDatabase();
		EffectDatabase edb = new EffectDatabase();

		HOIIVFile.createHOIIVFilePaths();
		save();
		menuController = new MenuController();
		menuController.launchMenuWindow(args); // Program starts!
	}

	public static String get(String key) {
		return properties.getProperty(key);
	}

	public static int getInt(String key) {
		return Integer.parseInt(properties.getProperty(key, "0"));
	}

	public static boolean getBoolean(String key) {
		return Boolean.parseBoolean(properties.getProperty(key, "false"));
	}

	public static void set(String key, String value) {
		properties.setProperty(key, value);
		save();
	}

	public static void save() {
		File externalFile = new File(PROPERTIES_FILE);
		if (!externalFile.exists()) {
			try {
				loadDefaultProperties();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		try (OutputStream output = new FileOutputStream(externalFile)) {
			properties.store(output, "HOIIVUtils Configuration");
			System.out.println("Configuration saved to: " + externalFile.getAbsolutePath());
		} catch (IOException e) {
			System.err.println("Failed to save configuration: " + e.getMessage());
		}
	}

	private static Boolean loadExternalProperties() {
		File externalFile = new File(PROPERTIES_FILE);
		if (externalFile.exists()) {
			try (FileInputStream input = new FileInputStream(externalFile)) {
				properties.load(input);
				System.out.println("External configuration loaded from: " + PROPERTIES_FILE);
			} catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
			return false;
        } else {
			System.out.println("External configuration file not found");
			return true;
		}
	}

	/**
	 * Loads the default properties file from within the JAR and copies it to an external location.
	 * If the external properties file does not exist, this method creates it along with the necessary directories.
	 * The external file serves as the application's configurable settings file.
	 *
	 * @throws IOException if an I/O error occurs during file creation or copying.
	 * @throws FileNotFoundException if the default properties file cannot be found in the JAR.
	 */
	private static void loadDefaultProperties() throws IOException {
		File externalFile = new File(PROPERTIES_FILE);

		if (externalFile.getParentFile() != null) {
			externalFile.getParentFile().mkdirs();
		}

		if (!externalFile.exists()) {
			externalFile.createNewFile();
		}

		try (InputStream defaultConfig = HOIIVUtils.class.getClassLoader().getResourceAsStream(DEFAULT_PROPERTIES);
			 OutputStream externalOut = new FileOutputStream(externalFile)) {

			System.out.println("Default Config" + defaultConfig);

			if (defaultConfig == null) {
				throw new FileNotFoundException("Default properties file not found in JAR: " + DEFAULT_PROPERTIES);
			}

			byte[] buffer = new byte[1024];
			int bytesRead;
			while ((bytesRead = defaultConfig.read(buffer)) != -1) {
				externalOut.write(buffer, 0, bytesRead);
				System.out.println("bytesRead:" + bytesRead);
			}

			System.out.println("Default properties copied to: " + externalFile.getAbsolutePath());
		}
	}
}