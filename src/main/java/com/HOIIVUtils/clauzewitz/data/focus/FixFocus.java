package com.HOIIVUtils.clauzewitz.data.focus;

import com.HOIIVUtils.clauzewitz.HOIIVUtils;
import com.HOIIVUtils.clauzewitz.data.country.CountryTags;
import com.HOIIVUtils.clauzewitz.exceptions.IllegalLocalizationFileTypeException;
import com.HOIIVUtils.clauzewitz.localization.Localizable;
import com.HOIIVUtils.clauzewitz.localization.LocalizationManager;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;

public class FixFocus extends HOIIVUtils {

	public static void fixLocalization(FocusTree focusTree) throws IOException {
		if (focusTree == null) throw new IllegalArgumentException("Focus tree cannot be null.");
		if (focusTree.focuses() == null || focusTree.focuses().isEmpty()) return;

		var focuses = focusTree.focuses();
		var locManager = LocalizationManager.get();
		File locFile = focusTree.primaryLocalizationFile();
		if (locFile == null) return;
		focuses.stream().filter(focus -> focus.localization(Localizable.Property.NAME) == null).forEach(focus -> {
			String focusName = focus.id();
			// todo improve country tag detection
			if (CountryTags.exists(focusName.substring(0, 3))) {
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
			focus.setLocalization(Localizable.Property.NAME, focusName, locFile);
			var descText = "added on " + LocalDateTime.now() + " by hoi4localizer.";
			focus.setLocalization(Localizable.Property.DESCRIPTION, descText, locFile);
		});
	}
}
