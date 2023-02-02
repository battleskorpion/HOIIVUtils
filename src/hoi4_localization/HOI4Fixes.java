package hoi4_localization;

import hoi4_localization.focus.*;
import hoi4_localization.focus.localization.FocusLocReqFinder;
import hoi4_localization.ui.focus_localization.FocusLoqReqWindow;
import hoi4_localization.idea.*;
import settings.LocalizationSettings;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import static settings.LocalizationSettings.Settings.*;

public class HOI4Fixes {

	public static final String applicationVersion = "2.2";
	public static String hoi4_dir_name;
	// "C:\\Users\\daria\\Documents\\Paradox Interactive\\Hearts of Iron IV\\mod\\nadivided-dev";
	public static LocalizationSettings settings;

	public static void main (String[] args) throws IOException {
		File hoi4_dir;
		File states_dir;
		File strat_region_dir;
		File localization_eng_dir;
		File focus_dir;

		/* load settings */
		settings = new LocalizationSettings(); 	// loads settings automatically

		if (settings.isNull(MOD_DIRECTORY))
		/* get directories */
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
			settings.saveSettings(MOD_DIRECTORY, hoi4_dir_name);
		}

		System.out.println(settings.get(MOD_DIRECTORY));
		hoi4_dir = new File(settings.get(MOD_DIRECTORY));
		states_dir = new File(settings.get(MOD_DIRECTORY) + "\\\\history\\\\states");
		strat_region_dir = new File(settings.get(MOD_DIRECTORY) + "\\\\map\\\\strategicregions");
		localization_eng_dir = new File(settings.get(MOD_DIRECTORY) + "\\localisation\\english");
		focus_dir = new File(settings.get(MOD_DIRECTORY) + "\\common\\national_focus");

		/* select focus localization */
		String[] loc_options = {"Fix Focus Localization", "Find Focuses without Localization", "Find Idea Localization", "Placeholder :)"};
		String selection = (String) JOptionPane.showInputDialog(null, "Localization Program Options",
				"Hoi4 Fixes " + applicationVersion, JOptionPane.QUESTION_MESSAGE, null, loc_options, loc_options[0]);

		if (selection.equals(loc_options[0])) {
			File focus_file;
			File loc_file;

			/* focus file */
			{
				JFileChooser j = new JFileChooser(focus_dir);
				j.setFileSelectionMode(JFileChooser.FILES_ONLY);
				j.setDialogTitle("Select Focus File");
				int opt = j.showOpenDialog(null);
				focus_file = j.getSelectedFile();
			}

			/* loc file */
			{
				JFileChooser j = new JFileChooser(localization_eng_dir);
				j.setFileSelectionMode(JFileChooser.FILES_ONLY);
				j.setDialogTitle("Select Localization File");
				int opt = j.showOpenDialog(null);
				loc_file = j.getSelectedFile();
			}

			FixFocus.addFocusLoc(focus_file, loc_file);
		}

		else if (selection.equals(loc_options[1])) {
			FocusLocReqFinder.findLocReqFocuses(hoi4_dir);

			// create window here displaying focus trees missing loc file,
			// and focus trees partially localized
			FocusLoqReqWindow window = new FocusLoqReqWindow();
			window.setVisible(true);
		}

		else if (selection.equals(loc_options[2])) {
			File idea_file;
			File loc_file;

			/* focus file */
			{
				JFileChooser j = new JFileChooser(hoi4_dir);
				j.setFileSelectionMode(JFileChooser.FILES_ONLY);
				j.setDialogTitle("Select Idea File");
				Integer opt = j.showOpenDialog(null);
				idea_file = j.getSelectedFile();
			}

			/* loc file */
			{
				JFileChooser j = new JFileChooser(localization_eng_dir);
				j.setFileSelectionMode(JFileChooser.FILES_ONLY);
				j.setDialogTitle("Select Localization File");
				Integer opt = j.showOpenDialog(null);
				loc_file = j.getSelectedFile();
			}

			FixIdea.addIdeaLoc(idea_file, loc_file);
		}

	}

	protected static boolean usefulData(String data) {
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
	protected static String titleCapitalize(String str) {
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

