package com.hoi4utils.clausewitz;

import com.hoi4utils.clausewitz.code.effect.EffectDatabase;
import com.hoi4utils.clausewitz.code.modifier.ModifierDatabase;
import com.hoi4utils.clausewitz.data.focus.FocusTree;
import com.hoi4utils.clausewitz.localization.EnglishLocalizationManager;
import com.hoi4utils.clausewitz.map.state.State;
import com.hoi4utils.ui.menu.MenuController;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.ArrayList;
import java.util.List;
/**
 * HOIIVUtils.java main method is here
 */
public class HOIIVUtils {
	/**
	 * HOIIVUTILS Directory
	 * Layout:
	 * HOIIVUtils\\target\\HOIIVUtils.jar
	 * HOIIVUtils\\demo_mod*
	 * HOIIVUtils\\HOIIVUtils.bat
	 * HOIIVUtils\\HOIIVUtils.properties
	 * HOIIVUtils\\HOIIVUtils.sh
	 */
	public static final Logger LOGGER = LogManager.getLogger(HOIIVUtils.class);
	public static String HOIIVUTILS_DIR;
    static {
        try {
            HOIIVUTILS_DIR = new File(new File(HOIIVUtils.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParent()).getParent();
			LOGGER.info("HOIIVUtils Directory: {}", HOIIVUTILS_DIR);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
	public static final String DEFAULT_PROPERTIES = "HOIIVUtils.properties"; // In JAR // Outside JAR
	public static final String PROPERTIES_FILE = HOIIVUTILS_DIR + File.separator + "HOIIVUtils.properties";
    private static final Properties properties = new Properties();
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
        try {
            autoSetPaths();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
	public static final String HOIIVUTILS_VERSION = get("version");
	static {
		System.out.println("HOIIVUtils" + " " + HOIIVUTILS_VERSION + " launched");
	}
	public static final String DARK_MODE_STYLESHEETURL = "com/hoi4utils/ui/javafx_dark.css";
	public static MenuController menuController;

	public static void main(String[] args) {
		/**
		 * Load modifiers and effects
		 * preprocessing which doesn't require settings
		 * TODO Fix module or pom.xml to compile the code with the database (.db) files
		 */
		ModifierDatabase mdb = new ModifierDatabase();
		EffectDatabase edb = new EffectDatabase();

		LOGGER.debug("This is a debug message");
		LOGGER.info("This is an info message");
		LOGGER.warn("This is a warn message");
		LOGGER.error("This is an error message");
		LOGGER.fatal("This is a fatal message");

		loadMod();

		menuController = new MenuController();
		menuController.launchMenuWindow(args); // Program starts!
	}

	public static void loadMod() {
		HOIIVFile.createHOIIVFilePaths();
		new EnglishLocalizationManager().reload();
		State.read();
		FocusTree.read();
	}

	public static String get(String key) {
		return properties.getProperty(key);
	}

	public static int getInt(String key) {
		try {
			return Integer.parseInt(properties.getProperty(key, "0"));
		} catch (NumberFormatException e) {
			// log this todo
			return 0;
		}
	}

	// todo do we want null for a unset setting?
	public static boolean getBoolean(String key) {
		try {
			return Boolean.parseBoolean(properties.getProperty(key, "false"));
		} catch (NumberFormatException e) {
			// log this todo
			return false;
		}
	}

	public static void set(String key, String value) {
		properties.setProperty(key, value);
		save();
	}

	public static void save() {
		File externalFile = new File(PROPERTIES_FILE);

		// Ensure external file exists before saving
		if (!externalFile.exists() && HOIIVUtils.class.getClassLoader().getResourceAsStream(DEFAULT_PROPERTIES) != null) {
			try {
				loadDefaultProperties();
			} catch (IOException e) {
				LOGGER.error("Failed to load default properties before saving", e);
				return;
			}
		}

		try (OutputStream output = new FileOutputStream(externalFile)) {
			properties.store(output, "HOIIVUtils Configuration");
			LOGGER.info("Configuration saved to: {}", externalFile.getAbsolutePath());
		} catch (IOException e) {
			LOGGER.error("Failed to save configuration", e);
		}
	}

	private static boolean loadExternalProperties() {
		File externalFile = new File(PROPERTIES_FILE);

		if (!externalFile.exists()) {
			LOGGER.warn("External configuration file not found: {}", PROPERTIES_FILE);
			return false;
		}

		try (FileInputStream input = new FileInputStream(externalFile)) {
			properties.load(input);
			LOGGER.info("External configuration loaded from: {}", PROPERTIES_FILE);
			return true;
		} catch (IOException e) {
			LOGGER.error("Error loading external properties from: {}", PROPERTIES_FILE, e);
			return false;
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

		try (InputStream defaultConfig = HOIIVUtils.class.getClassLoader().getResourceAsStream(DEFAULT_PROPERTIES)) {

			if (defaultConfig == null) {
				throw new FileNotFoundException("Default properties file not found in JAR: " + DEFAULT_PROPERTIES);
			}

			try (OutputStream externalOut = new FileOutputStream(externalFile)) {
				byte[] buffer = new byte[8192]; // 8KB buffer for better performance
				int bytesRead;
				while ((bytesRead = defaultConfig.read(buffer)) != -1) {
					externalOut.write(buffer, 0, bytesRead);
				}
				LOGGER.info("Default properties copied to: {}", externalFile.getAbsolutePath());
			}

		} catch (IOException e) {
			LOGGER.error("Failed to copy default properties to: {}", externalFile.getAbsolutePath(), e);
			throw e;
		}
	}

	private static void autoSetPaths() throws URISyntaxException {
		autoSetDemoModPath();
		autoSetHOIIVPath();
	}

	private static void autoSetDemoModPath() {
		if (HOIIVUTILS_DIR == null || HOIIVUTILS_DIR.isBlank()) {
			LOGGER.warn("HOIIVUTILS_DIR is not set. Cannot auto-set mod.path.");
			return;
		}

		String modPath = get("mod.path");

		if (modPath == null || modPath.isBlank()) {
			set("mod.path", HOIIVUTILS_DIR + File.separator + "demo_mod");
			LOGGER.info("Auto-set mod path to demo_mod");
		} else {
			try {
				if (Paths.get(modPath).getFileName().toString().equals("demo_mod")) {
					set("mod.path", HOIIVUTILS_DIR + File.separator + "demo_mod");
					LOGGER.info("Auto-reset mod path to demo_mod (directory was moved)");
				}
			} catch (InvalidPathException e) {
				LOGGER.error("Invalid mod.path: {}. Resetting to default.", modPath, e);
				set("mod.path", HOIIVUTILS_DIR + File.separator + "demo_mod");
			}
		}
	}


	private static void autoSetHOIIVPath() {
		if (get("hoi4.path") != null) {
			return;
		}

		String os = System.getProperty("os.name").toLowerCase();
		List<String> possiblePaths = new ArrayList<>();

		if (os.contains("win")) {
			possiblePaths.add("C:\\Program Files (x86)\\Steam\\steamapps\\common\\Hearts of Iron IV");
			possiblePaths.add(System.getenv("ProgramFiles") + "\\Steam\\steamapps\\common\\Hearts of Iron IV");
		} else if (os.contains("nix") || os.contains("nux")) {
			possiblePaths.add(System.getProperty("user.home") + "/.steam/steam/steamapps/common/Hearts of Iron IV");
			possiblePaths.add(System.getProperty("user.home") + "/.local/share/Steam/steamapps/common/Hearts of Iron IV");
		} else if (os.contains("mac")) {
			possiblePaths.add(System.getProperty("user.home") + "/Library/Application Support/Steam/steamapps/common/Hearts of Iron IV");
		}

		for (String path : possiblePaths) {
			File hoi4Dir = Paths.get(path).toAbsolutePath().toFile();
			if (hoi4Dir.exists()) {
				set("hoi4.path", hoi4Dir.getAbsolutePath());
				LOGGER.info("Auto-set HOI4 path: {}", hoi4Dir.getAbsolutePath());
				return;
			}
		}

		LOGGER.warn("Couldn't find HOI4 install folder. User must set it manually.");
		JOptionPane.showMessageDialog(null,
				"Couldn't find HOI4 install folder, please go to settings and add it (REQUIRED)",
				"Error Message", JOptionPane.WARNING_MESSAGE);

		save();
	}
}