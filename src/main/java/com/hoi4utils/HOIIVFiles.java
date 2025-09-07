package com.hoi4utils;

import scala.Option;

import java.io.File;

/**
 * TODO: Convert to scala
 */
public class HOIIVFiles {

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

	public static void setModPathChildDirs(String modPath) {
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
	}

	public static void setHoi4PathChildDirs(String hoi4Path) {
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
	}

	// A sample validation method that uses both mod and base files
	public static boolean isUnitsFolderValid() {
		return isValidDirectory(Mod.units_folder) && isValidDirectory(HOI4.units_folder);
	}

	/** Checks if a directory is valid */
	private static boolean isValidDirectory(File folder) {
		return folder != null && folder.exists() && folder.isDirectory();
	}
	
}
