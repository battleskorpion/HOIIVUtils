package com.hoi4utils.clausewitz.localization;

public class NoLocalizationManagerException extends RuntimeException {
    public NoLocalizationManagerException() {
        super("No localization manager is set.");
    }

}
