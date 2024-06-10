package com.HOIIVUtils.clauzewitz.data.country;

import com.HOIIVUtils.FileUtils;
import com.HOIIVUtils.clauzewitz.HOIIVUtils;
import com.HOIIVUtils.SettingsManager;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Scanner;

import static com.HOIIVUtils.Settings.MOD_PATH;

public class CountryTags extends HOIIVUtils implements Iterable<CountryTag> {

	private static ArrayList<CountryTag> country_tags;

	private static File country_tags_folder;
	private static File countries_main_file;

	private static ArrayList<CountryTag> loadCountryTags() throws IOException {
		country_tags = new ArrayList<>();
		country_tags_folder = new File(SettingsManager.get(MOD_PATH) + "\\common\\country_tags");
		countries_main_file = new File(country_tags_folder.getPath() + "\\00_countries.txt");

		if(SettingsManager.get(MOD_PATH) == null) {
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
				if (FileUtils.usefulData(data)) {
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
					if (FileUtils.usefulData(data)) {
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
					if (FileUtils.usefulData(data)) {
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

	public static ArrayList<CountryTag> getCountryTags() {
		if (country_tags == null) {
			try {
				return CountryTags.loadCountryTags();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		else {
			return country_tags;
		}
	}

	public static CountryTag get(String tag) {
		if (exists(tag)) {
			return country_tags.stream().filter(ct -> ct.tag().equals(tag)).findFirst().orElse(null);
		}
		return null;
	}

	public static boolean exists(String tag) {
		if (country_tags == null) {
			getCountryTags();
		}

		return country_tags.contains(new CountryTag(tag));
	}

	@NotNull
	@Override
	public Iterator<CountryTag> iterator() {
		return country_tags.iterator();
	}
}
