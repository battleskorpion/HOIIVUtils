package hoi4utils;

import hoi4utils.fileIO.FileListener.FileAdapter;
import hoi4utils.fileIO.FileListener.FileEvent;
import hoi4utils.fileIO.FileListener.FileWatcher;
import javafx.collections.ObservableList;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Button;
import javafx.stage.Screen;
import javafx.stage.Stage;
import ui.main_menu.SettingsWindow;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import static hoi4utils.Settings.PREFERRED_SCREEN;

/*
* HOIIVUtils File
*/
public class HOIIVUtils {
	public static final String hoi4utilsVersion = "version 0.2.14";
	
	public static String hoi4_dir_name;
	public static File focus_folder;
	public static File states_folder;
	public static File strat_region_dir;
	public static File localization_eng_folder;

	public static String[] args;

	public static boolean firstTimeSetup;

	private static SettingsWindow settingsWindow;
	
	public static SettingsManager settings;

	public static boolean DEV_MODE = false;

	public static FileWatcher stateDirWatcher;
	
	public static void main(String[] args) throws IOException {
		
		HOIIVUtils.args = args;

		/* hoi4utils.settings */
		try {
			settingsWindow = new SettingsWindow();

			settingsWindow.launchSettingsWindow(args);
		}
		catch (Exception exception) {
			HOIIVUtils.openError(exception);
		}

		/* init 
		StateCategory.loadStateCategories();
		Interface.loadGFX();*/

		/* main listeners */
		try {

			watchStateFiles(new File(hoi4_dir_name + states_folder));
		} catch (NullPointerException exc) {

			exc.printStackTrace();
			return;
		}

	}

	public static void openError(Exception exception) {
		if (Settings.DEV_MODE.enabled()) {
			exception.printStackTrace();
		}
		JOptionPane.showMessageDialog(null, exception, "ln: " + exception.getStackTrace()[0].getLineNumber(), JOptionPane.WARNING_MESSAGE);
	}

	public static void openError(String s) {
		if (Settings.DEV_MODE.enabled()) {
			System.err.println("open error window for error message: " + s);
		}
		JOptionPane.showMessageDialog(null, s, "HOIIVUtils Error Message", JOptionPane.WARNING_MESSAGE);
	}

	public static void closeWindow(Button button) {
		try {
			((Stage) (button.getScene().getWindow())).close();
		}
		catch(Exception exception) {
			openError(exception);
		}
	}

	public static void hideWindow(Button button) {
		try {
			((Stage) (button.getScene().getWindow())).hide();
		}
		catch(Exception exception) {
			openError(exception);
		}
	}

	public static void decideScreen(Stage primaryStage) {
		Integer preferredScreen = (Integer) PREFERRED_SCREEN.getSetting();
		ObservableList<Screen> screens = Screen.getScreens();
		if (preferredScreen > screens.size()) {
			if (Settings.DEV_MODE.enabled()) {
				System.err.println( "Preferred screen does not exist, resorting to defaults.");
			}
			return;
		}
		Screen screen = screens.get(preferredScreen);
		if (screen == null) {
			if (Settings.DEV_MODE.enabled()) {
				System.err.println( "Preferred screen is null error, resorting to defaults.");
			}
			return;
		}
		Rectangle2D bounds = screen.getVisualBounds();
		primaryStage.setX(bounds.getMinX() + 200);
		primaryStage.setY(bounds.getMinY() + 200);
	}

/*		public static void closeSettings() {
		try {
			settingsWindow.closeSettingsWindow();
		}
		catch(NullPointerException exception) {
			openError();
		}
	}*/

	public static boolean usefulData(String data) {
		if (!data.isEmpty()) {
			if (data.trim().charAt(0) == '#') {
				return false;
			} else {
				return true;
			}
		} else {
			return false;
		}
	}

	// for capitalizing
	public static String titleCapitalize(String str) {
		// some vars
		ArrayList<String> words = new ArrayList<String>(Arrays.asList(str.split(" ")));
		ArrayList<String> whitelist = new ArrayList<String>();

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

		// first word always capitalized
		if (words.get(0).length() == 1) {
			words.set(0, "" + Character.toUpperCase(words.get(0).charAt(0)));
		} else if (words.get(0).length() > 1) {
			words.set(0, "" + Character.toUpperCase(words.get(0).charAt(0))
					+ words.get(0).substring(1));
		} else {
			System.out.println("first word length < 1");
		}

		// rest of words (if applicable)
		int num_cap_letters;
		System.out.println("num words: " + words.size());
		for (int i = 1; i < words.size(); i++) {
			// check for acronym (all caps already)
			num_cap_letters = 0;
			for (int j = 0; j < words.get(i).length(); j++) {
				if (Character.isUpperCase(words.get(i).charAt(j))) {
					System.out.println("uppercase: " + words.get(i).charAt(j));
					num_cap_letters++;
				}
			}

			// if not acronym (acronym = all caps already)
			// && not on whitelist
			if (!(num_cap_letters == words.get(i).length()) && !(whitelist.contains(words.get(i)))) {
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

	private static void watchStateFiles(File stateDir) throws IOException {
		if (stateDir == null || !stateDir.exists() || !stateDir.isDirectory()) {
			System.err.println("State dir does not exist or is not a directory: " + stateDir);
			return;
		}

		// WatchService watchService;
		// watchService = FileSystems.getDefault().newWatchService();
		//
		// Path path = Paths.get(stateDir.getPath());
		// path.register(watchService, ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE);

		stateDirWatcher = new FileWatcher(stateDir);
		stateDirWatcher.addListener(new FileAdapter() {
			@Override
			public void onCreated(FileEvent event) {
//				System.out.println("State created in states dir");
				// todo building view thing
//				EventQueue.invokeLater(() -> {
//					stateDirWatcher.listenerPerformAction++;
//					File file = event.getFile();
//					State.readState(file);
//					stateDirWatcher.listenerPerformAction--;
//				});
			}

			@Override
			public void onModified(FileEvent event) {
//				System.out.println("State modified in states dir");
				// todo building view thing
//				EventQueue.invokeLater(() -> {
//					stateDirWatcher.listenerPerformAction++;
//					File file = event.getFile();
//					State.readState(file);
//					System.out.println(State.get(file).getStateInfrastructure().population());
//					System.out.println(State.infrastructureOfStates(State.listFromCountry(new CountryTag("SMA"))));
//					stateDirWatcher.listenerPerformAction--;
//				});
			}

			@Override
			public void onDeleted(FileEvent event) {
//				System.out.println("State deleted in states dir");
				// todo building view thing
//				EventQueue.invokeLater(() -> {
//					stateDirWatcher.listenerPerformAction++;
//					File file = event.getFile();
//					State.deleteState(file);
//					stateDirWatcher.listenerPerformAction--;
//				});
			}
		}).watch();
	}

}
