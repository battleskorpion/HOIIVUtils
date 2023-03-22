package clausewitz_coding.country;

import clausewitz_coding.HOI4Fixes;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

import static settings.LocalizerSettings.Settings.MOD_DIRECTORY;

public class CountryTags extends HOI4Fixes {

	private static ArrayList<CountryTag> country_tags;

	private static File country_tags_folder;
	private static File countries_main_file;

	private static ArrayList<CountryTag> find() throws IOException {
		country_tags = new ArrayList<CountryTag>();
		country_tags_folder = new File(HOI4Fixes.settings.get(MOD_DIRECTORY) + "\\common\\country_tags");
		countries_main_file = new File(country_tags_folder.getPath() + "\\00_countries.txt");

		if(HOI4Fixes.settings.get(MOD_DIRECTORY) == null) {
			return null;
		}
		if (!country_tags_folder.isDirectory()) {
			return null;
		}

		/* read 00_countries if applicable */
		/* else load vanilla tags */
		Scanner countryTagsReader = null;
		if (countries_main_file.exists()) {
			countryTagsReader = new Scanner(countries_main_file);

			// make a list of country tags
			while (countryTagsReader.hasNextLine()) {
				String data = countryTagsReader.nextLine().replaceAll("\\s", "");
				if (usefulData(data)) {
					// takes the defined tag at the beginning of the line
					CountryTag tag = new CountryTag(data.substring(0, data.indexOf('=')).trim());
					if (tag.equals(CountryTag.NULL_TAG)) {
						continue;
					}
					country_tags.add(tag);
					//System.out.println(data.substring(0, data.indexOf('=')));
				}
			}
		} else {
			System.out.println("loading default country tags because country_tags\\00_countries does not exist");
			File country_tags_default_folder = new File("hoi4files\\country_tags"); 	// with program
			if (!country_tags_default_folder.exists()) {
				throw new IOException("Missing " + country_tags_default_folder);
			}
			for (File file: country_tags_default_folder.listFiles()) {
				countryTagsReader = new Scanner(file);

				// make a list of country tags
				while (countryTagsReader.hasNextLine()) {
					String data = countryTagsReader.nextLine().replaceAll("\\s", "");
					if (usefulData(data)) {
						// takes the defined tag at the beginning of the line
						CountryTag tag = new CountryTag(data.substring(0, data.indexOf('=')).trim());
						if (tag.equals(CountryTag.NULL_TAG)) {
							continue;
						}
						country_tags.add(tag);
					}
				}
			}
		}

		/* read other country tags */
		if (country_tags_folder.listFiles() != null) {
			for (File file : country_tags_folder.listFiles()) {
				if (countries_main_file.exists() && file.equals(countries_main_file)) {
					continue;
				}
				// don't include dynamic country tags (used for civil wars)
				if (file.getName().contains("dynamic_countries")) {
					continue;
				}

				countryTagsReader = new Scanner(file);

				// make a list of country tags
				while (countryTagsReader.hasNextLine()) {
					String data = countryTagsReader.nextLine().replaceAll("\\s", "");
					if (usefulData(data)) {
						// takes the defined tag at the beginning of the line
						CountryTag tag = new CountryTag(data.substring(0, data.indexOf('=')).trim());
						if (tag.equals(CountryTag.NULL_TAG)) {
							continue; 
						}
						country_tags.add(tag);
					}
				}
			}
		}

		if (countryTagsReader != null) {
			countryTagsReader.close();
		} 

		return country_tags;
	}

	public static ArrayList<CountryTag> list() {
		if (country_tags == null) {
			try {
				return CountryTags.find();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		else {
			return country_tags;
		}
	}

	public static boolean exists(String substring) {
		if (country_tags == null) {
			list();
		}

		return country_tags.contains(new CountryTag(substring));
	}
}
