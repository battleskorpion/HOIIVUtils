package com.hoi4utils.clausewitz;

import com.hoi4utils.clausewitz.data.country.Country;
import com.hoi4utils.clausewitz.data.country.CountryTag;
import com.hoi4utils.clausewitz.data.focus.FocusTree;
import com.hoi4utils.clausewitz.data.idea.IdeaFile;
import com.hoi4utils.clausewitz.localization.LocalizationManager;
import com.hoi4utils.clausewitz.map.state.ResourcesFile;
import com.hoi4utils.clausewitz.map.state.State;
import com.hoi4utils.clausewitz.data.gfx.Interface;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.io.File;
import java.util.function.Consumer;
import java.util.Properties;

/**
 * Handles loading of mod data and file paths for the HOIIVUtils application.
 */
public class HOIIVModLoader {
	private static final Logger LOGGER = LogManager.getLogger(HOIIVModLoader.class);
	private final HOIIVUtilsConfig config;
	private final Properties properties;
	private final Consumer<Runnable> changeNotifier;
	
	/**
	 * Creates a new HOIIVModLoader using the specified config.
	 *
	 * @param config The configuration to use
	 */
	public HOIIVModLoader(HOIIVUtilsConfig config) {
		this.config = config;
		this.properties = config.getProperties();
		this.changeNotifier = config.getChangeNotifier();
	}

	/**
	 * Loads all mod data from the configured paths.
	 * This will set up file paths and reload all mod data.
	 */
	public void loadMod() {
		if (setupFilePaths()) {
			setProperty("valid.HOIIVFilePaths", "true");
		} else {
			LOGGER.error("Failed to create HOIIV file paths");
			setProperty("valid.HOIIVFilePaths", "false");
		}
		
		clearExistingData();
		reloadLocalization();
		loadModModules();
	}
	
	/**
	 * Sets up the file paths for both HOI4 and mod folders.
	 * Uses the paths stored in the configuration.
	 *
	 * @return true if successful, false otherwise
	 */
	public boolean setupFilePaths() {
		final String modPath = config.getProperty("mod.path");
		final String hoi4Path = config.getProperty("hoi4.path");
		
		return HOIIVFiles.setupFilePaths(modPath, hoi4Path, () -> {
			if (changeNotifier != null) {
				changeNotifier.accept(() -> {});
			}
		});
	}
	
	/**
	 * Clears any existing data to prepare for reloading.
	 */
	private void clearExistingData() {
		Interface.clear();
		State.clear();
		FocusTree.clear();
	}
	
	/**
	 * Reloads localization data.
	 */
	private void reloadLocalization() {
		try {
			LocalizationManager.get().reload();
		} catch (Exception e) {
			LOGGER.error("Failed to reload localization", e);
		}
	}
	
	/**
	 * Loads all mod modules (interface, resources, countries, etc.).
	 */
	private void loadModModules() {
		loadInterface();
		loadResources();
		loadCountryTags();
		loadCountries();
		loadStates();
		loadFocusTrees();
		loadIdeaFiles();
	}
	
	/**
	 * Loads interface data.
	 */
	private void loadInterface() {
		try {
			if (Interface.read()) {
				setProperty("valid.Interface", "true");
			} else {
				setProperty("valid.Interface", "false");
				LOGGER.error("Failed to read gfx interface files");
			}
		} catch (Exception e) {
			setProperty("valid.Interface", "false");
			LOGGER.error("Exception while reading interface files", e);
		}
	}
	
	/**
	 * Loads resources data.
	 */
	private void loadResources() {
		try {
			if (ResourcesFile.read()) {
				setProperty("valid.Resources", "true");
			} else {
				setProperty("valid.Resources", "false");
				LOGGER.error("Failed to read resources");
			}
		} catch (Exception e) {
			setProperty("valid.Resources", "false");
			LOGGER.error("Exception while reading resources", e);
		}
	}
	
	/**
	 * Loads country tags data.
	 */
	private void loadCountryTags() {
		try {
			if (CountryTag.read()) {
				setProperty("valid.CountryTag", "true");
			} else {
				setProperty("valid.CountryTag", "false");
				LOGGER.error("Failed to read country tags");
			}
		} catch (Exception e) {
			setProperty("valid.CountryTag", "false");
			LOGGER.error("Exception while reading country tags", e);
		}
	}
	
	/**
	 * Loads countries data.
	 */
	private void loadCountries() {
		try {
			if (Country.read()) {
				setProperty("valid.Country", "true");
			} else {
				setProperty("valid.Country", "false");
				LOGGER.error("Failed to read countries");
			}
		} catch (Exception e) {
			setProperty("valid.Country", "false");
			LOGGER.error("Exception while reading countries", e);
		}
	}
	
	/**
	 * Loads states data.
	 */
	private void loadStates() {
		try {
			if (State.read()) {
				setProperty("valid.State", "true");
			} else {
				setProperty("valid.State", "false");
				LOGGER.error("Failed to read states");
			}
		} catch (Exception e) {
			setProperty("valid.State", "false");
			LOGGER.error("Exception while reading states", e);
		}
	}
	
	/**
	 * Loads focus trees data.
	 */
	private void loadFocusTrees() {
		try {
			if (FocusTree.read()) {
				setProperty("valid.FocusTree", "true");
			} else {
				setProperty("valid.FocusTree", "false");
				LOGGER.error("Failed to read focus trees");
			}
		} catch (Exception e) {
			setProperty("valid.FocusTree", "false");
			LOGGER.error("Exception while reading focus trees", e);
		}
	}
	
	/**
	 * Loads idea files data.
	 */
	private void loadIdeaFiles() {
		try {
			if (IdeaFile.read()) {
				setProperty("valid.IdeaFiles", "true");
			} else {
				setProperty("valid.IdeaFiles", "false");
				LOGGER.error("Failed to read idea files");
			}
		} catch (Exception e) {
			setProperty("valid.IdeaFiles", "false");
			LOGGER.error("Exception while reading idea files", e);
		}
	}
	
	/**
	 * Sets a property in the configuration.
	 *
	 * @param key Property key
	 * @param value Property value
	 */
	private void setProperty(String key, String value) {
		properties.setProperty(key, value);
	}
}
