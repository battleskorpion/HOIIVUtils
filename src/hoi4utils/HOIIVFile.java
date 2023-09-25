package hoi4utils;

import hoi4utils.clausewitz_coding.state.State;
import hoi4utils.fileIO.FileListener.FileAdapter;
import hoi4utils.fileIO.FileListener.FileEvent;
import hoi4utils.fileIO.FileListener.FileWatcher;
import ui.message.MessageController;

import java.awt.*;
import java.io.File;
import java.io.IOException;

import static hoi4utils.Settings.MOD_PATH;

/**
 * HOIIV File
 * A Everything to do with the files that are found in a HOI 4 mod
 * - Paths
 * - Creating Files and Directories
 * ? <insert about whatever the file watchers do> chris learning what is a file watcher
 * - Future things
 */
public class HOIIVFile implements FileUtils {

	public static final File usersParadoxHOIIVModFolder = new File(File.separator + "Paradox Interactive" + File.separator + "Hearts of Iron IV" + File.separator + "mod");

	public static final String modPath = SettingsManager.get(MOD_PATH);

	public static FileWatcher stateFilesWatcher;

	public static File focus_folder;
	public static File states_folder;
	public static File strat_region_dir;
	public static File localization_eng_folder;
	public static File common_folder;
	public static File hoi4mods_folder;


	public static void createHOIIVFilePaths() {
		System.out.println("modPath: " + modPath);

		if (modPath == null) {
			MessageController window = new MessageController();
            window.open("Error: modPath was null and we can't create any files");
            return;
		}

		File modPathFile = new File(modPath);
		System.out.println("HOIIVFile created a File for 'modPath' called modPathFile");

		common_folder = new File(modPath + "\\common");
		System.out.println("HOIIVFile created a File for 'modPath\\common' called 'common_folder'");
		states_folder = new File(modPath + "\\history\\states");
		System.out.println("HOIIVFile created a File for 'modPath\\history\\states' called 'states_folder'");
		strat_region_dir =  new File(modPath + "\\map\\strategicregions");
		System.out.println("HOIIVFile created a File for 'modPath\\map\\strategicregions' called 'strat_region_dir'");
		localization_eng_folder =  new File(modPath + "\\localisation\\english");
		System.out.println("HOIIVFile created a File for 'modPath\\localisation\\english' called 'localization_eng_folder'");
		focus_folder = new File(modPath + "\\common\\national_focus");
		System.out.println("HOIIVFile created a File for 'modPath \\common\\national_focus' called 'focus_folder'");
		hoi4mods_folder = modPathFile.getParentFile();
		System.out.println("HOIIVFile gets the parent file for modPath called hoi4mods_folder. Parent file is called: \n" + hoi4mods_folder);
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
//				System.out.println("State created in states dir");
				// todo building view thing
				EventQueue.invokeLater(() -> {
					stateFilesWatcher.listenerPerformAction++;
					File file = event.getFile();
					State.readState(file);
					stateFilesWatcher.listenerPerformAction--;
					if (Settings.DEV_MODE.enabled()) {
						State state = State.get(file);
						System.out.println("State was created/loaded: " + state);
					}
				});
			}

			@Override
			public void onModified(FileEvent event) {
//				System.out.println("State modified in states dir");
				// todo building view thing
				EventQueue.invokeLater(() -> {
					stateFilesWatcher.listenerPerformAction++;
					File file = event.getFile();
					State.readState(file);
					if (Settings.DEV_MODE.enabled()) {
						State state = State.get(file);
						System.out.println("State was modified: " + state);
					}
					stateFilesWatcher.listenerPerformAction--;
				});
			}

			@Override
			public void onDeleted(FileEvent event) {
//				System.out.println("State deleted in states dir");
				// todo building view thing
				EventQueue.invokeLater(() -> {
					stateFilesWatcher.listenerPerformAction++;
					File file = event.getFile();
					State.deleteState(file);
					stateFilesWatcher.listenerPerformAction--;
					if (Settings.DEV_MODE.enabled()) {
						State state = State.get(file);
						System.out.println("State was deleted: " + state);
					}
				});
			}
		}).watch();
	}

}
