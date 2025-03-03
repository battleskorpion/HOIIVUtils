package com.hoi4utils.clausewitz;

import com.hoi4utils.clausewitz.data.focus.FocusTree;
import com.hoi4utils.clausewitz.localization.LocalizationManager;
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
		
		LocalizationManager.get().reload();
		
		if (Interface.read()) {
			config.setProperty("valid.Interface", "true");
		} else {
			config.setProperty("valid.Interface", "false");
			LOGGER.error("Failed to read gfx interface files");
		}
		
		if (State.read()) {
			config.setProperty("valid.State", "true");
		} else {
			config.setProperty("valid.State", "false");
			LOGGER.error("Failed to read states");
		}

		if (FocusTree.read()) {
			config.setProperty("valid.FocusTree", "true");
		} else {
			config.setProperty("valid.FocusTree", "false");
			LOGGER.error("Failed to read focus trees");
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
			JOptionPane.showMessageDialog(null, keyName + " is null or empty!", "Error", JOptionPane.ERROR_MESSAGE);
			return false;
		}

		File directory = new File(path);

		if (!directory.exists() || !directory.isDirectory()) {
			LOGGER.error("{} does not point to a valid directory: {}", keyName, path);
			JOptionPane.showMessageDialog(null, keyName + " does not point to a valid directory: " + path, "Error", JOptionPane.ERROR_MESSAGE);
			return false;
		}

		return true;
	}
}
