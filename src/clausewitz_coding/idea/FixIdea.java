package clausewitz_coding.idea;

import clausewitz_coding.country.CountryTag;
import clausewitz_coding.country.CountryTags;
import clausewitz_coding.HOI4Fixes;
import clausewitz_coding.localization.LocalizationFile;

import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;

import static settings.LocalizerSettings.Settings.MOD_DIRECTORY;

public class FixIdea extends HOI4Fixes {

	public static boolean addIdeaLoc(File idea_file, File loc_file) throws IOException {
		hoi4_dir_name = HOI4Fixes.settings.get(MOD_DIRECTORY);

		// some vars
		ArrayList<String> ideas_localized = new ArrayList<String>();
		LocalizationFile localization = new LocalizationFile(loc_file);
		localization.readLocalization();

		String idea_loc;

		Idea.load(idea_file);
		assert Idea.getIdeas() != null;
		for (Idea idea : Idea.getIdeas()) {
			if (!localization.isLocalized(idea.ideaID)) {
				// write to loc file
				// separate words in idea name
				int i = 0;
				if(CountryTags.list().contains(new CountryTag(idea.ideaID.substring(0, 3)))) {
					i+=3;
				}

				idea_loc = titleCapitalize(idea.ideaID.substring(i).replaceAll("_+", " ").trim()); // regex
				localization.setLocalization(idea.ideaID, idea_loc);

//				idea_loc = idea + loc_key + " ";
//				idea_loc += "\"";
//				idea_loc += titleCapitalize(idea.ideaID.substring(i, idea.ideaID.length()).replaceAll("_+", " ").trim()); // regex
//				idea_loc += "\"";
//				locPWriter.println("");
//				locPWriter.println("    " + idea_loc); 									// NO TAB, YML PREFERS SPACES
//				// add blank desc line:
//				locPWriter.println("    " + idea + "_desc" + loc_key + " " + "\"" + "\""); // NO TAB, YML PREFERS SPACES
//				System.out.println("added idea to loc, idea " + idea_loc);
			}
		}

		localization.writeLocalization();
		return true;
	}
}
