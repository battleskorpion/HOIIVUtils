package hoi4utils.clausewitz_coding.idea;

import hoi4utils.clausewitz_coding.country.CountryTags;
import hoi4utils.englishSuperDictionary;
import hoi4utils.HOIIVUtils;
import hoi4utils.clausewitz_coding.localization.LocalizationFile;
import java.io.*;
/**
 * This is the FixIdea file.
 */
public class FixIdea extends HOIIVUtils {

	public static boolean addIdeaLoc(File idea_file, File loc_file) throws IOException {

		// some vars
		LocalizationFile localization = new LocalizationFile(loc_file);
		localization.readLocalization();

		String idea_loc;

		Idea.load(idea_file);
		assert Idea.getIdeas() != null;
		for (Idea idea : Idea.getIdeas()) {
			// if idea id not localized
			if (!localization.isLocalized(idea.ideaID)) {
				// write to loc file
				// separate words in idea name
				int i = 0;
//				if(CountryTags.list().contains(new CountryTag(idea.ideaID.substring(0, 3)))) {
				if (CountryTags.exists(idea.ideaID.substring(0, 3))) {
					i+=3;
				}

				idea_loc = englishSuperDictionary.titleCapitalize(idea.ideaID.substring(i).replaceAll("_+", " ").trim()); // regex
				localization.setLocalization(idea.ideaID, idea_loc);

//				idea_loc = idea + loc_key + " ";
//				idea_loc += "\"";
//				idea_loc += titleCapitalize(idea.ideaID.substring(i, idea.ideaID.length()).replaceAll("_+", " ").trim()); // regex
//				idea_loc += "\"";
//				locPWriter.println("");
//				locPWriter.println("	" + idea_loc); 									// NO TAB, YML PREFERS SPACES
//				// add blank desc line:
//				locPWriter.println("	" + idea + "_desc" + loc_key + " " + "\"" + "\""); // NO TAB, YML PREFERS SPACES
//				System.out.println("added idea to loc, idea " + idea_loc);
			}
		}

		localization.writeLocalization();
		return true;
	}
}
