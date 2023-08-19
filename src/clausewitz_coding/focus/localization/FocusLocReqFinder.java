package clausewitz_coding.focus.localization;

import clausewitz_coding.country.CountryTags;
import clausewitz_coding.country.CountryTag;
import clausewitz_coding.focus.FocusTree;
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

		CountryTags.list(); // make sure country tags are generated

		/* instantiate each focus tree in national focus folder */
		for (File focus_file : Objects.requireNonNull(national_focus_dir.listFiles())) {
			if (!focus_file.isDirectory()) { // skip directories for now even if there is
												// focus files in them todo

				// make a list of all focus names
				FocusTree focus = new FocusTree(focus_file);
				// todo how long does it take to init these focus trees????

				// System.out.println(focus.country());
			}
		}

		/*
		 * loc file reader, determine if loc file localizes a tree and record focus ->
		 * loc file link
		 */
		// todo: blacklist files that definitely arent for focuses, or something idk
		for (File loc_file : Objects.requireNonNull(localization_dir.listFiles())) {
			if (loc_file.isFile()) {
				Scanner locReader = new Scanner(loc_file);

				aa: while (locReader.hasNext()) {
					String locLine = locReader.nextLine();
					if (locLine.trim().length() >= 3) {
						String potentialTag = locLine.trim().substring(0, 3);

						if (CountryTags.exists(potentialTag)) {
							CountryTag tag = new CountryTag(potentialTag);
							// System.out.println(potentialTag + " REEEEEE ");
							/* link loc file to focus file */
							if (FocusTree.getdankwizardisfrench(tag) != null) {
								ArrayList<String> focuses = FocusTree.getdankwizardisfrench(tag).listFocusNames();
								if (focuses != null) {
									if (focuses.contains(locLine.substring(0, locLine.indexOf(":")))) {
										FocusTree.getdankwizardisfrench(tag).setLocalization(loc_file);
									}
									// break aa;
									else {
										System.err.println(
												"Warning: Focus localized in locale but not found in linked focus tree?");
									}
								}
								break aa;
							} else {
								// tag in localization, but no focus tree with tag?
								System.err.println(
										"country tag " + tag + " in localization, but no focus tree with tag?");
								break aa;
							}
						}
					}
				}
			}
		}

		/* focuses without loc file, and focuses with incomplete loc file */

		return true;
	}
}
