package hoi4_localization.focus;

import hoi4_localization.HOI4Fixes;
import hoi4_localization.localization.LocalizationFile;
import hoi4_localization.country.CountryTag;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public final class FocusTree extends HOI4Fixes {

	private ArrayList<String> focus_names;
	private File focus_file;
	private CountryTag country;
	private LocalizationFile locFile = null;

	private String id;		// todo all
	private CountryModifier countryModifier;
	private boolean default_focus;
	private Point continuous_focus_position;

	/**
	 * Instantiate focus tree from pre-existing focus tree (file).
	 * @param focus_file pre-existing focus tree.
	 * @throws IOException file not found or otherwise unable to be accessed.
	 */
	public FocusTree(File focus_file) throws IOException {
		this.focus_file = focus_file;
		country = new CountryTag("###");

		find(focus_file);
		FocusTrees.add(country(), this);
	}

	/**
	 * Instantiate new focus tree.
	 * @param id Focus id (usually kept same as file/country name).
	 */
	public FocusTree(String id, CountryTag tag) {
		this.id = id;
		countryModifier = new CountryModifier();
		default_focus = false;
		continuous_focus_position = new Point(50, 1200);
		country = tag;

		FocusTrees.add(country(), this);
	}

	public ArrayList<String> find(File focus_file) throws IOException {
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

						/* get country */
						country = new CountryTag(data.trim().substring(focus_name_index, focus_name_index + 3));

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
	public ArrayList<String> list() throws IOException {
		if(focus_names == null) {
			return null; // bad :(
		}

		return focus_names;
	}

//	public static ArrayList<String> list(File focus_file) throws IOException {
//		if(NationalFocuses.get(focus_file) focus_names == null) {
//			return FocusTree.find(focus_file);
//		}
//
//		return focus_names;
//	}

	public CountryTag country() {
		if (country != null) {
			return country;
		}
		else {
			//idk :(
			return null;
		}
	}

	/**
	 *
	 * @return Localization file for this focus tree, or null.
	 */
	public LocalizationFile locFile() {
		return locFile;
	}

	public File focusFile() { return focus_file; }

	public File setLocalization (File locFile) {
		this.locFile = new LocalizationFile(locFile);
		return locFile;
	}

	@Override
	public boolean equals(Object other) {
		if (other.getClass() == this.getClass()) {
			return this.focus_file == ((FocusTree) other).focus_file;
		}

		return false;
	}

	@Override
	public String toString() {
		if (id != null && !id.equals("")) {
			return id;
		}
		return country.toString();
	}
}
