package hoi4_localization.focus;

import hoi4_localization.CountryTags;
import hoi4_localization.province.CountryTag;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Scanner;

public class FocusLocReqFinder {

	public static boolean findLocReqFocuses(File hoi4_dir) throws IOException {
		File common_dir = new File(hoi4_dir.getPath() + "\\common");
		File national_focus_dir = new File(hoi4_dir.getPath() + "\\common" + "\\national_focus");
		File country_tags_file = new File(hoi4_dir + "\\common\\country_tags\\00_countries.txt");
		File localization_dir = new File(hoi4_dir + "\\localisation\\english");

		String focus_loc_pathname = hoi4_dir + "\\localisation\\english\\focus_";
		ArrayList<File> focus_loc_files = new ArrayList<>();

		CountryTags.list(); 		// make sure country tags are generated

		for(File focus_file : Objects.requireNonNull(national_focus_dir.listFiles())) {
			if(!focus_file.isDirectory()) {         // skip directories for now even if there is
													// focus files in them

//				Scanner focusReader = new Scanner(focus_file);

				// make a list of all focus names
				new FocusTree(focus_file);

//				System.out.println(focus_names);
//				System.out.println(focus_names.size());
			}
		}

		for (File loc_file : Objects.requireNonNull(localization_dir.listFiles())) {
			if (loc_file.isFile()) {
				Scanner ideaReader = new Scanner(loc_file);
				aa:
				while (ideaReader.hasNext()) {
					String ideaLine = ideaReader.nextLine();
					if (CountryTags.exists(ideaLine.trim().substring(0, 3))) {
						CountryTag tag = new CountryTag(ideaLine.trim().substring(0, 3));

						if (NationalFocuses.get(tag) != null) {
							ArrayList<String> focuses = NationalFocuses.get(tag).list();
							if(focuses.contains(ideaLine.substring(0, ideaLine.indexOf(":")))) {
								NationalFocuses.get(tag).setLocalization(loc_file);
								break aa;
							}
							//break aa;
						}
					}
				}
			}
		}

		/* focuses without loc file, and focuses with incomplete loc file */

		return true;
	}

	private static boolean usefulData(String data) {
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
}
