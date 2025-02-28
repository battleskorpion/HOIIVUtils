package com.hoi4utils.clausewitz.data.focus;

import com.hoi4utils.clausewitz.HOIIVUtils;
import com.hoi4utils.clausewitz.data.country.CountryTagsManager;
import com.hoi4utils.clausewitz.localization.LocalizationManager;
import com.hoi4utils.clausewitz.localization.Property;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import scala.jdk.javaapi.CollectionConverters;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collection;

/**
 * FixFocus is a utility class for fixing focus localization in a focus tree.
 * It ensures that all focuses have proper localization for their names and descriptions.
 */
public class FixFocus extends HOIIVUtils {
	private static final Logger LOGGER = LogManager.getLogger(FixFocus.class);

	public static void fixLocalization(FocusTree focusTree) throws IOException {
		validateFocusTree(focusTree);

		var locManager = LocalizationManager.get();
		
		File locFile = focusTree.primaryLocalizationFile().get();

		LOGGER.debug("Fixing focus localization for {}", locFile.getName());
		Collection<Focus> focuses = CollectionConverters.asJavaCollection(focusTree.focuses());
		
		focuses.parallelStream()
				.filter(focus -> focus.localization(Property.NAME) == null)
				.forEach(focus -> processFocusLocalization(focus, locManager, locFile));
	}

	private static void validateFocusTree(FocusTree focusTree) {
		if (focusTree == null) {
			throw new IllegalArgumentException("Focus tree cannot be null.");
		}
		if (focusTree.focuses() == null || focusTree.focuses().isEmpty()) {
			LOGGER.fatal("Focus tree has no focuses");
			JOptionPane.showMessageDialog(null, "Focus tree has no focuses.", "Error", JOptionPane.ERROR_MESSAGE);
			throw new IllegalStateException("Focus tree has no focuses.");
		}
	}

	private static void processFocusLocalization(Focus focus, LocalizationManager locManager, File locFile) {
		String focusName = extractFocusName(focus.id().getOrElse(null));

		// Format the focus name
		String formattedName = locManager.titleCapitalize(focusName.replaceAll("_+", " ").trim());

		// Set missing localizations
		focus.setLocalization(Property.NAME, formattedName, locFile);
		focus.setLocalization(Property.DESCRIPTION, generateDescription(), locFile);
	}

	private static String extractFocusName(String focusName) {
		if (focusName == null || focusName.length() < 4) {
			return "Unnamed Focus"; // Fallback in case of invalid focus name
		}

		String tag = focusName.substring(0, 3);
		if (CountryTagsManager.exists(tag)) {
			boolean hasUnderscore = focusName.charAt(3) == '_';
			focusName = focusName.substring(hasUnderscore ? 4 : 3);
		}

		return focusName;
	}

	private static String generateDescription() {
		return "Added on " + LocalDateTime.now() + " by hoi4localize.";
	}
}
