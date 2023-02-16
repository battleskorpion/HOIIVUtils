package hoi4_coding.idea;

import hoi4_coding.country.CountryTags;
import hoi4_coding.HOI4Fixes;

import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;

import static settings.LocalizationSettings.Settings.MOD_DIRECTORY;

public class FixIdea extends HOI4Fixes {

	public static boolean addIdeaLoc(File idea_file, File loc_file) throws IOException {
		hoi4_dir_name = HOI4Fixes.settings.get(MOD_DIRECTORY);

		// some vars
		ArrayList<String> ideas_localized = new ArrayList<String>();
		String loc_key = ":0";

		// more var
		Scanner locReader = new Scanner(loc_file);

		/* make a list of every localized idea */
		boolean found_lang = false;
		while(found_lang == false) {
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

		/* list of localized ideas */
		while (locReader.hasNextLine()) {
			String data = locReader.nextLine().replaceAll("\\s", "");
			if (usefulData(data)) {
				if (data.contains(":")) {
					// fixed crash when there were no ideas localized hopefully
					if (Idea.list(idea_file) != null && Idea.list(idea_file)
							.contains(data.substring(0, data.indexOf(":")))) {
						ideas_localized.add(data.substring(0, data.indexOf(":")).trim());
					}
				}
			}
		}
		locReader.close();

		// do stuff with nonlocalized ideasw
		// some vars
		FileWriter locWriter = new FileWriter(loc_file, true);		// true = append
		BufferedWriter locBWriter = new BufferedWriter(locWriter);
		PrintWriter locPWriter = new PrintWriter(locBWriter); 		        // for println syntax
		String idea_loc;

		assert Idea.list() != null;
		for (String idea : Idea.list()) {
			if(!ideas_localized.contains(idea)) {
				// write to loc file
				// separate words in idea name
				int i = 0;
				if(CountryTags.list().contains(idea.substring(0, 3))) {
					i+=3;
				}

				idea_loc = idea + loc_key + " ";
				idea_loc += "\"";
				idea_loc += titleCapitalize(idea.substring(i, idea.length()).replaceAll("_+", " ").trim()); // regex
				idea_loc += "\"";
				locPWriter.println("");
				locPWriter.println("    " + idea_loc); 									// NO TAB, YML PREFERS SPACES
				// add blank desc line:
				locPWriter.println("    " + idea + "_desc" + loc_key + " " + "\"" + "\""); // NO TAB, YML PREFERS SPACES
				System.out.println("added idea to loc, idea " + idea_loc);
			}
		}

		locPWriter.close();

		return true;
	}
}
