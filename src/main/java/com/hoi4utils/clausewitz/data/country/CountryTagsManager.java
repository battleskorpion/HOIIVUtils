package com.hoi4utils.clausewitz.data.country;

import com.hoi4utils.FileUtils;
import com.hoi4utils.clausewitz.HOIIVUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class CountryTagsManager extends HOIIVUtils implements Iterable<CountryTag> {

	private static ArrayList<CountryTag> country_tags;

	private static File mod_country_tags_folder;
	private static File base_country_tags_folder;
	private static Collection<File> mod_country_tags_files;
	private static Collection<File> base_country_tags_files; 

	private static ArrayList<CountryTag> loadCountryTags() throws IOException {
		country_tags = new ArrayList<>();
		
		if (HOIIVUtils.get("mod.path") != null) mod_country_tags_folder = new File(HOIIVUtils.get("mod.path") + "\\common\\country_tags");
		else mod_country_tags_folder = null; 
		if (HOIIVUtils.get("hoi4.path") != null) base_country_tags_folder = new File(HOIIVUtils.get("hoi4.path") + "\\common\\country_tags");
		else base_country_tags_folder = null; 
		
		if (mod_country_tags_folder != null && mod_country_tags_folder.exists() && mod_country_tags_folder.isDirectory() 
				&& mod_country_tags_folder.listFiles() != null) {
			mod_country_tags_files = List.of(Objects.requireNonNull(mod_country_tags_folder.listFiles()));
		} else {
			//country_tags_files = new ArrayList<>();
			mod_country_tags_files = null;
		}
		if (base_country_tags_folder != null && base_country_tags_folder.exists() && base_country_tags_folder.isDirectory() 
				&& base_country_tags_folder.listFiles() != null) {
			base_country_tags_files = List.of(Objects.requireNonNull(base_country_tags_folder.listFiles()));
		} else {
			//country_tags_files = new ArrayList<>();
			base_country_tags_files = null;
		}

		/* read countries if applicable */
		Scanner countryTagsReader = null;
		if (mod_country_tags_files != null) {
			for (File f : mod_country_tags_files) {
				// don't include dynamic country tags (used for civil wars)
				if (f.getName().contains("dynamic_countries")) {
					continue;
				}

				countryTagsReader = new Scanner(f);

				// make a list of country tags
				while (countryTagsReader.hasNextLine()) {
					String data = countryTagsReader.nextLine().replaceAll("\\s", "");
					if (FileUtils.usefulData(data)) {
						// takes the defined tag at the beginning of the line
						CountryTag tag = new CountryTag(data.substring(0, data.indexOf('=')).trim());
						if (tag.equals(CountryTag.NULL_TAG())) {
							continue;
						}
						country_tags.add(tag);
					}
				}
			}
		}
		if (base_country_tags_files != null) {
			for (File f : base_country_tags_files) {
				// don't include dynamic country tags (used for civil wars)
				if (f.getName().contains("dynamic_countries")) {
					continue;
				}

				countryTagsReader = new Scanner(f);

				// make a list of country tags
				while (countryTagsReader.hasNextLine()) {
					String data = countryTagsReader.nextLine().replaceAll("\\s", "");
					if (FileUtils.usefulData(data)) {
						// takes the defined tag at the beginning of the line
						CountryTag tag = new CountryTag(data.substring(0, data.indexOf('=')).trim());
						if (tag.equals(CountryTag.NULL_TAG())) {
							continue;
						}
						if (!country_tags.contains(tag)) country_tags.add(tag); // dont overwrite mod tags (shouldn't actually matter) 
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
				return CountryTagsManager.loadCountryTags();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		else {
			return country_tags;
		}
	}

	//unused and causing conflict @battleskorp fix this cause the solution depends on what you want
//	public static CountryTag get(String tag) {
//		if (exists(tag)) {
//			return country_tags.stream().filter(ct -> ct.get().equals(tag)).findFirst().orElse(null);
//		}
//		return null;
//	}

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
