package hoi4utils;

import hoi4utils.clausewitz_coding.state.State;
import hoi4utils.fileIO.FileListener.FileAdapter;
import hoi4utils.fileIO.FileListener.FileEvent;
import hoi4utils.fileIO.FileListener.FileWatcher;
import ui.main_menu.SettingsWindow;
import ui.main_menu.MenuWindow;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

/*
* HOIIVUtils File
*/
public class HOIIVUtils {

	public static String[] args;
	public static final String hoi4utilsVersion = "Version 0.4.1";
	public static boolean firstTimeSetup;
	public static SettingsWindow settingsWindow;
	public static MenuWindow menuWindow;
	public static SettingsManager settings;
	
	public static boolean DEV_MODE = false;
	public static boolean SKIP_SETTINGS = false;
	
	public static FileWatcher stateDirWatcher;
	//	public static String hoi4_dir_name;
	public static File focus_folder;
	public static File states_folder;
	public static File strat_region_dir;
	public static File localization_eng_folder;
	public static File common_folder;

	public static void main(String[] args) throws RuntimeException,IOException {
		try {
			SettingsManager.getSavedSettings();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	
		if (HOIIVUtils.firstTimeSetup) {
			HOIIVUtils.settingsWindow = new SettingsWindow();
			HOIIVUtils.settingsWindow.launchSettingsWindow(args);
		} else {
			HOIIVFilePaths.createHOIIVFilePaths();
			if (Settings.SKIP_SETTINGS.enabled()) {
				HOIIVUtils.menuWindow = new MenuWindow();
				HOIIVUtils.menuWindow.launchMenuWindow(args);
			} else {
				HOIIVUtils.settingsWindow = new SettingsWindow();
				HOIIVUtils.settingsWindow.launchSettingsWindow(args);
			}
		}
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

	// for capitalizing
	public static String titleCapitalize(String str) {
		if (str == null) {
			return null;
		}
		if (str.trim().isEmpty()) {
			return str;
		}

		// some vars
		ArrayList<String> words = new ArrayList<String>(Arrays.asList(str.split(" ")));
		HashSet<String> whitelist = createCapitalizationWhitelist();

		// first word always capitalized
		if (words.get(0).length() == 1) {
			words.set(0, "" + Character.toUpperCase(words.get(0).charAt(0)));
		} else if (words.get(0).length() > 1) {
			words.set(0, "" + Character.toUpperCase(words.get(0).charAt(0))
					+ words.get(0).substring(1));
		} else {
			// todo this should never happen now right?
			System.out.println("first word length < 1");
		}

		// rest of words (if applicable)
		System.out.println("num words: " + words.size());
		for (int i = 1; i < words.size(); i++) {

			// if not acronym (acronym = all caps already)
			// && not on whitelist
			if (!isAcronym(words.get(i)) && !(whitelist.contains(words.get(i)))) {
				if (words.get(i).length() == 1) {
					words.set(i, "" + Character.toUpperCase(words.get(i).charAt(0)));
				} else if (words.get(i).length() > 1) {
					// System.out.println("working cap");
					words.set(i, "" + Character.toUpperCase(words.get(i).charAt(0))
							+ words.get(i).substring(1));
				}
			}

		}

		System.out.println("capitalized: " + String.join(" ", words));
		return String.join(" ", words);
	}

	/**
	 * @param table
	 * @param row
	 * //todo may be outdated as is for JTable stuff
	 * @return
	 */
	public static int rowToModelIndex(JTable table, int row) {
		if (row >= 0) {
			RowSorter<?> rowSorter = table.getRowSorter();
			return rowSorter != null ? rowSorter
					.convertRowIndexToModel(row) : row;
		}
		return -1;
	}

	// ! todo
	public static void watchStateFiles(File stateDir) throws IOException {
		if (stateDir == null || !stateDir.exists() || !stateDir.isDirectory()) {
			System.err.println("State dir does not exist or is not a directory: " + stateDir);
			return;
		}

		stateDirWatcher = new FileWatcher(stateDir);
		stateDirWatcher.addListener(new FileAdapter() {
			@Override
			public void onCreated(FileEvent event) {
//				System.out.println("State created in states dir");
				// todo building view thing
				EventQueue.invokeLater(() -> {
					stateDirWatcher.listenerPerformAction++;
					File file = event.getFile();
					State.readState(file);
					stateDirWatcher.listenerPerformAction--;
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
					stateDirWatcher.listenerPerformAction++;
					File file = event.getFile();
					State.readState(file);
					if (Settings.DEV_MODE.enabled()) {
						State state = State.get(file);
						System.out.println("State was modified: " + state);
					}
					stateDirWatcher.listenerPerformAction--;
				});
			}

			@Override
			public void onDeleted(FileEvent event) {
//				System.out.println("State deleted in states dir");
				// todo building view thing
				EventQueue.invokeLater(() -> {
					stateDirWatcher.listenerPerformAction++;
					File file = event.getFile();
					State.deleteState(file);
					stateDirWatcher.listenerPerformAction--;
					if (Settings.DEV_MODE.enabled()) {
						State state = State.get(file);
						System.out.println("State was deleted: " + state);
					}
				});
			}
		}).watch();
	}

	private static boolean isAcronym(String word) {
		// check for acronym (all caps already)
		int num_cap_letters = numCapLetters(word);

		return num_cap_letters == word.length();
	}

	private static int numCapLetters(String word) {
		if (word == null) {
			return 0; 
		}

		int num_cap_letters;
		num_cap_letters = 0;
		for (int j = 0; j < word.length(); j++) {
			if (Character.isUpperCase(word.charAt(j))) {
				num_cap_letters++;
			}
		}
		return num_cap_letters;
	}

	private static HashSet<String> createCapitalizationWhitelist() {
		HashSet<String> whitelist = new HashSet<String>();

		// create the whitelist
		whitelist.add("a");
		whitelist.add("above");
		whitelist.add("after");
		whitelist.add("among");
		whitelist.add("an");
		whitelist.add("and");
		whitelist.add("around");
		whitelist.add("as");
		whitelist.add("at");
		whitelist.add("below");
		whitelist.add("beneath");
		whitelist.add("beside");
		whitelist.add("between");
		whitelist.add("but");
		whitelist.add("by");
		whitelist.add("for");
		whitelist.add("from");
		whitelist.add("if");
		whitelist.add("in");
		whitelist.add("into");
		whitelist.add("nor");
		whitelist.add("of");
		whitelist.add("off");
		whitelist.add("on");
		whitelist.add("onto");
		whitelist.add("or");
		whitelist.add("over");
		whitelist.add("since");
		whitelist.add("the");
		whitelist.add("through");
		whitelist.add("throughout");
		whitelist.add("to");
		whitelist.add("under");
		whitelist.add("until");
		whitelist.add("up");
		whitelist.add("with");
		return whitelist;
	}

}
