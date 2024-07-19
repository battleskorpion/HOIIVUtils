package main.java.com.hoi4utils.clauzewitz.data.country;

import main.java.com.hoi4utils.FileUtils;
import main.java.com.hoi4utils.clauzewitz.HOIIVUtils;
import main.java.com.hoi4utils.SettingsManager;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static main.java.com.hoi4utils.Settings.MOD_PATH;

public class CountryTagsManager extends HOIIVUtils implements Iterable<CountryTag> {

	private static ArrayList<CountryTag> country_tags;

	private static File country_tags_folder;
	private static Collection<File> country_tags_files;

	private static ArrayList<CountryTag> loadCountryTags() throws IOException {
		country_tags = new ArrayList<>();
		country_tags_folder = new File(SettingsManager.get(MOD_PATH) + "\\common\\country_tags");
		if (country_tags_folder.exists() && country_tags_folder.isDirectory() && country_tags_folder.listFiles() != null) {
			country_tags_files = List.of(Objects.requireNonNull(country_tags_folder.listFiles()));
		} else {
			//country_tags_files = new ArrayList<>();
			country_tags_files = null;
		}

		if(SettingsManager.get(MOD_PATH) == null) {
			return null;
		}
		if (!country_tags_folder.isDirectory()) {
			return null;
		}

		/* read countries if applicable */
		/* else load vanilla tags */
		// TODO this needs to be better fixed (vanilla overwritten or not???)
		Scanner countryTagsReader = null;
		if (country_tags_files != null) {
			for (File f : country_tags_files) {
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
						if (tag.equals(CountryTag.NULL_TAG)) {
							continue;
						}
						country_tags.add(tag);
						//System.out.println(data.substring(0, data.indexOf('=')));
					}
				}
			}
		} else {
			// TODO this needs! to be fixed
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

	public static CountryTag get(String tag) {
		if (exists(tag)) {
			return country_tags.stream().filter(ct -> ct.get().equals(tag)).findFirst().orElse(null);
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
