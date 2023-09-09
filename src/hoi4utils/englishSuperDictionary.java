package hoi4utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

public class englishSuperDictionary {

	/**
	 * Captilizes 
	 * @param str
	 * @return
	 */
	public static String titleCapitalize(String str) {
		if (str == null) {
			return null;
		}
		if (str.trim().isEmpty()) {
			return str;
		}
	
		// some vars
		ArrayList<String> words = new ArrayList<String>(Arrays.asList(str.split(" ")));
		HashSet<String> whitelist = englishSuperDictionary.createCapitalizationWhitelist();
	
		// first word always capitalized
		if (words.get(0).length() == 1) {
			words.set(0, "" + Character.toUpperCase(words.get(0).charAt(0)));
		} else if (words.get(0).length() > 1) {
			words.set(0, "" + Character.toUpperCase(words.get(0).charAt(0))
					+ words.get(0).substring(1));
		} else {
			// todo this should never happen now right?
			System.out.println("first word length < 1");
		}
	
		// rest of words (if applicable)
		System.out.println("num words: " + words.size());
		for (int i = 1; i < words.size(); i++) {
	
			// if not acronym (acronym = all caps already)
			// && not on whitelist
			if (!englishSuperDictionary.isAcronym(words.get(i)) && !(whitelist.contains(words.get(i)))) {
				if (words.get(i).length() == 1) {
					words.set(i, "" + Character.toUpperCase(words.get(i).charAt(0)));
				} else if (words.get(i).length() > 1) {
					// System.out.println("working cap");
					words.set(i, "" + Character.toUpperCase(words.get(i).charAt(0))
							+ words.get(i).substring(1));
				}
			}
	
		}
	
		System.out.println("capitalized: " + String.join(" ", words));
		return String.join(" ", words);
	}

	static boolean isAcronym(String word) {
		// check for acronym (all caps already)
		int num_cap_letters = englishSuperDictionary.numCapLetters(word);
	
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
		HashSet<String> whitelist = new HashSet<String>();
	
		// create the whitelist
		whitelist.add("a");
		whitelist.add("above");
		whitelist.add("after");
		whitelist.add("among"); // among us
		whitelist.add("an");
		whitelist.add("and");
		whitelist.add("around");
		whitelist.add("as");
		whitelist.add("at");
		whitelist.add("below");
		whitelist.add("beneath");
		whitelist.add("beside");
		whitelist.add("between");
		whitelist.add("but");
		whitelist.add("by");
		whitelist.add("for");
		whitelist.add("from");
		whitelist.add("if");
		whitelist.add("in");
		whitelist.add("into");
		whitelist.add("nor");
		whitelist.add("of");
		whitelist.add("off");
		whitelist.add("on");
		whitelist.add("onto");
		whitelist.add("or");
		whitelist.add("over");
		whitelist.add("since");
		whitelist.add("the");
		whitelist.add("through");
		whitelist.add("throughout");
		whitelist.add("to");
		whitelist.add("under");
		whitelist.add("until");
		whitelist.add("up");
		whitelist.add("with");
		return whitelist;
	}
	
}
