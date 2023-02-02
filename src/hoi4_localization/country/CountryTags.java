package hoi4_localization.country;

import hoi4_localization.HOI4Fixes;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

import static settings.LocalizationSettings.Settings.MOD_DIRECTORY;

public class CountryTags extends HOI4Fixes {

	private static ArrayList<CountryTag> country_tags;

	private static File country_tags_file;

	private static ArrayList<CountryTag> find() throws IOException {
		country_tags = new ArrayList<CountryTag>();
		country_tags_file = new File(HOI4Fixes.settings.get(MOD_DIRECTORY) + "\\common\\country_tags\\00_countries.txt");

		if(HOI4Fixes.settings.get(MOD_DIRECTORY) == null) {
			return null;
		}
		Scanner countryTagsReader = new Scanner(country_tags_file);

		// make a list of country tags
		while (countryTagsReader.hasNextLine()) {
			String data = countryTagsReader.nextLine().replaceAll("\\s", "");
			if (usefulData(data)) {
				// takes the defined tag at the beginning of the line
				country_tags.add(new CountryTag(data.substring(0, data.indexOf('=')).trim()));
				//System.out.println(data.substring(0, data.indexOf('=')));
			}
		}
		countryTagsReader.close();

		return country_tags;
	}

	public static ArrayList<CountryTag> list() throws IOException {
		if (country_tags == null) {
			return CountryTags.find();
		}
		else {
			return country_tags;
		}
	}

	public static boolean exists(String substring) {
		return country_tags.contains(new CountryTag(substring));
	}
}
