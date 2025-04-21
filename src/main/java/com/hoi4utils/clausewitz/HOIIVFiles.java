package com.hoi4utils.clausewitz;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

public class HOIIVFiles {
    private static final Logger LOGGER = LogManager.getLogger(HOIIVFiles.class);

	// Nested class for mod files
	public static class Mod {
		public static File folder;
		public static File focus_folder;
		public static File ideas_folder;
		public static File states_folder;
		public static File strat_region_dir;
		public static File localization_folder;
		public static File common_folder;
		public static File units_folder;
		public static File interface_folder; 
		public static File resources_file;
		public static File state_category_dir;
		public static File country_folder; 
		public static File country_tags_folder;
		public static File province_map_file;
		public static File definition_csv_file;
	}

	// Nested class for base (HOIIV) files
	public static class HOI4 {
		public static File folder;
		public static File localization_folder;
		public static File units_folder;
		public static File interface_folder;
		public static File resources_file;
		public static File state_category_dir;
		public static File country_folder;
		public static File country_tags_folder;
		public static File province_map_file;
		public static File definition_csv_file;
	}

	// Example of a file that might be shared or used as a default mod folder
	public static final File usersParadoxHOIIVModFolder =
			new File(File.separator + "Paradox Interactive" + File.separator + "Hearts of Iron IV" + File.separator + "mod");

	/**
	 * Sets up all mod child directories based on the provided mod path.
	 * 
	 * @param modPath Path to the mod directory
	 * @return true if the directory is valid and paths were set up, false otherwise
	 */
	public static boolean setModPathChildDirs(String modPath) {
		if (!validateDirectoryPath(modPath, "mod.path")) {
			return false;
		}
		
		Mod.folder = new File(modPath);
		Mod.common_folder = new File(modPath, "common");
		Mod.focus_folder = new File(modPath, "common\\national_focus");
		Mod.ideas_folder = new File(modPath, "common\\ideas");
		Mod.units_folder = new File(modPath,"common\\units");
		Mod.states_folder = new File(modPath, "history\\states");
		Mod.localization_folder = new File(modPath, "localisation\\english"); // 's' vs 'z' note in the original comment
		Mod.strat_region_dir = new File(modPath, "map\\strategicregions");
		Mod.interface_folder = new File(modPath, "interface");
		Mod.resources_file = new File(modPath, "common\\resources\\00_resources.txt");
		Mod.state_category_dir = new File(modPath, "common\\state_category");
		Mod.country_folder = new File(modPath, "history\\countries");
		Mod.country_tags_folder = new File(modPath, "common\\country_tags");
		Mod.province_map_file = new File(modPath, "map\\provinces.bmp");
		Mod.definition_csv_file = new File(modPath, "map\\definition.csv");
		
		return true;
	}

	/**
	 * Sets up all HOI4 child directories based on the provided HOI4 path.
	 * 
	 * @param hoi4Path Path to the HOI4 directory
	 * @return true if the directory is valid and paths were set up, false otherwise
	 */
	public static boolean setHoi4PathChildDirs(String hoi4Path) {
		if (!validateDirectoryPath(hoi4Path, "hoi4.path")) {
			return false;
		}
		
		HOI4.folder = new File(hoi4Path);
		HOI4.localization_folder = new File(hoi4Path, "localisation\\english");
		HOI4.units_folder = new File(hoi4Path, "common\\units");
		HOI4.interface_folder = new File(hoi4Path, "interface");
		HOI4.resources_file = new File(hoi4Path, "common\\resources\\00_resources.txt");
		HOI4.state_category_dir = new File(hoi4Path, "common\\state_category");
		HOI4.country_folder = new File(hoi4Path, "history\\countries");
		HOI4.country_tags_folder = new File(hoi4Path, "common\\country_tags");
		HOI4.province_map_file = new File(hoi4Path, "map\\provinces.bmp");
		HOI4.definition_csv_file = new File(hoi4Path, "map\\definition.csv");
		
		return true;
	}

	/**
	 * Sets up both mod and HOI4 file paths based on the provided paths.
	 *
	 * @param modPath Path to the mod directory
	 * @param hoi4Path Path to the HOI4 directory
	 * @param changeNotifier Optional notifier to call after paths are updated
	 * @return true if both paths are valid and set up correctly, false otherwise
	 */
	public static boolean setupFilePaths(String modPath, String hoi4Path, Runnable changeNotifier) {
		boolean success = true;
		
		if (!setHoi4PathChildDirs(hoi4Path)) {
			success = false;
		}
		
		if (!setModPathChildDirs(modPath)) {
			success = false;
		}
		
		if (changeNotifier != null) {
			changeNotifier.run();
		}
		
		return success;
	}

	// A sample validation method that uses both mod and base files
	public static boolean isUnitsFolderValid() {
		return isValidDirectory(Mod.units_folder) && isValidDirectory(HOI4.units_folder);
	}

	/** Checks if a directory is valid */
	private static boolean isValidDirectory(File folder) {
		return folder != null && folder.exists() && folder.isDirectory();
	}
	
	/** Validates whether the provided directory path is valid */
	public static boolean validateDirectoryPath(String path, String keyName) {
		if (path == null || path.isEmpty()) {
			LOGGER.error("{} is null or empty!", keyName);
			return false;
		}

		File directory = new File(path);

		if (!directory.exists() || !directory.isDirectory()) {
			LOGGER.error("{} does not point to a valid directory: {}", keyName, path);
			return false;
		}

		return true;
	}
}
