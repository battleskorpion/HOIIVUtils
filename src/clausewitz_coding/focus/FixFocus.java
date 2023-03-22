package clausewitz_coding.focus;

import clausewitz_coding.country.CountryTags;
import clausewitz_coding.HOI4Fixes;
import ui.focus_localization.FocusTreeLocProgress;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;

import static settings.LocalizerSettings.Settings.MOD_DIRECTORY;

public class FixFocus extends HOI4Fixes {

	private static String hoi4_dir_name;

	public static boolean addFocusLoc(File focus_file, File loc_file) throws IOException {

		hoi4_dir_name = HOI4Fixes.settings.get(MOD_DIRECTORY);

		// some vars
		ArrayList<String> focuses_localized = new ArrayList<String>();
		//ArrayList<String> focuses_nonlocalized = new ArrayList<String>();
		System.out.println(hoi4_dir_name);
		String loc_key = ":0";
		FocusTree focusTree = new FocusTree(focus_file);

		/* open the ui */
		FocusTreeLocProgress focusLocProgress = new FocusTreeLocProgress(focusTree);
		focusLocProgress.setVisible(true);

		// more vars
		Scanner locReader = new Scanner(loc_file);
		
		// make a list of every localized focus
		boolean found_lang = false; 
		while(!found_lang) {
			if (locReader.hasNextLine()) {
				String data = locReader.nextLine().replaceAll("\\s", "");
				if (usefulData(data)) {
					if (data.trim().contains("l_")) {
						found_lang = true; 
					}
				}
			}
			else {
				break; 
			}
		}
		focusLocProgress.incrementProgressBar();

		/* list of localized focuses */
		while (locReader.hasNextLine()) {
			String data = locReader.nextLine().replaceAll("\\s", ""); 
			if (usefulData(data)) {
				if (data.contains(":")) {
					if (focusTree.list().contains(data.substring(0, data.indexOf(":")))) {
						focuses_localized.add(data.substring(0, data.indexOf(":")).trim()); 
					}
				}
			}
		}
		locReader.close();
		focusLocProgress.incrementProgressBar();
		
		// do stuff with nonlocalized focuses
		// some vars
		FileWriter locWriter = new FileWriter(loc_file, true);		// true = append
		BufferedWriter locBWriter = new BufferedWriter(locWriter); 
		PrintWriter locPWriter = new PrintWriter(locBWriter); 		        // for println syntax
		String focus_loc; 
		//ArrayList<String> focus_loc_array; 
		focusLocProgress.incrementProgressBar();

		int numFocusesUnloc = 0;
		ArrayList<Focus> focusesUnloc = new ArrayList<>();
		assert focusTree.list() != null;
		for (Focus focus : focusTree.focuses())
		{
			// if focus not in localized focuses
			if (!focuses_localized.contains(focus.id()))
			{ 
				// write to loc file 
				// separate words in focus name
				int i = 0;	//counter
				if (CountryTags.exists(focus.id().substring(0, 3)))
				{
					i += 3;
				}

				// localize focus name
				focus_loc = titleCapitalize(focus.id().substring(i, focus.id().length()).replaceAll("_+", " ").trim()); // regex
				locPWriter.println("");
				locPWriter.print("    " + focus + loc_key + " "); 									// NO TAB, YML PREFERS SPACES
				locPWriter.print("\"" + focus_loc + "\"" + "\n");
				// set focus loc
				focus.setFocusLoc(focus_loc);

				// add blank desc line: 
				locPWriter.println("    " + focus + "_desc" + loc_key + " " + "\"" + "\""); // NO TAB, YML PREFERS SPACES
				System.out.println("added focus to loc, focus " + focus_loc);
				numFocusesUnloc++;
				focusesUnloc.add(focus);
			}
		}
		locPWriter.close();
		/* ui */
		focusLocProgress.incrementProgressBar();
		focusLocProgress.setNumFocusesUnloc(numFocusesUnloc);
		focusLocProgress.refreshUnlocFocusesTable(focusesUnloc);

		return true; 
	}

	// useful lines function

}
