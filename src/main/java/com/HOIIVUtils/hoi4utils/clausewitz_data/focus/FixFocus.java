package com.HOIIVUtils.hoi4utils.clausewitz_data.focus;

import com.HOIIVUtils.hoi4utils.EnglishSuperDictionary;
import com.HOIIVUtils.hoi4utils.HOIIVUtils;
import com.HOIIVUtils.hoi4utils.Settings;
import com.HOIIVUtils.hoi4utils.clausewitz_data.country.CountryTags;
import com.HOIIVUtils.hoi4utils.clausewitz_data.localization.FocusLocalizationFile;
import com.HOIIVUtils.hoi4utils.clausewitz_data.localization.IllegalLocalizationFileTypeException;
import com.HOIIVUtils.hoi4utils.clausewitz_data.localization.Localization;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;

import static com.HOIIVUtils.hoi4utils.clausewitz_data.country.CountryTag.COUNTRY_TAG_LENGTH;

/*
 * FixFoucus Documentation
 */
public class FixFocus extends HOIIVUtils {

	public static boolean addFocusLoc(File focus_file, File loc_file) throws IOException {

		// ArrayList<String> focuses_nonlocalized = new ArrayList<String>();
		// FocusTree focusTree = new FocusTree(focus_file);
		FocusTree focusTree = FocusTree.get(focus_file);
		FocusLocalizationFile localization = null;
		try {
			localization = new FocusLocalizationFile(loc_file);
		} catch (IllegalLocalizationFileTypeException e) {
			throw new RuntimeException(e);
		}
		localization.read();

		/* open the ui */
		// FocusTreeLocProgress focusLocProgress = new FocusTreeLocProgress(focusTree);
		// focusLocProgress.setVisible(true);
		// todo

		String focus_loc;

		ArrayList<Focus> focusesUnloc = new ArrayList<>();
		assert focusTree.listFocusIDs() != null;
		for (Focus focus : focusTree.focuses()) {
			// if focus id not localized
			if (!localization.isLocalized(focus.id())) {
				// write to loc file
				// separate words in focus name
				int i = 0; // counter
				if (CountryTags.exists(focus.id().substring(0, 3))) {
					i += 3;
				}

				// localize focus name
				focus_loc = EnglishSuperDictionary
						.titleCapitalize(focus.id().substring(i).replaceAll("_+", " ").trim()); // regex

				// set focus loc
				focus.setNameLocalization(focus_loc);

				focusesUnloc.add(focus);

				localization.setLocalization(focus.id(), focus_loc);
				localization.setLocalizationDesc(focus.id + "_desc",
						"added focus on " + LocalDateTime.now() + " by hoi4localizer.");
			}
			// HOIIVUtilsStageLoader.openError("Test");
		}

		/* ui */
		// focusLocProgress.incrementProgressBar();
		// focusLocProgress.setNumFocusesUnloc(numFocusesUnloc);
		// focusLocProgress.refreshUnlocFocusesTable(focusesUnloc);

		// localization.writeLocalization();
		return true;
	}

	/**
	 * Adds focus localization for unlocalized focuses in the focus tree, which are
	 * focuses with an id
	 * but without a given name in the selected localization file of a particular
	 * language.
	 * 
	 * @param focusTree
	 * @param localization
	 * @return
	 * @throws IOException
	 */
	public static int addFocusLoc(FocusTree focusTree, FocusLocalizationFile localization) throws IOException {
		localization.read();

		String focus_loc;
		String focus_loc_desc;
		ArrayList<Focus> focusesAddedLoc = new ArrayList<>();

		assert focusTree.listFocusIDs() != null;
		for (Focus focus : focusTree.focuses()) {
			// if focus id not localized
			if (!localization.isLocalized(focus.id())) {
				// write to loc file
				// separate words in focus name

				/* ignore country tag */
				int i = 0; // title start index
				if (CountryTags.exists(focus.id().substring(0, COUNTRY_TAG_LENGTH))) {
					i += COUNTRY_TAG_LENGTH;
				}

				// localize focus name
				focus_loc = focus.id().substring(i).replaceAll("_+", " ").trim();
				focus_loc = EnglishSuperDictionary.titleCapitalize(focus_loc); // regex
				focus_loc_desc = "added focus on " + LocalDateTime.now() + " by hoi4localizer.";
				if (Settings.DEV_MODE.enabled()) {
					System.out.println("focus loc: " + focus_loc + ", desc: " + focus_loc_desc);
				}

				// set focus loc
				focus.setLocalization(localization, focus_loc, focus_loc_desc);
				focusesAddedLoc.add(focus);
				continue; // localize and move on
			}

			// add preexisting loc
			Localization.Status locStatus = localization.getLocalization(focus.id()).status();
			if (locStatus.equals(Localization.Status.EXISTS)) {
				focus.setLocalization(localization);
			}
		}

		// localization.writeLocalization(); // eh nah not here
		return focusesAddedLoc.size();
	}

	public static void addFocusLoc(FocusTree focusTree) throws IOException {
		addFocusLoc(focusTree, focusTree.locFile());
	}
}
