package hoi4utils;

import static hoi4utils.Settings.MOD_PATH;

import java.awt.EventQueue;
import java.io.File;
import java.io.IOException;

import hoi4utils.clausewitz_coding.state.State;
import hoi4utils.fileIO.FileListener.FileAdapter;
import hoi4utils.fileIO.FileListener.FileEvent;
import hoi4utils.fileIO.FileListener.FileWatcher;

/**
 * HOIIV File
 * A Everything to do with the files that are found in a HOI 4 mod
 * - Paths
 * - Creating Files and Directories
 * ? <insert about whatever the file watchers do> chris learning what is a file watcher
 * - Future things
 */
public class HOIIVFile {

	public static final File usersHome = new File(System.getProperty("user.home"));
	public static final File usersDocuments = new File(usersHome + File.separator + "Documents");
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
		if (Settings.DEV_MODE.enabled()) {
			System.out.println(modPath);
		}

		File modPathFile = new File(modPath);

		common_folder = new File(modPath + "\\common");
		states_folder = new File(modPath + "\\history\\states");
		strat_region_dir =  new File(modPath + "\\map\\strategicregions");
		localization_eng_folder =  new File(modPath + "\\localisation\\english");
		focus_folder = new File(modPath + "\\common\\national_focus");
		hoi4mods_folder = modPathFile.getParentFile();
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

	/**
	 * 
	 * @param data
	 * @return
	 */
	public static boolean usefulData(String data) {
		if (data.isEmpty()) {
			return false;
		}
	
		return data.trim().charAt(0) != '#';
	}
}