package com.hoi4utils.clausewitz;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.Properties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.hoi4utils.clausewitz.code.effect.EffectDatabase;
import com.hoi4utils.clausewitz.code.modifier.ModifierDatabase;
import com.hoi4utils.clausewitz.data.focus.FocusTree;
import com.hoi4utils.clausewitz.localization.EnglishLocalizationManager;
import com.hoi4utils.clausewitz.map.state.State;
import com.hoi4utils.ui.menu.MenuController;

import javax.swing.*;

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
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
	public static final String DEFAULT_PROPERTIES = "HOIIVUtils.properties"; // In JAR // Outside JAR
	public static final String PROPERTIES_FILE = HOIIVUTILS_DIR + File.separator + "HOIIVUtils.properties";
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

	private static void autoSetPaths() throws URISyntaxException {
		autoSetDemoModPath();
		autoSetHOIIVPath();
	}

	private static void autoSetDemoModPath() throws URISyntaxException {
		if (get("mod.path") == null) {
			set("mod.path", HOIIVUTILS_DIR + File.separator + "demo_mod");
			System.out.println("auto set mod path to demo mod");
		} else if (Paths.get(get("mod.path")).getFileName().toString().equals("demo_mod")) { // If mod path directory is named demo mod it will reset it incase entire directory moved
			set("mod.path", HOIIVUTILS_DIR + File.separator + "demo_mod");
			System.out.println("auto set mod path to demo mod");
		}
	}

	private static void autoSetHOIIVPath() {
		// If the HOI4 path is already set, do nothing
		if (get("hoi4.path") != null) {
			return;
		}

		// List of potential default paths to check
		String[] possiblePaths = {
				"C:\\Program Files (x86)\\Steam\\steamapps\\common\\Hearts of Iron IV",
				"~/.steam/steam/steamapps/common"
		};

		// Loop through potential paths and set the first valid one
		for (String path : possiblePaths) {
			String expandedPath = path.replace("~", System.getProperty("user.home"));
			if (new File(expandedPath).exists()) {
				set("hoi4.path", expandedPath);
				System.out.println("Auto-set HOI4 path: " + expandedPath);
				return;
			}
		}

		// If no valid path found, show a message
		JOptionPane.showMessageDialog(null,
				"Couldn't find HOI4 install folder, please go to settings and add it (REQUIRED)",
				"Error Message", JOptionPane.WARNING_MESSAGE);
		save();
	}
}