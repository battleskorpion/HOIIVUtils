package clausewitz_coding;

import clausewitz_coding.state.StateCategory;
import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import ui.menu.Mainmenu;
import settings.LocalizerSettings;

import javax.swing.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import static settings.LocalizerSettings.Settings.*;

public class HOI4Fixes {

	public static final String applicationVersion = "2.2";
	public static LocalizerSettings settings;
	public static String hoi4_dir_name;
	// "C:\\Users\\daria\\Documents\\Paradox Interactive\\Hearts of Iron IV\\mod\\nadivided-dev";
	public static String focus_folder;
	public static String states_folder;
	public static String strat_region_dir;
	public static String localization_eng_folder;
	public static boolean DEV_MODE = false;

	public static void main (String[] args) throws IOException {
		/* load settings */
		settings = new LocalizerSettings(); 	// loads settings automatically

		/* ui preliminary */
		try {
			//UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			if (DARK_MODE.getMode()) {
				UIManager.setLookAndFeel(new FlatDarkLaf());
			} else {
				UIManager.setLookAndFeel(new FlatLightLaf());
			}
		} catch (Exception exc) {
			exc.printStackTrace();
			System.err.println( "Failed to initialize look and feel" );
		}

		if (LocalizerSettings.isNull(MOD_DIRECTORY))
		/* get directory */
		{
			JFileChooser j = new JFileChooser();
			j.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			j.setDialogTitle("Choose Mod Directory");

			int opt = j.showOpenDialog(null);
			if (opt == JFileChooser.APPROVE_OPTION) {
				hoi4_dir_name = j.getSelectedFile().getPath();
			} else {
				return;
			}

			/* directory acquired, now save settings */
			LocalizerSettings.saveSettings(MOD_DIRECTORY, hoi4_dir_name);
		} else {
			hoi4_dir_name = LocalizerSettings.get(MOD_DIRECTORY);
		}

		System.out.println(LocalizerSettings.get(MOD_DIRECTORY));
		states_folder = "\\history\\states";
		strat_region_dir = "\\map\\strategicregions";
		localization_eng_folder = "\\localisation\\english";
		focus_folder = "\\common\\national_focus";

		/* init */
		StateCategory.loadStateCategories();

		/* main menu */
		//String[] loc_options = {"Fix Focus Localization", "Find Focuses without Localization", "Find Idea Localization", "View Buildings"};
		Mainmenu mainmenuWindow = new Mainmenu();
		mainmenuWindow.setVisible(true);

	}

	public static boolean usefulData(String data) {
		if (!data.isEmpty()) {
			if (data.trim().charAt(0) == '#') {
				return false;
			}
			else {
				return true;
			}
		}
		else {
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
		}
		else if (words.get(0).length() > 1) {
			words.set(0, "" + Character.toUpperCase(words.get(0).charAt(0))
					+ words.get(0).substring(1));
		}
		else {
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
				}
				else if (words.get(i).length() > 1) {
					//System.out.println("working cap");
					words.set(i, "" + Character.toUpperCase(words.get(i).charAt(0))
							+ words.get(i).substring(1));
				}
			}

		}

		System.out.println("capitalized: " + String.join(" ", words));
		return String.join(" ", words);
	}

}

