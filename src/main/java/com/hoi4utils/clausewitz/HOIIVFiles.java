package com.hoi4utils.clausewitz;

import java.io.File;

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

		public static final String folder_field_name = "mod_folder";
		public static final String focus_folder_field_name = "mod_focus_folder";
		public static final String ideas_folder_field_name = "mod_ideas_folder";
		public static final String states_folder_field_name = "mod_states_folder";
		public static final String strat_region_dir_field_name = "mod_strat_region_dir";
		public static final String localization_folder_field_name = "mod_localization_folder";
		public static final String common_folder_field_name = "mod_common_folder";
		public static final String units_folder_field_name = "mod_units_folder";
		public static final String interface_folder_field_name = "mod_interface_folder";
		public static final String resources_folder_field_name = "mod_resources_folder";
		public static final String state_category_dir_field_name = "mod_state_category_dir";
		public static final String country_folder_field_name = "mod_country_folder";
		public static final String country_tags_folder_field_name = "mod_country_tags_folder";
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

		public static final String localization_folder_field_name = "hoi4_localization_folder";
		public static final String mods_folder_field_name = "hoi4mods_folder";
		public static final String units_folder_field_name = "hoi4_units_folder";
		public static final String interface_folder_field_name = "hoi4_interface_folder";
		public static final String resources_folder_field_name = "hoi4_resources_folder";
		public static final String state_category_dir_field_name = "hoi4_state_category_dir";
		public static final String country_folder_field_name = "hoi4_country_folder";
		public static final String country_tags_folder_field_name = "hoi4_country_tags_folder";
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
