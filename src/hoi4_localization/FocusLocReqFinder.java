package hoi4_localization;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Scanner;

public class FocusLocReqFinder {

	public static boolean findLocReqFocuses(File hoi4_dir) throws IOException {
		File common_dir = new File(hoi4_dir.getPath() + "\\\\common");
		File national_focus_dir = new File(hoi4_dir.getPath() + "\\\\common" + "\\\\national_focus");
		File country_tags_file = new File(hoi4_dir + "\\common\\country_tags\\00_countries.txt");

		ArrayList<String> focus_names = new ArrayList<String>();
		ArrayList<String> country_tags = new ArrayList<String>();

		for(File focus_file : national_focus_dir.listFiles()) {
			if(!focus_file.isDirectory()) {         // skip directories for now even if there is
													// focus files in them

				Scanner focusReader = new Scanner(focus_file);

				//TODO call country tags

				// make a list of all focus names
				focus_names = Focus.find(focus_file);

//				System.out.println(focus_names);
//				System.out.println(focus_names.size());
			}
		}
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
