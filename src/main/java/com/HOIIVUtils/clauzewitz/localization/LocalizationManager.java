package com.HOIIVUtils.clauzewitz.localization;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public abstract class LocalizationManager {
    //static Map<String, Localization> localizations = new HashMap<>();
    private static LocalizationManager primaryManager = null;

    public LocalizationManager() {
    }

    public static File getLocalizationFile(String key) {
        return get().localizations().getLocalizationFile(key);
    }

    public final void setManager(LocalizationManager manager) {
        primaryManager = manager;
    }

    public abstract void reload();

    public static LocalizationManager get() throws NoLocalizationManagerException {
        if (primaryManager == null) throw new NoLocalizationManagerException();
        return primaryManager;
    }

    public static Localization get(String key) {
        return get().getLocalization(key);
    }

    public static List<Localization> getAll(List<String> localizationKeys) {
        return get().localizations().getAll(localizationKeys);
    }

    public static Localization find(String key) throws IllegalArgumentException {
        return get().getLocalization(key);
    }

    /**
     * @param key
     * @return the localization for the given key.
     * @throws IllegalArgumentException
     */
    public abstract Localization getLocalization(String key) throws IllegalArgumentException;

    /**
     * Sets the localization for the given key.
     * @param key the key of the localization
     * @param localization the localization to set
     * @return the previous localization for the given key, or null if there was no previous localization.
     * @throws IllegalArgumentException if the key or localization is null.
     * @throws UnexpectedLocalizationStatusException if the localization is not replaceable
     */
    public Localization setLocalization(String key, Localization localization) throws IllegalArgumentException, UnexpectedLocalizationStatusException {
        var localizations = localizations();
        if (key == null) throw new IllegalArgumentException("Key cannot be null.");
        if (localization == null) throw new IllegalArgumentException("Localization cannot be null.");

        if (localizations.containsLocalizationKey(key)) {
            var prevLocalization = localizations.get(key);
            if (prevLocalization.isReplaceableBy(localization))
                return localizations.replace(key, localization);
            else
                throw new UnexpectedLocalizationStatusException(prevLocalization, localization);
        }
        else if (localization.isNew()) {
            //localizations.put(key, localization);
        } else {
            throw new IllegalArgumentException("Localization is not new, but there is no existing mod localization for the given key.");
        }

        return null;
    }

    /**
     * Sets the localization for the given key, with the given text.
     * @param key the key of the localization
     * @param text the text of the localization
     * @return the previous localization for the given key, or null if there was no previous localization.
     * @throws IllegalArgumentException if the key or localization is null.
     * @throws UnexpectedLocalizationStatusException if the localization is not replaceable
     */
    public Localization setLocalization(String key, String text, @NotNull File file) throws IllegalArgumentException, UnexpectedLocalizationStatusException {
        return setLocalization(key, null, text, file);
    }

    /**
     * Sets the localization for the given key, with the given text.
     * @param key the key of the localization
     * @param text the text of the localization
     * @return the previous localization for the given key, or null if there was no previous localization.
     * @throws IllegalArgumentException if the key or localization is null.
     * @throws UnexpectedLocalizationStatusException if the localization is not replaceable
     */
    public Localization setLocalization(String key, Integer version, String text, @NotNull File file) throws IllegalArgumentException, UnexpectedLocalizationStatusException {
        var localizations = localizations();
        if (key == null) throw new IllegalArgumentException("Key cannot be null.");

        if (localizations.containsLocalizationKey(key)) {
            var prevLocalization = localizations.get(key);
            var localization = prevLocalization.replaceWith(text, version, file);
            return localizations.replace(key, localization);
        } else {
            var localization = new Localization(key, version, text, Localization.Status.NEW);
            localizations.add(localization, file);
            return null;
        }
    }

    /**
     * Replaces the localization for the given key with the given text.
     * @param key the key of the localization to replace
     * @throws IllegalArgumentException if the key is null or localization with the given key does not exist.
     * @throws UnexpectedLocalizationStatusException if the localization is not replaceable.
     */
    @NotNull
    public Localization replaceLocalization(String key, String text) throws IllegalArgumentException, UnexpectedLocalizationStatusException {
        var localizations = localizations();
        if (key == null) throw new IllegalArgumentException("Key cannot be null.");

        if (localizations.containsLocalizationKey(key)) {
            var prevLocalization = localizations.get(key);
            var localization = prevLocalization.replaceWith(text);
            // there are maps that support mapping to null, which is why the null check is necessary.
            // (read the docs for the replace method)
            return Objects.requireNonNull(localizations.replace(key, localization));
        }
        else throw new IllegalArgumentException("Localization with the given key does not exist.");
    }

    /**
     * Adds a new localization to the localization list if it does not already exist.
     *
     * @param localization the localization to add
     */
    public abstract void addLocalization(Localization localization, File file) throws LocalizationExistsException;

    public abstract boolean isLocalized(String localizationId);

    /**
     * @return map of localizations and their keys.
     */
    protected abstract LocalizationCollection localizations();

    public List<Localization> localizationList() {
        return localizations().getAll();
    }

    public abstract String titleCapitalize(String trim);

    // todo let user change?
    abstract HashSet<String> capitalizationWhitelist();

    static int numCapitalLetters(String word) {
        if (word == null) {
            return 0;
        }

        int num_cap_letters;
        num_cap_letters = 0;
        for (int j = 0; j < word.length(); j++) {
            if (Character.isUpperCase(word.charAt(j))) {
                num_cap_letters++;
            }
        }
        return num_cap_letters;
    }

    static boolean isAcronym(String word) {
        return numCapitalLetters(word) == word.length();
    }
}
