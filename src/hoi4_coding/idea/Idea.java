package hoi4_coding.idea;

import hoi4_coding.country.CountryTags;
import hoi4_coding.HOI4Fixes;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class Idea extends HOI4Fixes {

	private static ArrayList<String> idea_list;

	public static ArrayList<String> find(File idea_file) throws IOException {
		Scanner ideaReader = new Scanner(idea_file);
		idea_list = new ArrayList<String>();

		// make a list of all idea names
		//boolean findIdeaName = false;
		//int idea_list_index;  // index of focus name in string
		while (ideaReader.hasNextLine()) {
			String data = ideaReader.nextLine().replaceAll("\\s", "");
//
			if(usefulData(data)) {
				// need enough data length before checks are made to prevent error
				if(data.length() >= 5) {
					// if theoretical TAG at beginning of idea name is in the list of tags
					// (this means the text we have is likely an idea since the tag exists)
					if (CountryTags.list().contains(data.trim().substring(0, 3))) {
						// if this is likely an idea, then check if there is " = {"
						// at the end to confirm likelihood
						// also, a tag is TAG, all caps. check for this after.
						if (data.trim().substring(data.length() - 2, data.length()).equals("={")) {
							if(data.trim().substring(0, 3).equals(data.trim().substring(0, 3).toUpperCase())) {
								// if here, ***should*** be good! data is an idea name,
								// once we clean it up

								// return idea var name, remove "={"
								idea_list.add(data.trim().substring(0, data.length() - 2));
							}
						}
					}
				}
			}
		}
		ideaReader.close();

		return idea_list;
	}

	/**
	 * Lists last set of focuses
	 * @return
	 */
	public static ArrayList<String> list() throws IOException {
		if(idea_list == null) {
			return null; // bad :(
		}

		return idea_list;
	}

	public static ArrayList<String> list(File idea_file) throws IOException {
		if(idea_list == null) {
			return Idea.find(idea_file);
		}

		return idea_list;
	}

}

