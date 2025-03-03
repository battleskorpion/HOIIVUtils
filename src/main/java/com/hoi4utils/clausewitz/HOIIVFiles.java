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

		public static final String folder_field_name = "mod_folder";
		public static final String focus_folder_field_name = "mod_focus_folder";
		public static final String ideas_folder_field_name = "mod_ideas_folder";
		public static final String states_folder_field_name = "mod_states_folder";
		public static final String strat_region_dir_field_name = "mod_strat_region_dir";
		public static final String localization_folder_field_name = "mod_localization_folder";
		public static final String common_folder_field_name = "mod_common_folder";
		public static final String units_folder_field_name = "mod_units_folder";
		public static final String interface_folder_field_name = "mod_interface_folder";
	}

	// Nested class for base (HOIIV) files
	public static class HOI4 {
		public static File folder;
		public static File localization_folder;
		public static File units_folder;
		public static File interface_folder;

		public static final String localization_folder_field_name = "hoi4_localization_folder";
		public static final String mods_folder_field_name = "hoi4mods_folder";
		public static final String units_folder_field_name = "hoi4_units_folder";
		public static final String interface_folder_field_name = "hoi4_interface_folder";
	}

	// Example of a file that might be shared or used as a default mod folder
	public static final File usersParadoxHOIIVModFolder =
			new File(File.separator + "Paradox Interactive" + File.separator + "Hearts of Iron IV" + File.separator + "mod");

	public static void setModPathChildDirs(String modPath) {
		Mod.folder = null;
		Mod.common_folder = null;
		Mod.focus_folder = null;
		Mod.ideas_folder = null;
		Mod.units_folder = null;
		Mod.states_folder = null;
		Mod.localization_folder = null;
		Mod.strat_region_dir = null;
		
		Mod.folder = new File(modPath);
		Mod.common_folder = new File(modPath, "common");
		Mod.focus_folder = new File(HOIIVFiles.Mod.common_folder, "national_focus");
		Mod.ideas_folder = new File(HOIIVFiles.Mod.common_folder, "ideas");
		Mod.units_folder = new File(HOIIVFiles.Mod.common_folder, "units");
		Mod.states_folder = new File(modPath, "history\\states");
		Mod.localization_folder = new File(modPath, "localisation\\english"); // 's' vs 'z' note in the original comment
		Mod.strat_region_dir = new File(modPath, "map\\strategicregions");
		Mod.interface_folder = new File(modPath, "interface");
	}

	public static void setHoi4PathChildDirs(String hoi4Path) {
		HOI4.folder = null;
		HOI4.localization_folder = null;
		HOI4.units_folder = null;
		HOI4.interface_folder = null;
		
		HOI4.folder = new File(hoi4Path);
		HOI4.localization_folder = new File(hoi4Path, "localisation\\english");
		HOI4.units_folder = new File(hoi4Path, "common\\units");
		HOI4.interface_folder = new File(hoi4Path, "interface");
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
