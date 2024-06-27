package com.HOIIVUtils.clauzewitz.localization;

import com.HOIIVUtils.clauzewitz.HOIIVFile;
import com.HOIIVUtils.ui.FXWindow;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Interface for Clausewitz-localizable objects. This interface is specifically related to
 * Clausewitz-engine localization, and not the general localization/i18n of this program.
 */
public interface Localizable {

	enum Property {
		NAME,
		DESCRIPTION,
	}

	/**
	 * Default method to get the number of localizable properties.
	 * @implNote this method should not usually be overwritten.
	 * @return the number of localizable properties.
	 */
	default int numLocalizableProperties() {
		return getLocalizableProperties().size();
	}

	/**
	 * Default method to get the localizable property identifiers and keys.
	 * @return a map of localizable property identifiers and keys.
	 */
	@NotNull Map<Property, String> getLocalizableProperties();

	default List<String> getLocalizationKeys() {
		return new ArrayList<>(getLocalizableProperties().values());
	}

	/**
	 * Add a localizable property using the specified key.
	 * @param property the localized property to add.
	 * @param localizationKey the property localization key to add.
	 */
	default void addLocalizableProperty(Property property, String localizationKey) {
		getLocalizableProperties().put(property, localizationKey);
	}

	/**
	 * Default method to clear the localizable properties.
	 */
	default void clearLocalizableProperties() {
		getLocalizableProperties().clear();
	}

	default List<Localization> getLocalization() {
		return LocalizationManager.getAll(getLocalizationKeys());
	}

	/**
	 * Gets the localization for the given property.
	 * @param property the localizable property to get localization for.
	 * @return the localization for the given property.
	 */
	default Localization localization(Property property) {
		String key = getLocalizableProperties().get(property);
		if (key == null) return null;
		return LocalizationManager.get(key);
	}

	/**
	 * Gets the localization text for the given property.
	 * @param property the localizable property to get localization text for.
	 * @return the localization text for the given property, or a placeholder if the localization
	 * is null.
	 */
	default @NotNull String localizationText(Property property) {
		if (localization(property) == null) return "[null]";
		return localization(property).text();
	}

	default @NotNull String localizationStatus(Property property) {
		if (localization(property) == null) return "[null]";
		return localization(property).status().toString();
	}

	// todo may bring back at some point
//	/**
//	 * Sets the localization for the given property to the new value, or creates a new localization if none exists.
//	 * @param property the localizable property to set.
//	 * @param text the new localization text.
//\	 */
//	default void setLocalization(Property property, String text) {
//		setLocalization(property, text, primaryLocalizationFile());
//	}

//	default File primaryLocalizationFile() {
//		var localizableGroup = getLocalizableGroup();
//		return localizableGroup.stream()
//				.flatMap(localizable -> localizable.getLocalizationKeys().stream())
//				.map(LocalizationManager::getLocalizationFile)
//				.filter(Objects::nonNull)
//				.findFirst().orElse(null);
//	}
default File primaryLocalizationFile() {
	var localizableGroup = getLocalizableGroup();
	for (Object localizable : localizableGroup) {
		if (!(localizable instanceof Localizable)) {
			throw new ClassCastException("Element is not an instance of Localizable: " + localizable.getClass());
		}
		System.out.println("Localizable element: " + localizable.getClass());
	}
	return localizableGroup.stream()
			.flatMap(localizable -> localizable.getLocalizationKeys().stream())
			.map(LocalizationManager::getLocalizationFile)
			.filter(Objects::nonNull)
			.findFirst().orElse(null);
}

	/**
	 * Gets the localizable group of objects that this object is a part of.
	 * @return the localizable group of objects.
	 */
	@NotNull Collection<? extends Localizable> getLocalizableGroup();

	/**
	 * Asks the user to determine the localization file to use.
	 * @return the localization file to use.
	 */
	default File askUserForLocalizationFile() {
		File initialDirectory = HOIIVFile.mod_localization_folder;
		File file = FXWindow.openChooser(initialDirectory, false);
		if (file == null) file = new File(HOIIVFile.mod_localization_folder, "HOIUtils_extra_localization.yml");
		return file;
	}

	/**
	 * Sets the localization for the given property to the new value, or creates a new localization if none exists.
	 * @param property the localizable property to set.
	 * @param text the new localization text.
	 * @param file the file the localization belongs in.
	 */
	default void setLocalization(Property property, String text, @NotNull File file) {
		setLocalization(property, null, text, file);
	}

	/**
	 * Sets the localization for the given property to the new value, or creates a new localization if none exists.
	 * @param property the localizable property to set.
	 * @param version the localization version number
	 * @param text the new localization text.
	 * @param file the file the localization belongs in.
	 */
	default void setLocalization(Property property, Integer version, String text, @NotNull File file) {
		String key = getLocalizableProperties().get(property);
		LocalizationManager.get().setLocalization(key, version, text, file);
	}

	/**
	 * Sets the localization for the given property to the new value.
	 * @param property the localizable property to set.
	 * @param text the new localization text.
	 */
	default void replaceLocalization(Property property, String text) {
		String key = getLocalizableProperties().get(property);
		LocalizationManager.get().replaceLocalization(key, text);
	}

}
