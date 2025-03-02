package com.hoi4utils.clausewitz;

import java.io.File;

public class HOIIVUtilsFiles {
	public static final File usersParadoxHOIIVModFolder =
		new File(File.separator + "Paradox Interactive" + File.separator + "Hearts of Iron IV" + File.separator + "mod");

	public static File mod_folder;
	public static File mod_focus_folder;
	public static File mod_ideas_folder;
	public static File mod_states_folder;
	public static File mod_strat_region_dir;
	public static File mod_localization_folder;
	public static File mod_common_folder;
	public static File mod_units_folder;
	public static final String mod_folder_field_name = "mod_folder";
	public static final String mod_focus_folder_field_name = "mod_focus_folder";
	public static final String mod_ideas_folder_field_name = "mod_ideas_folder";
	public static final String mod_states_folder_field_name = "mod_states_folder";
	public static final String mod_strat_region_dir_field_name = "mod_strat_region_dir";
	public static final String mod_localization_folder_field_name = "mod_localization_folder";
	public static final String mod_common_folder_field_name = "mod_common_folder";
	public static final String mod_units_folder_field_name = "mod_units_folder";

	public static File hoi4_folder;
	public static File hoi4_localization_folder;
	public static File hoi4_units_folder;
	public static final String hoi4_localization_folder_field_name = "hoi4_localization_folder";
	public static final String hoi4mods_folder_field_name = "hoi4mods_folder";
	public static final String hoi4_units_folder_field_name = "hoi4_units_folder";

	public static boolean isUnitsFolderValid() {
		return isValidDirectory(mod_units_folder) && isValidDirectory(hoi4_units_folder);
	}

	/** Checks if a directory is valid */
	private static boolean isValidDirectory(File folder) {
		return folder != null && folder.exists() && folder.isDirectory();
	}
}
