package com.hoi4utils.clausewitz;

import com.hoi4utils.clausewitz.data.focus.FocusTree;
import com.hoi4utils.clausewitz.localization.EnglishLocalizationManager;
import com.hoi4utils.clausewitz.map.state.State;
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
		if (!createHOIIVFilePaths()) {
			LOGGER.error("Failed to create HOIIV file paths");
			config.setProperty("valid.HOIIVFilePaths", "false");
		} else {
			config.setProperty("valid.HOIIVFilePaths", "true");
		}

		new EnglishLocalizationManager().reload();
		LOGGER.info("List of states: {}", State.list());
		State.delete();
		if (!State.read()) {
			LOGGER.error("Failed to read states");
			config.setProperty("valid.State", "false");
		} else {
			config.setProperty("valid.State", "true");
		}

		if (!FocusTree.read()) {
			LOGGER.error("Failed to read focus trees");
			config.setProperty("valid.FocusTree", "false");
		} else {
			config.setProperty("valid.FocusTree", "true");
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
		HOIIVUtilsFiles.mod_folder = null;
		HOIIVUtilsFiles.mod_common_folder = null;
		HOIIVUtilsFiles.mod_focus_folder = null;
		HOIIVUtilsFiles.mod_ideas_folder = null;
		HOIIVUtilsFiles.mod_units_folder = null;
		HOIIVUtilsFiles.mod_states_folder = null;
		HOIIVUtilsFiles.mod_localization_folder = null;
		HOIIVUtilsFiles.mod_strat_region_dir = null;

		HOIIVUtilsFiles.mod_folder = new File(modPath);
		HOIIVUtilsFiles.mod_common_folder = new File(modPath, "common");
		HOIIVUtilsFiles.mod_focus_folder = new File(HOIIVUtilsFiles.mod_common_folder, "national_focus");
		HOIIVUtilsFiles.mod_ideas_folder = new File(HOIIVUtilsFiles.mod_common_folder, "ideas");
		HOIIVUtilsFiles.mod_units_folder = new File(HOIIVUtilsFiles.mod_common_folder, "units");
		HOIIVUtilsFiles.mod_states_folder = new File(modPath, "history\\states");
		HOIIVUtilsFiles.mod_localization_folder = new File(modPath, "localisation\\english"); // 's' vs 'z' note in the original comment
		HOIIVUtilsFiles.mod_strat_region_dir = new File(modPath, "map\\strategicregions");
		return true;
	}

	private boolean createHOIIVPaths() {
		String hoi4Path = config.getProperty("mod.path");

		if (!validateDirectoryPath(hoi4Path, "hoi4.path")) {
			return false;
		}

		HOIIVUtilsFiles.hoi4_folder = new File(hoi4Path);
		HOIIVUtilsFiles.hoi4_localization_folder = new File(hoi4Path, "localisation\\english");
		HOIIVUtilsFiles.hoi4_units_folder = new File(hoi4Path, "common\\units");
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
