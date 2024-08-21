package com.hoi4utils.clausewitz.data.focus;

import com.hoi4utils.clausewitz.HOIIVUtils;
import com.hoi4utils.clausewitz.data.country.CountryTagsManager;
import com.hoi4utils.clausewitz.localization.*;
import scala.collection.View;
import scala.jdk.javaapi.CollectionConverters;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.stream.Stream;

public class FixFocus extends HOIIVUtils {

	public static void fixLocalization(FocusTree focusTree) throws IOException {
		if (focusTree == null) throw new IllegalArgumentException("Focus tree cannot be null.");
		if (focusTree.focuses() == null || focusTree.focuses().isEmpty()) return;

		var focuses = CollectionConverters.asJavaCollection(focusTree.focuses());
		var locManager = LocalizationManager.get();
		File locFile = focusTree.primaryLocalizationFile();
		if (locFile == null) return;
		focuses.parallelStream().filter(focus -> focus.localization(Property.NAME) == null).forEach(focus -> {
			String focusName = focus.id().getOrElse(null); 
			// todo improve country tag detection
			if (CountryTagsManager.exists(focusName.substring(0, 3))) {
				// next character with typical formatting 'should' be a '_'
				if (focusName.charAt(3) != '_') {
					System.out.println("Warning: Focus id " + focusName + " is not formatted correctly. An underscore ('_') is typically placed after the country tag.");
					focusName = focusName.substring(3);
				} else {
					focusName = focusName.substring(4);
				}
			}
			focusName = locManager.titleCapitalize(focusName.replaceAll("_+", " ").trim());
			// setting the loc file explicitly is okay as this is for missing loc.
			focus.setLocalization(Property.NAME, focusName, locFile);
			var descText = "added on " + LocalDateTime.now() + " by hoi4localizer.";
			focus.setLocalization(Property.DESCRIPTION, descText, locFile);
		});
	}
}
