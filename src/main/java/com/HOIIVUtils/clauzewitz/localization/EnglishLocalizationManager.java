package com.HOIIVUtils.clauzewitz.localization;

import com.HOIIVUtils.FileUtils;
import com.HOIIVUtils.clauzewitz.HOIIVFile;
import com.HOIIVUtils.clauzewitz.exceptions.IllegalLocalizationFileTypeException;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class EnglishLocalizationManager extends LocalizationManager implements FileUtils {
    protected static final String l_english = "l_english:";
    protected static final String versionNumberRegex = ":(\\d*)";    //  (version number is optional)
    protected static final String language_def = l_english;
    /**
    a record is final, so this is fine. The key will always be the same as the localization key
    from when it is initially put in the map.
     */
    protected final LocalizationCollection localizationCollection = new LocalizationCollection();

    public EnglishLocalizationManager() {
        super();
        setManager(this);
    }

//    public static void setLanguage(String language) {
//        language_def = "l_" + language;
//    }

    public void reload() {
        localizationCollection.clear();
        // load all localization files within the localization folder and
        // any subfolders
        loadLocalization();
    }

    protected void loadLocalization() {
        if (HOIIVFile.mod_localization_folder == null) {
            System.err.println("Localization folder unknown.");
            return;
        }
        loadLocalization(HOIIVFile.mod_localization_folder);
        // todo vanilla folder
    }

    protected void loadLocalization(File localizationFolder) {
        File[] files = localizationFolder.listFiles();
        if (files == null) {
            return;
        }
        for (File file : files) {
            if (file.isDirectory()) {
                loadLocalization(file);
            } else {
                if (file.getName().endsWith(".yml")) {
                    loadLocalizationFile(file);
                } else {
                    System.out.println("Localization files can only be of type .yml. File: " + file.getAbsolutePath());
                }
            }
        }
    }

    protected void loadLocalizationFile(File file) {
        try (Scanner scanner = new Scanner(file)) {
            // check language
            boolean languageFound = false;
            while (scanner.hasNextLine() && !languageFound) {
                String line = scanner.nextLine();
                /* ignore BOM */
                if (line.startsWith("\uFEFF")) {
                    line = line.substring(1);
                }
                if (!FileUtils.usefulData(line)) continue;
                if (!line.trim().startsWith(language_def)) {
                    // todo we would have optional logging
                    System.out.println("Localization file is not in English: " + file.getAbsolutePath());
                    return;
                } else {
                    languageFound = true;
                }
            }
            if (!languageFound) {
                System.out.println("Localization file does not have a language definition: " + file.getAbsolutePath());
                return;
            }

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (!FileUtils.usefulData(line)) continue;
                String[] data = line.splitWithDelimiters(versionNumberRegex, 2);
                if (data.length != 3) {
                    System.err.println("Invalid localization file format: " + file.getAbsolutePath()
                            + ".\n" + "line: " + line);
                    return;
                }
                // trim whitespace
                for (int i = 0; i < data.length; i++) {
                    data[i] = data[i].trim();
                }
                // ignore ":" before version number
                data[1] = data[1].substring(1);
                // ignore trailing comments
                // todo make this better (lazy)
                if (data[2].contains("#")) {
                    // Replace \# with a placeholder
                    data[2] = data[2].replaceAll("\\\\#", "\u0000");
                    if (data[2].contains("#")) {
                        data[2] = data[2].substring(0, data[2].indexOf("#"));
                        data[2] = data[2].trim();
                    }
                    // Replace the placeholder back to \#
                    data[2] = data[2].replace("\u0000", "\\#");
                }

                /*
                .yml example format:
                CONTROLS_GREECE: "Controls all states in the §Y$strategic_region_greece$§! strategic region"
                CONTROLS_ASIA_MINOR:1 "Controls all states in the §Y$strategic_region_asia_minor$§! strategic region"
                 */
                String key;
                Integer version;
                String value;
                if (data[1].isBlank()) {
                    key = data[0];
                    version = null;
                    value = data[2];
                } else {
                    key = data[0];
                    version = Integer.parseInt(data[1]);
                    value = data[2];
                }

                if (value.length() == 1 || !value.startsWith("\"") || !value.endsWith("\"")) {
                    System.err.println("Invalid localization file format: " + file.getAbsolutePath()
                            + ".\n" + "line: " + line);
                    return;
                }
                // fix file format issues (as it is a UTF-8 BOM file) and remove leading/trailing quotes
                value = value.replaceAll("(Â§)", "§").substring(1, value.length() - 1);
                Localization localization = new Localization(key, version, value, Localization.Status.EXISTS);
                localizationCollection.add(localization, file);
            }
        } catch (IOException exc) {
            exc.printStackTrace();
        }
    }

    public void saveLocalization() {
        File localizationFolder = HOIIVFile.mod_localization_folder;
        File[] files = localizationFolder.listFiles();
        if (files == null) {
            return;
        }

        // Load all localization files from the directory
        for (File file : files) {
            if (file.isDirectory()) {
                loadLocalization(file);
            } else if (file.getName().endsWith(".yml")) {
                loadLocalizationFile(file);
            } else throw new IllegalLocalizationFileTypeException(file);
        }

        // Separate new and changed localizations
        var changedLocalizations = localizationCollection.filterByStatus(Localization.Status.UPDATED);
        var newLocalizations = localizationCollection.filterByStatus(Localization.Status.NEW);

        // Save updated and new localizations
        changedLocalizations.forEach(entry -> writeAllLocalization(entry.getValue(), entry.getKey()));
        newLocalizations.forEach(entry -> writeAllLocalization(entry.getValue(), entry.getKey()));

    }


    public void writeAllLocalization(List<Localization> list, File file) {
        for (Localization localization : list) {
            String key = localization.ID();
            String version = localization.version() == null ? "" : String.valueOf(localization.version());
            String value = localization.text();

            switch (localization.status()) {
                case UPDATED -> writeLocalization(file, key, version, value, false);
                case NEW -> writeLocalization(file, key, version, value, true);
                default -> throw new IllegalStateException("Unexpected value: " + localization.status());
            }
        }
    }

    /**
     * Use to replace existing localization with entry
     *
     * @param file
     * @param key
     * @param version
     * @param value
     * @param append
     */
    protected void writeLocalization(File file, String key, String version, String value, boolean append) {
        String entry = key + ":" + version + " \"" + value + "\"";
        entry = entry.replaceAll("§", "Â§");    // necessary with UTF-8 BOM
        if (append) {
            try (PrintWriter writer = getLocalizationWriter(file, true)) {
                writer.println(entry);
            } catch (IOException exc) {
                System.err.println("Failed to write new localization to file. " + "\n\tLocalization: " + entry
                        + "\n\tFile: " + file.getAbsolutePath());
            }
        } else {
            try {
                boolean lineReplaced = false;
                List<String> lines = Files.readAllLines(Paths.get(file.getAbsolutePath()));
                for (int i = 0; i < lines.size(); i++) {
                    if (lines.get(i).trim().startsWith(key)) {
                        lines.set(i, entry);
                        lineReplaced = true;
                        System.out.println("Replaced localization " + key);
                        break;
                    }
                }
                if (!lineReplaced) throw new IOException(); // todo better exception
                Files.write(Paths.get(file.getAbsolutePath()), lines);
            } catch (IOException exc) {
                System.err.println("Failed to update localization in file. " + "\n\tLocalization: " + entry
                        + "\n\tFile: " + file.getAbsolutePath());
            }
        }
    }

    protected PrintWriter getLocalizationWriter(File file, boolean append) throws IOException {
        return new PrintWriter(new BufferedWriter(new FileWriter(file, append)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addLocalization(Localization localization, File file) throws LocalizationExistsException {
        if (localization == null) return;
        if (localizationCollection.containsLocalizationKey(localization.ID())) throw new LocalizationExistsException(localization);
        localizationCollection.add(localization, file);
    }

    /**
     * Finds the localization corresponding to the given key/localization ID.
     *
     * @return the localization corresponding to the given ID, or null if not found.
     */
    @Override
    public Localization getLocalization(String key) throws IllegalArgumentException {
        if (key == null) throw new IllegalArgumentException("localization ID cannot be null");
        return localizationCollection.get(key);
    }

    /**
     * Checks if the given localization ID is localized (has a localization entry).
     *
     * @param localizationId the ID of the localization to check
     * @return true if the localization is localized, false otherwise
     */
    @Override
    public boolean isLocalized(String localizationId) {
        Localization loc = getLocalization(localizationId);
        return (loc != null);
    }

    @Override
    protected LocalizationCollection localizations() {
        return localizationCollection;
    }

    /**
     * Capitalizes every word in a string with a pre-set whitelist
     *
     * @param title
     * @return Returns the edited string unless the string has no words
     */
    @Override
    public String titleCapitalize(String title) {
        if (title == null) {
            return null;
        }
        if (title.trim().isEmpty()) {
            return title;
        }

        ArrayList<String> words = new ArrayList<>(Arrays.asList(title.split(" ")));
        HashSet<String> whitelist = capitalizationWhitelist();

        if (words.get(0).length() == 1) {
            words.set(0, Character.toUpperCase(words.get(0).charAt(0)) + "");
        } else if (words.get(0).length() > 1) {
            words.set(0, Character.toUpperCase(words.get(0).charAt(0))
                    + words.get(0).substring(1));
        } else {
            // todo this should never happen now right?
            System.out.println("first word length < 1");
        }

        System.out.println("num words: " + words.size());
        for (int i = 1; i < words.size(); i++) {
            if (!isAcronym(words.get(i)) && !(whitelist.contains(words.get(i)))) {
                if (words.get(i).length() == 1) {
                    words.set(i, Character.toUpperCase(words.get(i).charAt(0)) + "");
                } else if (words.get(i).length() > 1) {
                    // System.out.println("working cap");
                    words.set(i, Character.toUpperCase(words.get(i).charAt(0))
                            + words.get(i).substring(1));
                }
            }

        }

        System.out.println("capitalized: " + String.join(" ", words));
        return String.join(" ", words);
    }

    // todo let user change?
    @Override
    HashSet<String> capitalizationWhitelist() {
        String[] whitelist = {
                "a",
                "above",
                "after",
                "among", // among us
                "an",
                "and",
                "around",
                "as",
                "at",
                "below",
                "beneath",
                "beside",
                "between",
                "but",
                "by",
                "for",
                "from",
                "if",
                "in",
                "into",
                "nor",
                "of",
                "off",
                "on",
                "onto",
                "or",
                "over",
                "since",
                "the",
                "through",
                "throughout",
                "to",
                "under",
                "underneath",
                "until",
                "up",
                "with",
        };

        // create the whitelist
        return new HashSet<>(List.of(whitelist));
    }

    @Override
    public String toString() {
        return "EnglishLocalizationManager{" +
                "localizations=" + localizationCollection +
                "}";
    }

    @Override
    public int hashCode() {
        return Objects.hash(localizationCollection);
    }
}
