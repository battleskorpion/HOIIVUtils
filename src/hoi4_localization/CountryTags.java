package hoi4_localization;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class CountryTags extends HOI4Fixes {

	private static ArrayList<String> country_tags;

	private static File country_tags_file;

	private static ArrayList<String> find() throws IOException {
		country_tags = new ArrayList<String>();
		country_tags_file = new File(hoi4_dir_name + "\\common\\country_tags\\00_countries.txt");

		if(HOI4Fixes.hoi4_dir_name == null) {
			return null;
		}
		Scanner countryTagsReader = new Scanner(country_tags_file);

		// make a list of country tags
		while (countryTagsReader.hasNextLine()) {
			String data = countryTagsReader.nextLine().replaceAll("\\s", "");
			if (usefulData(data)) {
				// takes the defined tag at the beginning of the line
				country_tags.add(data.substring(0, data.indexOf('=')).trim());
				//System.out.println(data.substring(0, data.indexOf('=')));
			}
		}
		countryTagsReader.close();

		return country_tags;
	}

	public static ArrayList<String> list() throws IOException {
		if (country_tags == null) {
			return CountryTags.find();
		}
		else {
			return country_tags;
		}
	}

}
