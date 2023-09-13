package hoi4utils.clausewitz_coding.focus;

import hoi4utils.clausewitz_coding.country.CountryTags;
import hoi4utils.clausewitz_coding.localization.Localization;
import hoi4utils.EnglishSuperDictionary;
import hoi4utils.HOIIVUtils;
import hoi4utils.clausewitz_coding.localization.FocusLocalizationFile;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
/*
 * FixFoucus Documentation
 */
public class FixFocus extends HOIIVUtils {

	public static boolean addFocusLoc(File focus_file, File loc_file) throws IOException {

		//ArrayList<String> focuses_nonlocalized = new ArrayList<String>();
		FocusTree focusTree = new FocusTree(focus_file);
		FocusLocalizationFile localization = new FocusLocalizationFile(loc_file);
		localization.readLocalization();

		/* open the ui */
//		FocusTreeLocProgress focusLocProgress = new FocusTreeLocProgress(focusTree);
//		focusLocProgress.setVisible(true);
		// todo

		String focus_loc;

		ArrayList<Focus> focusesUnloc = new ArrayList<>();
		assert focusTree.listFocusNames() != null;
		for (Focus focus : focusTree.focuses())
		{
			// if focus id not localized
			if (!localization.isLocalized(focus.id.toString()))
			{
				// write to loc file
				// separate words in focus name
				int i = 0;	//counter
				if (CountryTags.exists(focus.id().substring(0, 3))) {
					i += 3;
				}

				// localize focus name
				focus_loc = EnglishSuperDictionary.titleCapitalize(focus.id().substring(i).replaceAll("_+", " ").trim()); // regex

				// set focus loc
				focus.setNameLocalization(focus_loc);


				focusesUnloc.add(focus);

				localization.setLocalization(focus.id(), focus_loc);
				localization.setLocalizationDesc(focus.id + "_desc", "added focus on " + LocalDateTime.now() + " by hoi4localizer.");
			}
		}

		/* ui */
//		focusLocProgress.incrementProgressBar();
//		focusLocProgress.setNumFocusesUnloc(numFocusesUnloc);
//		focusLocProgress.refreshUnlocFocusesTable(focusesUnloc);

		localization.writeLocalization();
		return true;
	}

	/**
	 * Adds focus localization for unlocalized focuses in the focus tree, which are focuses with an id
	 * but without a given name in the selected localization file of a particular language.
	 * @param focusTree
	 * @param localization
	 * @return
	 * @throws IOException
	 */
	public static int addFocusLoc(FocusTree focusTree, FocusLocalizationFile localization) throws IOException {
		localization.readLocalization();

		String focus_loc;
		String focus_loc_desc;

		ArrayList<Focus> focusesUnloc = new ArrayList<>();
		assert focusTree.listFocusNames() != null;
		for (Focus focus : focusTree.focuses())
		{
			// if focus id not localized
			if (!localization.isLocalized(focus.id()))
			{
				// write to loc file
				// separate words in focus name
				int i = 0;	//counter
				if (CountryTags.exists(focus.id().substring(0, 3))) {
					i += 3;
				}

				// localize focus name
				focus_loc = EnglishSuperDictionary.titleCapitalize(focus.id().substring(i).replaceAll("_+", " ").trim()); // regex
				focus_loc_desc = "added focus on " + LocalDateTime.now() + " by hoi4localizer.";

				// set focus loc
//				focus.setNameLocalization(focus_loc);
//				focus.setDescLocalization(focus_loc_desc);
				Localization nameLoc = localization.setLocalization(focus.id(), focus_loc);
				Localization descLoc = localization.setLocalizationDesc(focus.id() + "_desc", focus_loc_desc);
				focus.setNameLocalization(nameLoc);
				focus.setDescLocalization(descLoc);

				focusesUnloc.add(focus);
			}
		}

		localization.writeLocalization();
		return focusesUnloc.size();
	}

}
