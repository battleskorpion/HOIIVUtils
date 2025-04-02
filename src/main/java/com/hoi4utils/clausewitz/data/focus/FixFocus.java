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

	// TODO improve
	public static void fixLocalization(FocusTree focusTree) throws IOException {
		LOGGER.debug("Starting fixLocalization for FocusTree: {}", focusTree);

		if (!validateFocusTree(focusTree)) return;

		var locManager = LocalizationManager.get();
		LOGGER.debug("LocalizationManager loaded");

		File locFile = focusTree.primaryLocalizationFile().get();
		LOGGER.debug("Primary localization file: {}", locFile.getAbsolutePath());

		Collection<Focus> focuses = CollectionConverters.asJavaCollection(focusTree.focuses());
		LOGGER.debug("Total focuses in tree: {}", focuses.size());

		focuses.parallelStream()
				.filter(focus -> {
					boolean missingLocalization = focus.localization(Property.NAME).isEmpty();
					if (missingLocalization) {
						LOGGER.debug("Missing localization for focus: {}", focus.id().str());
					}
					return missingLocalization;
				})
				.forEach(focus -> processFocusLocalization(focus, locManager, locFile));

		LOGGER.debug("Finished fixing focus localization.");
	}

	private static boolean validateFocusTree(FocusTree focusTree) {
		LOGGER.debug("Validating FocusTree: {}", focusTree);

		if (focusTree == null) {
			LOGGER.fatal("Focus tree is null.");
			JOptionPane.showMessageDialog(null, "Focus tree cannot be null.", "Error", JOptionPane.ERROR_MESSAGE);
			return false;
		}

		if (focusTree.focuses() == null || focusTree.focuses().isEmpty()) {
			LOGGER.fatal("Focus tree has NO focuses! Stopping initialization.");
			JOptionPane.showMessageDialog(null, "Error: Focus tree has no focuses.", "Error", JOptionPane.ERROR_MESSAGE);
			throw new IllegalStateException("Focus tree has no focuses.");
		}
		if (focusTree.primaryLocalizationFile().isEmpty()) {
			LOGGER.info("Focus tree has no localization file."); // todo say which focus tree
			JOptionPane.showMessageDialog(null, "Warning: Focus tree has no localization file.", "Warning", JOptionPane.WARNING_MESSAGE);
			return false;
		}
		LOGGER.debug("Focus tree is valid: {}", focusTree);
		return true;
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
		//return "Added on " + LocalDateTime.now() + " by hoi4localizer.";
		return ""; 
	}
}
