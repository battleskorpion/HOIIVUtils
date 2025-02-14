package com.hoi4utils.clausewitz.exceptions;

import com.hoi4utils.clausewitz.localization.Localization;

public class LocalizationExistsException extends Throwable {
    public LocalizationExistsException(Localization localization) {
        super("Localization already exists: " + localization.ID());
    }

    public LocalizationExistsException(Localization prevLocalization, Localization localization) {
        super("Localization already exists: " + prevLocalization + "\n\tand cannot be replaced by: " + localization);
    }

    public LocalizationExistsException(Localization prevLocalization, String text) {
        super("Localization already exists: " + prevLocalization + "\n\tand cannot be replaced with the text: " + text);
    }
}
