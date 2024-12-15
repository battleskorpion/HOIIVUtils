package com.hoi4utils.clausewitz;

import com.hoi4utils.FileUtils;
import com.hoi4utils.PublicFieldChangeNotifier;
import com.hoi4utils.fileIO.FileListener.FileAdapter;
import com.hoi4utils.fileIO.FileListener.FileEvent;
import com.hoi4utils.fileIO.FileListener.FileWatcher;
import com.hoi4utils.clausewitz.map.state.State;
import com.hoi4utils.ui.message.MessageController;

import java.awt.*;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;

/**
 * HOIIV File A Everything to do with the files that are found in a HOI 4 mod - Paths - Creating
 * Files and Directories ? <insert about whatever the file watchers do> chris learning what is a
 * file watcher - Future things
 */
public class HOIIVFile implements FileUtils {
	public static final File usersParadoxHOIIVModFolder =
			new File(File.separator + "Paradox Interactive" + File.separator + "Hearts of Iron IV" + File.separator + "mod");

	@SuppressWarnings("exports")
	public static FileWatcher stateFilesWatcher;

	public static File mod_folder;
	public static File mod_focus_folder;
	public static File mod_ideas_folder;
	public static File mod_states_folder;
	public static File mod_strat_region_dir;
	public static File mod_localization_folder;
	public static File hoi4_localization_folder;
	public static File mod_common_folder;
	public static File hoi4mods_folder;
	public static File mod_units_folder;
	public static File hoi4_units_folder;
	public static final String mod_folder_field_name = "mod_folder";
	public static final String mod_focus_folder_field_name = "mod_focus_folder";
	public static final String mod_ideas_folder_field_name = "mod_ideas_folder";
	public static final String mod_states_folder_field_name = "mod_states_folder";
	public static final String mod_strat_region_dir_field_name = "mod_strat_region_dir";
	public static final String mod_localization_folder_field_name = "mod_localization_folder";
	public static final String hoi4_localization_folder_field_name = "hoi4_localization_folder";
	public static final String mod_common_folder_field_name = "mod_common_folder";
	public static final String hoi4mods_folder_field_name = "hoi4mods_folder";
	public static final String mod_units_folder_field_name = "mod_units_folder";
	public static final String hoi4_units_folder_field_name = "hoi4_units_folder";

	private static final PublicFieldChangeNotifier changeNotifier = new PublicFieldChangeNotifier(HOIIVFile.class);

	/**
	 * TODO: Make Demo
	 */
    public static void createHOIIVFilePaths() {

		String modPath = HOIIVUtils.get("mod.path");
		String hoi4Path = HOIIVUtils.get("hoi4.path");
		System.out.println("modPath: " + modPath);
		System.out.println("hoi4Path: " + hoi4Path);

		if (modPath == null) {
			System.err.println("mod path is null!!!!!!!!");
			return;
		}

		File modPathFile = new File(modPath);

		mod_folder = new File(modPath);
		mod_common_folder = new File(modPath + "\\common");
		mod_states_folder = new File(modPath + "\\history\\states");
		mod_strat_region_dir = new File(modPath + "\\map\\strategicregions");
		mod_localization_folder = new File(modPath + "\\localisation\\english");
		hoi4_localization_folder = new File(hoi4Path + "\\localisation\\english");
		mod_focus_folder = new File(modPath + "\\common\\national_focus");
		mod_ideas_folder = new File(modPath + "\\common\\ideas");
		mod_units_folder = new File(modPath + "\\common\\units");
		hoi4_units_folder = new File(hoi4Path + "\\common\\units");
		hoi4mods_folder = modPathFile.getParentFile();
		System.out.println("HOIIVFile created path objects");

		// Check for changes after setting the fields
		changeNotifier.checkAndNotifyChanges();
	}

	/**
	 * Watch the state files in the given directory.
	 *
	 * @param stateFiles The directory containing state files.
	 * @throws IOException If an I/O error occurs.
	 */
	public static void watchStateFiles(File stateFiles) {
		if (stateFiles == null || !stateFiles.exists() || !stateFiles.isDirectory()) {
			System.err.println("State dir does not exist or is not a directory: " + stateFiles);
			return;
		}

		stateFilesWatcher = new FileWatcher(stateFiles);

		stateFilesWatcher.addListener(new FileAdapter() {
			@Override
			public void onCreated(FileEvent event) {
				EventQueue.invokeLater(() -> {
					stateFilesWatcher.listenerPerformAction++;
					File file = event.getFile();
					State.readState(file);
					stateFilesWatcher.listenerPerformAction--;
					if (HOIIVUtils.getBoolean("dev.mode.enabled")) {
						State state = State.get(file);
						System.out.println("State was created/loaded: " + state);
					}
				});
			}

			@Override
			public void onModified(FileEvent event) {
				EventQueue.invokeLater(() -> {
					stateFilesWatcher.listenerPerformAction++;
					File file = event.getFile();
					State.readState(file);
					if (HOIIVUtils.getBoolean("dev.mode.enabled")) {
						State state = State.get(file);
						System.out.println("State was modified: " + state);
					}
					stateFilesWatcher.listenerPerformAction--;
				});
			}

			@Override
			public void onDeleted(FileEvent event) {
				EventQueue.invokeLater(() -> {
					stateFilesWatcher.listenerPerformAction++;
					File file = event.getFile();
					State.deleteState(file);
					stateFilesWatcher.listenerPerformAction--;
					if (HOIIVUtils.getBoolean("dev.mode.enabled")) {
						State state = State.get(file);
						System.out.println("State was deleted: " + state);
					}
				});
			}
		}).watch();
	}

	public static void addPropertyChangeListener(PropertyChangeListener listener) {
		changeNotifier.addPropertyChangeListener(listener);
	}

	public static void removePropertyChangeListener(PropertyChangeListener listener) {
		changeNotifier.removePropertyChangeListener(listener);
	}
}
