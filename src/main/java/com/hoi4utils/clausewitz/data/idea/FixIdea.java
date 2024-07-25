package com.hoi4utils.clausewitz.data.idea;

import com.hoi4utils.clausewitz.HOIIVUtils;

/**
 * This is the FixIdea file.
 * todo: modify design to work with new localization system
 */
public class FixIdea extends HOIIVUtils {

//	public static int addIdeaLoc(IdeaFile idea_file, LocalizationFile localization) throws IOException {
//		localization.read();
//
//		String idea_loc;
//		ArrayList<Idea> ideasAddedLoc = new ArrayList<>();
//
//		assert idea_file.listIdeas() != null;
//		for (Idea idea : idea_file.listIdeas()) {
//			// if idea id not localized
//			if (!localization.isLocalized(idea.id())) {
//				// write to loc file
//				// separate words in idea name
//
//				/* ignore country tag */
//				int i = 0;
//				if (CountryTags.exists(idea.id().substring(0, COUNTRY_TAG_LENGTH))) {
//					i += COUNTRY_TAG_LENGTH;
//				}
//
//				idea_loc = idea.id().substring(i)
//						.replaceAll("_+", " ")
//						.replaceAll("\\s\\s+", " ")
//						.trim();
//				idea_loc = EnglishSuperDictionary.titleCapitalize(idea_loc); // regex
//
//				var newLoc = localization.setLocalization(idea.id(), idea_loc);
//				idea.setLocalization(newLoc);
//				ideasAddedLoc.add(idea);
//				continue;           // localize and move on
//			}
//
//			// add preexisting loc
//			Localization.Status locStatus = localization.getLocalization(idea.id()).status();
//			if (locStatus.equals(Localization.Status.EXISTS)) {
//				idea.setLocalization(localization);
//			}
//		}
//
////		localization.writeLocalization();       // eh nah not here
//		return ideasAddedLoc.size();
//	}
}
