package main.java.com.hoi4utils.clauzewitz.localization;

public class NoLocalizationManagerException extends RuntimeException {
    public NoLocalizationManagerException() {
        super("No localization manager is set.");
    }

}
