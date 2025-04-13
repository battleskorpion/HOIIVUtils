package com.hoi4utils.clausewitz;

import com.hoi4utils.PublicFieldChangeNotifier;
import com.hoi4utils.clausewitz.code.effect.EffectDatabase;
import com.hoi4utils.clausewitz.code.modifier.ModifierDatabase;
import com.hoi4utils.clausewitz.data.country.Country;
import com.hoi4utils.clausewitz.data.country.CountryTag;
import com.hoi4utils.clausewitz.data.focus.FocusTree;
import com.hoi4utils.clausewitz.data.idea.IdeaFile;
import com.hoi4utils.clausewitz.localization.EnglishLocalizationManager;
import com.hoi4utils.clausewitz.localization.LocalizationManager;
import com.hoi4utils.clausewitz.map.state.ResourcesFile;
import com.hoi4utils.clausewitz.map.state.State;
import com.hoi4utils.clausewitz.data.gfx.Interface;
import com.hoi4utils.fileIO.FileListener.FileAdapter;
import com.hoi4utils.fileIO.FileListener.FileEvent;
import com.hoi4utils.fileIO.FileListener.FileWatcher;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

/**
 * Handles initialization of the HOIIVUtils application.
 * Separates initialization logic from utility functions.
 */
public class HOIIVUtilsInitializer {
	private static final Logger LOGGER = LogManager.getLogger(HOIIVUtilsInitializer.class);
	public static final PublicFieldChangeNotifier changeNotifier = new PublicFieldChangeNotifier(HOIIVUtilsInitializer.class);
	private final Properties properties = new Properties();
	
	@SuppressWarnings("exports")
	private FileWatcher stateFilesWatcher;
	
	private HOIIVModLoader modLoader;
	private File hoi4UtilsDir;
	private String propertiesFile;
	private InputStream defaultProperties;
	private String version;

	/**
	 * Initializes the HOIIVUtils application.
	 *
	 * @return Configured HOIIVUtils configuration for use by the application
	 */
	public HOIIVUtilsConfig initialize() {
		LOGGER.info("Initializing HOIIVUtils");
		// Load databases first
		ModifierDatabase.init();
		EffectDatabase.init();

		// Configure application directories
		resolveApplicationDirectory();

		// Load configuration
		initializeConfiguration();

		// Configure paths
		autoSetHOIIVPath();
		autoSetDemoModPath();

		// Save configuration after initialization
		saveConfiguration();

		// Set version
		version = getProperty("version");
		LOGGER.info("HOIIVUtils {} initialized", version);

		// Create config with change notifier
		HOIIVUtilsConfig config = createConfig();
		
		// Initialize mod loader
		modLoader = new HOIIVModLoader(config);
		
		// Return configuration for use by application
		return config;
	}

	private void resolveApplicationDirectory() {
		try {
			URI sourceLocation = HOIIVUtils.class.getProtectionDomain().getCodeSource().getLocation().toURI();
			File sourceFile = new File(sourceLocation);

			// Check if parent exists
			File parentDir = new File(sourceFile.getParent());
			if (!parentDir.exists()) {
				LOGGER.warn("Parent directory does not exist: {}", parentDir);
				throw new RuntimeException("Failed to determine application parent directory");
			}

			hoi4UtilsDir = new File(parentDir.getParent());

			boolean isInvalidDir = !hoi4UtilsDir.exists() || !hoi4UtilsDir.isDirectory() || hoi4UtilsDir == null;
			if (isInvalidDir) {
				LOGGER.warn("Invalid HOIIVUTILS_DIR: {}", hoi4UtilsDir);
				throw new RuntimeException("Invalid HOIIVUtils directory");
			}
		} catch (URISyntaxException e) {
			LOGGER.error("Failed to determine application directory", e);
			throw new RuntimeException("Failed to determine application directory", e);
		} catch (NullPointerException e) {
			LOGGER.error("Null reference encountered while determining application directory", e);
			throw new RuntimeException("Null reference in application directory resolution", e);
		} catch (Exception e) {
			LOGGER.error("Unexpected error while determining application directory", e);
			throw new RuntimeException("Unexpected error determining application directory", e);
		}

		LOGGER.debug("HOIIVUtils Directory: {}", hoi4UtilsDir);
	}

	private void initializeConfiguration() {
		propertiesFile = hoi4UtilsDir + File.separator + "HOIIVUtils.properties";
		defaultProperties = HOIIVUtils.class.getClassLoader().getResourceAsStream("HOIIVUtils.properties");
		loadConfiguration();
	}

	private void loadConfiguration() {
		File externalFile = new File(propertiesFile);

		if (!externalFile.exists()) {
			LOGGER.warn("External configuration file not found: {}", propertiesFile);
			loadDefaultProperties();
		}

		try (FileInputStream input = new FileInputStream(externalFile)) {
			LOGGER.debug("External configuration loaded from: {}", propertiesFile);
			properties.load(input);
		} catch (IOException e) {
			LOGGER.fatal("Error loading external properties from: {}", propertiesFile, e);
			loadDefaultProperties();
		}
	}

	private void loadDefaultProperties() {
		File externalFile = new File(propertiesFile);
		try (OutputStream externalOut = new FileOutputStream(externalFile)) {
			byte[] buffer = new byte[8192];
			int bytesRead;
			while ((bytesRead = defaultProperties.read(buffer)) != -1) {
				externalOut.write(buffer, 0, bytesRead);
			}
			LOGGER.debug("Default properties copied to: {}", externalFile.getAbsolutePath());
		} catch (IOException e) {
			LOGGER.error("Failed to copy default properties to external file", e);
		}
	}

	public void saveConfiguration() {
		File externalFile = new File(propertiesFile);

		boolean noSavedSettings = !externalFile.exists() && defaultProperties != null;
		if (noSavedSettings) {
			loadDefaultProperties();
		}

		try (OutputStream output = new FileOutputStream(externalFile)) {
			LOGGER.debug("Configuration saved to: {}", externalFile.getAbsolutePath());
			properties.store(output, "HOIIVUtils Configuration");
		} catch (IOException e) {
			LOGGER.error("Failed to save configuration", e);
			throw new RuntimeException(e);
		}
	}

	private void autoSetHOIIVPath() {
		String hoi4Path = getProperty("hoi4.path");
		boolean hoi4PathNotSet = hoi4Path == null || hoi4Path.isBlank();

		if (!hoi4PathNotSet) {
			LOGGER.debug("HOI4 path already set. Skipping auto-set.");
			return;
		}

		for (String path : getPossibleHOIIVPaths()) {
			File hoi4Dir = Paths.get(path).toAbsolutePath().toFile();
			if (hoi4Dir.exists()) {
				setProperty("hoi4.path", hoi4Dir.getAbsolutePath());
				LOGGER.debug("Auto-set HOI4 path: {}", hoi4Dir.getAbsolutePath());
				return;
			}
		}

		LOGGER.warn("Couldn't find HOI4 install folder. User must set it manually.");
		JOptionPane.showMessageDialog(null,
			"Couldn't find HOI4 install folder, please go to settings and add it (REQUIRED)",
			"Error Message", JOptionPane.WARNING_MESSAGE);
	}

	private List<String> getPossibleHOIIVPaths() {
		String os = System.getProperty("os.name").toLowerCase();
		List<String> possibleHOIIVPaths = new ArrayList<>();

		if (os.contains("win")) {
			possibleHOIIVPaths.add("C:\\Program Files (x86)\\Steam\\steamapps\\common\\Hearts of Iron IV");
			possibleHOIIVPaths.add(System.getenv("ProgramFiles") + "\\Steam\\steamapps\\common\\Hearts of Iron IV");
		} else if (os.contains("nix") || os.contains("nux")) {
			possibleHOIIVPaths.add(System.getProperty("user.home") + "/.steam/steam/steamapps/common/Hearts of Iron IV");
			possibleHOIIVPaths.add(System.getProperty("user.home") + "/.local/share/Steam/steamapps/common/Hearts of Iron IV");
		} else if (os.contains("mac")) {
			possibleHOIIVPaths.add(System.getProperty("user.home") + "/Library/Application Support/Steam/steamapps/common/Hearts of Iron IV");
		}
		return possibleHOIIVPaths;
	}

	private void autoSetDemoModPath() {
		String modPath = getProperty("mod.path");
		boolean modPathNotSet = modPath == null || modPath.isBlank();

		if (modPathNotSet) {
			setProperty("mod.path", hoi4UtilsDir + File.separator + "demo_mod");
			LOGGER.debug("Auto-set mod path to demo_mod");
			return;
		}

		boolean modPathIsDemo = false;
		try {
			modPathIsDemo = Paths.get(modPath).getFileName().toString().equals("demo_mod");
		} catch (Exception e) {
			LOGGER.warn("Error checking mod path: {}", e.getMessage());
		}

		if (modPathIsDemo) {
			setProperty("mod.path", hoi4UtilsDir + File.separator + "demo_mod");
			LOGGER.debug("Reset mod path to demo_mod");
		} else {
			LOGGER.debug("Mod path already set. Skipping auto-set.");
		}
	}

	/**
	 * Loads mod data using the HOIIVModLoader.
	 * This delegates to the central mod loading logic.
	 */
	public void loadMod() {
		if (modLoader == null) {
			LOGGER.error("Cannot load mod: mod loader is not initialized");
			return;
		}
		modLoader.loadMod();
	}

	private HOIIVUtilsConfig createConfig() {
		return new HOIIVUtilsConfig(
			hoi4UtilsDir,
			propertiesFile,
			defaultProperties,
			version,
			properties, 
			changeNotifier::checkAndNotifyChanges
		);
	}

	public String getProperty(String key) {
		return properties.getProperty(key);
	}

	public void setProperty(String key, String value) {
		properties.setProperty(key, value);
	}

	/**
	 * Watches the state files in the given directory.
	 * @param stateFiles The directory containing state files.
	 */
	public void watchStateFiles(File stateFiles) {
		if (!HOIIVFiles.validateDirectoryPath(Optional.ofNullable(stateFiles).map(File::getPath).orElse(null), "State files directory")) {
			return;
		}

		stateFilesWatcher = new FileWatcher(stateFiles);

		stateFilesWatcher.addListener(new FileAdapter() {
			@Override
			public void onCreated(FileEvent event) {
				handleStateFileEvent(event, "created/loaded", State::readState);
			}

			@Override
			public void onModified(FileEvent event) {
				handleStateFileEvent(event, "modified", State::readState);
			}

			@Override
			public void onDeleted(FileEvent event) {
				handleStateFileEvent(event, "deleted", State::removeState);
			}
		}).watch();
	}

	/**
	 * Handles state file events.
	 * @param event File event that occurred.
	 * @param actionName Name of the action performed.
	 * @param stateAction Function to apply to the file.
	 */
	private void handleStateFileEvent(FileEvent event, String actionName, java.util.function.Consumer<File> stateAction) {
		EventQueue.invokeLater(() -> {
			stateFilesWatcher.listenerPerformAction++;
			File file = event.getFile();
			stateAction.accept(file);
			stateFilesWatcher.listenerPerformAction--;
			LOGGER.debug("State was {}: {}", actionName, State.get(file));
		});
	}

	public void addPropertyChangeListener(PropertyChangeListener listener) {
		changeNotifier.addPropertyChangeListener(listener);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		changeNotifier.removePropertyChangeListener(listener);
	}
}