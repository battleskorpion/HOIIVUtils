package hoi4_localization;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class Focus extends HOI4Fixes {

	private static ArrayList<String> focus_names;

	public static ArrayList<String> find(File focus_file) throws IOException {
		Scanner focusReader = new Scanner(focus_file);
		focus_names = new ArrayList<String>();

		// make a list of all focus names
		boolean findFocusName = false;
		int focus_name_index;  // index of focus name in string
		while (focusReader.hasNextLine()) {
			String data = focusReader.nextLine().replaceAll("\\s", "");
			if (usefulData(data)) {
				if (!findFocusName) {
					if ((data.trim().length() >= 6) && (data.trim().substring(0, 6).equals("focus="))) {
						findFocusName = true;
					}
				} else {
					if (data.trim().length() >= 3 && data.trim().substring(0, 3).equals(("id="))) {
						focus_name_index = data.indexOf("id=") + 3;
						focus_names.add(data.substring(focus_name_index, data.length()).trim());
						findFocusName = false;
					}
				}
			}
		}
		focusReader.close();

		return focus_names;
	}

	/**
	 * Lists last set of focuses
	 * @return
	 */
	public static ArrayList<String> list() throws IOException {
		if(focus_names == null) {
			return null; // bad :(
		}

		return focus_names;
	}

	public static ArrayList<String> list(File focus_file) throws IOException {
		if(focus_names == null) {
			return Focus.find(focus_file);
		}

		return focus_names;
	}

}
