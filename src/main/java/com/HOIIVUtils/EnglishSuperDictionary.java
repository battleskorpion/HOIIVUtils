package com.HOIIVUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class EnglishSuperDictionary {

	/**
	 * Capitalizes every word in a string with a pre-set whitelist
	 * 
	 * @param title
	 * @return Returns the edited string unless the string has no words
	 */
	public static String titleCapitalize(String title) {
		if (title == null) {
			return null;
		}
		if (title.trim().isEmpty()) {
			return title;
		}

		ArrayList<String> words = new ArrayList<>(Arrays.asList(title.split(" ")));
		HashSet<String> whitelist = EnglishSuperDictionary.createCapitalizationWhitelist();

		if (words.get(0).length() == 1) {
			words.set(0, Character.toUpperCase(words.get(0).charAt(0)) + "");
		} else if (words.get(0).length() > 1) {
			words.set(0, Character.toUpperCase(words.get(0).charAt(0))
					+ words.get(0).substring(1));
		} else {
			// todo this should never happen now right?
			System.out.println("first word length < 1");
		}

		System.out.println("num words: " + words.size());
		for (int i = 1; i < words.size(); i++) {
			if (!EnglishSuperDictionary.isAcronym(words.get(i)) && !(whitelist.contains(words.get(i)))) {
				if (words.get(i).length() == 1) {
					words.set(i, Character.toUpperCase(words.get(i).charAt(0)) + "");
				} else if (words.get(i).length() > 1) {
					// System.out.println("working cap");
					words.set(i, Character.toUpperCase(words.get(i).charAt(0))
							+ words.get(i).substring(1));
				}
			}

		}

		System.out.println("capitalized: " + String.join(" ", words));
		return String.join(" ", words);
	}

	static boolean isAcronym(String word) {
		int num_cap_letters = EnglishSuperDictionary.numCapLetters(word);

		return num_cap_letters == word.length();
	}

	static int numCapLetters(String word) {
		if (word == null) {
			return 0;
		}

		int num_cap_letters;
		num_cap_letters = 0;
		for (int j = 0; j < word.length(); j++) {
			if (Character.isUpperCase(word.charAt(j))) {
				num_cap_letters++;
			}
		}
		return num_cap_letters;
	}

	static HashSet<String> createCapitalizationWhitelist() {
		String[] whitelist = {
				"a",
				"above",
				"after",
				"among", // among us
				"an",
				"and",
				"around",
				"as",
				"at",
				"below",
				"beneath",
				"beside",
				"between",
				"but",
				"by",
				"for",
				"from",
				"if",
				"in",
				"into",
				"nor",
				"of",
				"off",
				"on",
				"onto",
				"or",
				"over",
				"since",
				"the",
				"through",
				"throughout",
				"to",
				"under",
				"underneath",
				"until",
				"up",
				"with",
		};

		// create the whitelist
		return new HashSet<>(List.of(whitelist));
	}

}
