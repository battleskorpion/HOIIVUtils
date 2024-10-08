package com.hoi4utils.clausewitz.localization;

public class UnexpectedLocalizationStatusException extends RuntimeException {
    public UnexpectedLocalizationStatusException(Localization localization, Localization other) {
        super("Unexpected localization statuses: " + localization + ", status: " + localization.status()
                + " and " + other + ", status: " + other.status());
    }

    public UnexpectedLocalizationStatusException(Localization localization) {
        super("Unexpected localization status: " + localization + ", status: " + localization.status());
    }
}
