package clausewitz_coding.focus;

import clausewitz_coding.country.CountryTags;
import clausewitz_coding.HOI4Fixes;
import clausewitz_coding.localization.FocusLocalizationFile;
//import clausewitz_coding.localization.LocalizationFile;
import ui.focus_localization.FocusTreeLocProgress;

//import java.io.BufferedWriter;
import java.io.File;
//import java.io.FileWriter;
import java.io.IOException;
//import java.io.PrintWriter;
//import java.time.LocalDate;
import java.time.LocalDateTime;
//import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
//import java.util.Scanner;

import static settings.HOIIVUtilsProperties.Settings.MOD_DIRECTORY;

public class FixFocus extends HOI4Fixes {

//	private static String hoi4_dir_name;

	public static boolean addFocusLoc(File focus_file, File loc_file) throws IOException {

		final String hoi4_dir_name = HOI4Fixes.settings.get(MOD_DIRECTORY);

		// some vars
		ArrayList<String> focuses_localized = new ArrayList<String>();
		//ArrayList<String> focuses_nonlocalized = new ArrayList<String>();
		FocusTree focusTree = new FocusTree(focus_file);
		FocusLocalizationFile localization = new FocusLocalizationFile(loc_file);
		localization.readLocalization();

		/* open the ui */
		FocusTreeLocProgress focusLocProgress = new FocusTreeLocProgress(focusTree);
		focusLocProgress.setVisible(true);

		String focus_loc;

		int numFocusesUnloc = 0;
		ArrayList<Focus> focusesUnloc = new ArrayList<>();
		assert focusTree.list() != null;
		for (Focus focus : focusTree.focuses())
		{
			// if focus id not localized
			if (!localization.isLocalized(focus.id))
			{ 
				// write to loc file 
				// separate words in focus name
				int i = 0;	//counter
				if (CountryTags.exists(focus.id().substring(0, 3))) {
					i += 3;
				}

				// localize focus name
				focus_loc = titleCapitalize(focus.id().substring(i).replaceAll("_+", " ").trim()); // regex

				// set focus loc
				focus.setFocusLoc(focus_loc);

				numFocusesUnloc++;
				focusesUnloc.add(focus);

				localization.setLocalization(focus.id, focus_loc);
				localization.setLocalizationDesc(focus.id + "_desc", "added focus on " + LocalDateTime.now() + " by hoi4localizer.");
			}
		}

		/* ui */
		focusLocProgress.incrementProgressBar();
		focusLocProgress.setNumFocusesUnloc(numFocusesUnloc);
		focusLocProgress.refreshUnlocFocusesTable(focusesUnloc);

		localization.writeLocalization();
		return true; 
	}

	// useful lines function

}
