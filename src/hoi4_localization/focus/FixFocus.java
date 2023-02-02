package hoi4_localization.focus;

import hoi4_localization.country.CountryTags;
import hoi4_localization.HOI4Fixes;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;

import static settings.LocalizationSettings.Settings.MOD_DIRECTORY;

public class FixFocus extends HOI4Fixes {

	private static String hoi4_dir_name;

	public static boolean addFocusLoc(File focus_file, File loc_file) throws IOException {

		hoi4_dir_name = HOI4Fixes.settings.get(MOD_DIRECTORY);

		// some vars
		ArrayList<String> focuses_localized = new ArrayList<String>();
		//ArrayList<String> focuses_nonlocalized = new ArrayList<String>(); 
		ArrayList<String> country_tags;
		System.out.println(hoi4_dir_name);
		String loc_key = ":0";
		FocusTree focusTree = new FocusTree(focus_file);
		
		// more vars
		Scanner locReader = new Scanner(loc_file);
		
		// make a list of every localized focus
		boolean found_lang = false; 
		while(!found_lang) {
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


		/* list of localized focuses */
		while (locReader.hasNextLine()) {
			String data = locReader.nextLine().replaceAll("\\s", ""); 
			if (usefulData(data)) {
				if (data.contains(":")) {
					if (focusTree.list().contains(data.substring(0, data.indexOf(":")))) {
						focuses_localized.add(data.substring(0, data.indexOf(":")).trim()); 
					}
				}
			}
		}
		locReader.close(); 
		
		// do stuff with nonlocalized focuses
		// some vars
		FileWriter locWriter = new FileWriter(loc_file, true);		// true = append
		BufferedWriter locBWriter = new BufferedWriter(locWriter); 
		PrintWriter locPWriter = new PrintWriter(locBWriter); 		        // for println syntax
		String focus_loc; 
		//ArrayList<String> focus_loc_array; 
		
		for (String focus : focusTree.list())
		{
			// if focus not in localized focuses
			if (!focuses_localized.contains(focus)) 
			{ 
				// write to loc file 
				// separate words in focus name
				int i = 0;	//counter
				if (CountryTags.list().contains(focus.substring(0, 3)))
				{
					i += 3;
				}
				// make a list of each word in focus_loc
				//focus_loc_array = new ArrayList<String>(Arrays.asList(focus_loc.split(" ")));
				
				focus_loc = focus + loc_key + " ";
				focus_loc += "\""; 
				focus_loc += titleCapitalize(focus.substring(i, focus.length()).replaceAll("_+", " ").trim()); // regex
				focus_loc += "\""; 
				locPWriter.println("");
				locPWriter.println("    " + focus_loc); 									// NO TAB, YML PREFERS SPACES 
				// add blank desc line: 
				locPWriter.println("    " + focus + "_desc" + loc_key + " " + "\"" + "\""); // NO TAB, YML PREFERS SPACES 
				System.out.println("added focus to loc, focus " + focus_loc); 
			}
		}
		locPWriter.close(); 
		
		return true; 
	}

	// useful lines function

}
