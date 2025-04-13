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

public class HOIIVModLoader {
	private static final Logger LOGGER = LogManager.getLogger(HOIIVModLoader.class);
	private HOIIVUtilsConfig config;
	public HOIIVModLoader(HOIIVUtilsConfig config) {
		this.config = config;
	}

	void loadMod() {
		if (createHOIIVFilePaths()) {
			config.setProperty("valid.HOIIVFilePaths", "true");
		} else {
			LOGGER.error("Failed to create HOIIV file paths");
			config.setProperty("valid.HOIIVFilePaths", "false");
			}
		
		Interface.clear();
		State.clear();
		FocusTree.clear();
		
		try {
			LocalizationManager.get().reload();
		} catch (Exception e) {
			LOGGER.error("Failed to reload localization", e);
		}
		
		try {
			if (Interface.read()) {
				config.setProperty("valid.Interface", "true");
			} else {
				config.setProperty("valid.Interface", "false");
				LOGGER.error("Failed to read gfx interface files");
			}
		} catch (Exception e) {
			config.setProperty("valid.Interface", "false");
			LOGGER.error("Exception while reading interface files", e);
		}

		try {
			if (ResourcesFile.read()) {
				config.setProperty("valid.Resources", "true");
			} else {
				config.setProperty("valid.Resources", "false");
				LOGGER.error("Failed to read resources");
			}
		} catch (Exception e) {
			config.setProperty("valid.Resources", "false");
			LOGGER.error("Exception while reading resources", e);
		}

		try {
			if (CountryTag.read()) {
				config.setProperty("valid.CountryTag", "true");
			} else {
				config.setProperty("valid.CountryTag", "false");
				LOGGER.error("Failed to read country tags");
			}
		} catch (Exception e) {
			config.setProperty("valid.CountryTag", "false");
			LOGGER.error("Exception while reading country tags", e);
		}
		
		try {
			if (Country.read()) {
				config.setProperty("valid.Country", "true");
			} else {
				config.setProperty("valid.Country", "false");
				LOGGER.error("Failed to read countries");
			}
		} catch (Exception e) {
			config.setProperty("valid.Country", "false");
			LOGGER.error("Exception while reading countries", e);
		}
		
		try {
			if (State.read()) {
				config.setProperty("valid.State", "true");
			} else {
				config.setProperty("valid.State", "false");
				LOGGER.error("Failed to read states");
			}
		} catch (Exception e) {
			config.setProperty("valid.State", "false");
			LOGGER.error("Exception while reading states", e);
		}

		try {
			if (FocusTree.read()) {
				config.setProperty("valid.FocusTree", "true");
			} else {
				config.setProperty("valid.FocusTree", "false");
				LOGGER.error("Failed to read focus trees");
			}
		} catch (Exception e) {
			config.setProperty("valid.FocusTree", "false");
			LOGGER.error("Exception while reading focus trees", e);
		}

		try {
			if (IdeaFile.read()) {
				config.setProperty("valid.IdeaFiles", "true");
			} else {
				config.setProperty("valid.IdeaFiles", "false");
				LOGGER.error("Failed to read idea files");
			}
		} catch (Exception e) {
			config.setProperty("valid.IdeaFiles", "false");
			LOGGER.error("Exception while reading idea files", e);
		}
	}

	private boolean createHOIIVFilePaths() {
		if (!createHOIIVPaths()) {
			return false;
		}
		if (!createModPaths()) {
			return false;
		}
		HOIIVUtilsInitializer.changeNotifier.checkAndNotifyChanges();
		return true;
	}

	private boolean createModPaths() {
		String modPath = config.getProperty("mod.path");

		if (!validateDirectoryPath(modPath, "mod.path")) {
			return false;
		}
		

		HOIIVFiles.setModPathChildDirs(modPath);
		return true;
	}

	private boolean createHOIIVPaths() {
		String hoi4Path = config.getProperty("hoi4.path");

		if (!validateDirectoryPath(hoi4Path, "hoi4.path")) {
			return false;
		}

		HOIIVFiles.setHoi4PathChildDirs(hoi4Path);
		return true;
	}

	/** Validates whether the provided directory path is valid */
	private boolean validateDirectoryPath(String path, String keyName) {
		if (path == null || path.isEmpty()) {
			LOGGER.error("{} is null or empty!", keyName);
			// Log but don't show popup - we'll show a consolidated warning later
			return false;
		}

		File directory = new File(path);

		if (!directory.exists() || !directory.isDirectory()) {
			LOGGER.error("{} does not point to a valid directory: {}", keyName, path);
			// Log but don't show popup - we'll show a consolidated warning later
			return false;
		}

		return true;
	}
}
