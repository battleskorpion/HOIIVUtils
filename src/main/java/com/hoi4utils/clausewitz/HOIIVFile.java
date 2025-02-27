package com.hoi4utils.clausewitz;

import com.hoi4utils.FileUtils;
import com.hoi4utils.PublicFieldChangeNotifier;
import com.hoi4utils.clausewitz.map.state.State;
import com.hoi4utils.fileIO.FileListener.FileAdapter;
import com.hoi4utils.fileIO.FileListener.FileEvent;
import com.hoi4utils.fileIO.FileListener.FileWatcher;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.*;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.Optional;

/**
 * HOIIVFile
 * Holds onto static object files.
 */
public class HOIIVFile implements FileUtils {
	private static final Logger LOGGER = LogManager.getLogger(HOIIVFile.class);

	public static final File usersParadoxHOIIVModFolder =
			new File(File.separator + "Paradox Interactive" + File.separator + "Hearts of Iron IV" + File.separator + "mod");

	@SuppressWarnings("exports")
	public static FileWatcher stateFilesWatcher;

	public static File mod_folder, mod_focus_folder, mod_ideas_folder, mod_states_folder;
	public static File mod_strat_region_dir, mod_localization_folder, mod_common_folder, mod_units_folder;
	public static File hoi4_folder, hoi4_localization_folder, hoi4_units_folder;

	private static final PublicFieldChangeNotifier changeNotifier = new PublicFieldChangeNotifier(HOIIVFile.class);

	/** Initializes file paths for the mod and HOI4 directories */
	public static void createHOIIVFilePaths() {
		createModPaths();
		createHOIIVPaths();
		changeNotifier.checkAndNotifyChanges();
	}

	private static void createModPaths() {
		String modPath = HOIIVUtils.get("mod.path");

		if (!validateDirectoryPath(modPath, "mod.path")) {
			return;
		}

		mod_folder = new File(modPath);
		mod_common_folder = new File(modPath, "common");
		mod_focus_folder = new File(mod_common_folder, "national_focus");
		mod_ideas_folder = new File(mod_common_folder, "ideas");
		mod_units_folder = new File(mod_common_folder, "units");
		mod_states_folder = new File(modPath, "history/states");
		mod_localization_folder = new File(modPath, "localisation/english"); // 's' vs 'z' note in the original comment
		mod_strat_region_dir = new File(modPath, "map/strategicregions");
	}

	private static void createHOIIVPaths() {
		String hoi4Path = HOIIVUtils.get("hoi4.path");

		if (!validateDirectoryPath(hoi4Path, "hoi4.path")) {
			return;
		}

		hoi4_folder = new File(hoi4Path);
		hoi4_localization_folder = new File(hoi4Path, "localisation/english");
		hoi4_units_folder = new File(hoi4Path, "common/units");
	}

	/** Validates whether the provided directory path is valid */
	private static boolean validateDirectoryPath(String path, String keyName) {
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

	/**
	 * Watches the state files in the given directory.
	 * @param stateFiles The directory containing state files.
	 */
	public static void watchStateFiles(File stateFiles) {
		if (!validateDirectoryPath(Optional.ofNullable(stateFiles).map(File::getPath).orElse(null), "State files directory")) {
			return;
		}

		stateFilesWatcher = new FileWatcher(stateFiles);

		stateFilesWatcher.addListener(new FileAdapter() {
			@Override
			public void onCreated(FileEvent event) {
				handleStateFileEvent(event, "created/loaded", State::readState);
			}

			@Override
			public void onModified(FileEvent event) {
				handleStateFileEvent(event, "modified", State::readState);
			}

			@Override
			public void onDeleted(FileEvent event) {
				handleStateFileEvent(event, "deleted", State::deleteState);
			}
		}).watch();
	}

	/**
	 * Handles state file events.
	 * @param event File event that occurred.
	 * @param actionName Name of the action performed.
	 * @param stateAction Function to apply to the file.
	 */
	private static void handleStateFileEvent(FileEvent event, String actionName, java.util.function.Consumer<File> stateAction) {
		EventQueue.invokeLater(() -> {
			stateFilesWatcher.listenerPerformAction++;
			File file = event.getFile();
			stateAction.accept(file);
			stateFilesWatcher.listenerPerformAction--;
			LOGGER.debug("State was {}: {}", actionName, State.get(file));
		});
	}

	public static void addPropertyChangeListener(PropertyChangeListener listener) {
		changeNotifier.addPropertyChangeListener(listener);
	}

	public static void removePropertyChangeListener(PropertyChangeListener listener) {
		changeNotifier.removePropertyChangeListener(listener);
	}

	public static boolean isUnitsFolderValid() {
		return isValidDirectory(mod_units_folder) && isValidDirectory(hoi4_units_folder);
	}

	/** Checks if a directory is valid */
	private static boolean isValidDirectory(File folder) {
		return folder != null && folder.exists() && folder.isDirectory();
	}
}
