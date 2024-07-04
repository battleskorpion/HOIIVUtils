package com.HOIIVUtils.clauzewitz.localization;

public class NoLocalizationManagerException extends RuntimeException {
    public NoLocalizationManagerException() {
        super("No localization manager is set.");
    }

}
