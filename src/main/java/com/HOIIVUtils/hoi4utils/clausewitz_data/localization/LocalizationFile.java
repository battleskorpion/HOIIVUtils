package com.HOIIVUtils.hoi4utils.clausewitz_data.localization;

import com.HOIIVUtils.hoi4utils.FileUtils;
import com.HOIIVUtils.hoi4utils.ioexceptions.IllegalLocalizationFileTypeException;
import com.HOIIVUtils.ui.FXWindow;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;

public class LocalizationFile extends File {
    private static final String l_english = "l_english:";
    private static String loc_key = ":0"; // todo this just the default each loc could have its own
    private static String language_def = l_english;
    protected List<Localization> localizationList;
    private static final String LOCALIZATION_FILE_TYPE_ERROR = "Localization files can only be of type .yml";

    public LocalizationFile(@NotNull String pathname) throws IllegalLocalizationFileTypeException {
        super(pathname);

        // todo are other file endings allowed for yaml/hoi4?
        if (!pathname.endsWith(".yml")) {
            throw new IllegalLocalizationFileTypeException(LOCALIZATION_FILE_TYPE_ERROR);
        }

        localizationList = new ArrayList<>();
    }

    public LocalizationFile(@NotNull URI uri) throws IllegalLocalizationFileTypeException {
        super(uri);

        if (!uri.getPath().endsWith(".yml")) {
            throw new IllegalLocalizationFileTypeException(LOCALIZATION_FILE_TYPE_ERROR);
        }

        localizationList = new ArrayList<>();
    }

    public LocalizationFile(File file) throws IllegalLocalizationFileTypeException {
        super(file.toURI());

        if (!file.getPath().endsWith(".yml")) {
            throw new IllegalLocalizationFileTypeException(LOCALIZATION_FILE_TYPE_ERROR);
        }

        localizationList = new ArrayList<>();
    }

    public File getFile() {
        return this;
    }

    @Override
    public String toString() {
        if (super.isFile()) {
            return super.toString();
        }
        return super.toString();
    }

    public void read() {
        if (!exists()) {
            return;
        }

        localizationList.clear();

        /* file reader */
        Scanner reader = null;
        try {
            reader = new Scanner(this);
        } catch (Exception exception) {
            FXWindow.openGlobalErrorWindow(exception);
        }
        if (reader == null) {
            return;
        }

        /* language declaration */
        boolean found_lang = false;
        while (!found_lang) {
            if (reader.hasNextLine()) {
                String data = reader.nextLine().replaceAll("\\s", "");
                // line++;
                if (FileUtils.usefulData(data)) {
                    if (data.trim().contains("l_")) {
                        if (!data.contains(language_def)) {
                            // todo
                            System.err.println("language not english, not presently supported.");
                            return;
                        }
                        found_lang = true;
                    }
                }
            } else {
                break;
            }
        }

        /* exit if no loc */
        if (!reader.hasNextLine()) {
            return;
        }

        while (reader.hasNextLine()) {
            String data = reader.nextLine().trim();

            if (FileUtils.usefulData(data)) {
                try {
                    String id = null;
                    String text = null;

                    if (data.contains(loc_key)) {
                        int locKeyIndex = data.indexOf(loc_key);
                        id = data.substring(0, locKeyIndex).trim();
                        int startQuoteIndex = data.indexOf("\"", locKeyIndex);
                        int endQuoteIndex = data.lastIndexOf("\"");

                        if (startQuoteIndex != -1 && endQuoteIndex != -1 && startQuoteIndex < endQuoteIndex) {
                            text = data.substring(startQuoteIndex + 1, endQuoteIndex).trim();
                        } else {
                            System.err.println("Invalid quote indices: startQuoteIndex=" + startQuoteIndex
                                    + ", endQuoteIndex=" + endQuoteIndex);
                            System.err.println("\t-> data: " + data);
                        }
                    } else if (data.contains(":")) {
                        int colonIndex = data.indexOf(":");
                        id = data.substring(0, colonIndex).trim();
                        int startQuoteIndex = data.indexOf("\"", colonIndex);
                        int endQuoteIndex = data.lastIndexOf("\"");

                        if (startQuoteIndex != -1 && endQuoteIndex != -1 && startQuoteIndex < endQuoteIndex) {
                            text = data.substring(startQuoteIndex + 1, endQuoteIndex).trim();
                        } else {
                            System.err.println("Invalid quote indices: startQuoteIndex=" + startQuoteIndex
                                    + ", endQuoteIndex=" + endQuoteIndex);
                            System.err.println("\t-> data: " + data);
                        }
                    }

                    if (id != null && text != null) {
                        text = text.replaceAll("(Â§)", "§");
                        if (text.startsWith("\"")) {
                            text = text.substring(1);
                        }
                        if (text.endsWith("\"")) {
                            text = text.substring(0, text.length() - 1);
                        }
                        Localization localization = new Localization(id, text, Localization.Status.EXISTS);
                        localizationList.add(localization);
                    }
                } catch (StringIndexOutOfBoundsException e) {
                    System.err.println("String index out of bounds for data: " + data);
                    e.printStackTrace();
                }
            }
        }
        reader.close();
    }

    public void writeLocalization() throws IOException {
        /* load file data, before writer init so data not disappeared */
        Scanner scanner = new Scanner(this);
        StringBuilder fileBuffer = new StringBuilder();
        try {
            while (scanner.hasNextLine()) {
                fileBuffer.append(scanner.nextLine()).append(System.lineSeparator());
            }

            FileWriter writer = new FileWriter(this, false);
            BufferedWriter BWriter = new BufferedWriter(writer);
            PrintWriter PWriter = new PrintWriter(BWriter); // for println syntax

            for (Localization localization : localizationList) {
                if (localization.status() == Localization.Status.UPDATED) {
                    /* replace loc */
                    int start = fileBuffer.indexOf(localization.ID());
                    if (start < 0) {
                        System.err.println("Start of localization id is negative!");
                    }
                    int temp = fileBuffer.indexOf("\"", start);
                    int end = 1;
                    // end char must be literally " and not \"
                    do {
                        end = fileBuffer.indexOf("\"", temp + 1);
                    } while (fileBuffer.charAt(end - 1) == '\\');
                    if (end < 0) {
                        System.err.println("end of localization id is negative!");
                    }

                    String loc = localization.toString();
                    loc = loc.replaceAll("§", "Â§"); // necessary with UTF-8 BOM
                    fileBuffer.replace(start, end + 1, loc);
                    System.out.println("replaced " + localization.ID());
                } else if (localization.status() == Localization.Status.NEW) {
                    /* append loc */
                    String loc = localization.toString();
                    loc = loc.replaceAll("§", "Â§"); // necessary with UTF-8 BOM
                    fileBuffer.append("\t").append(loc).append(System.lineSeparator());
                    System.out.println("append " + localization.ID());
                } else {
                    // System.out.println("no change " + localization.ID());
                }
            }

            /* print */
            PWriter.print(fileBuffer);
            PWriter.close();
            scanner.close();
        } catch (Exception exception) {
            FXWindow.openGlobalErrorWindow(exception);
        }
    }

    /**
     * Adds a new localization to the localization list if it doesn't already exist.
     *
     * @param newLocalization the localization to be added
     */
    public void addLocalization(Localization newLocalization) {
        if (newLocalization == null) {
            return;
        }

        for (Localization loc : localizationList) {
            if (loc.ID().equals(newLocalization.ID())) {
                System.err.println("Localization " + newLocalization.ID() + " already exists.");
                // dont overwrite // todo - for now
                return;
            }
        }

        localizationList.add(newLocalization);
    }

    /**
     * Gets localization from this file corresponding to the ID
     * 
     * @param localizationId
     * @return Localization corresponding with ID, or null if not found
     */
    public Localization getLocalization(String localizationId) {
        if (localizationId == null) {
            System.err.println("Null localization ID in " + this);
            return null;
        }

        for (Localization loc : localizationList) {
            if (loc.ID().equals(localizationId)) {
                return loc;
            }
        }

        return null;
    }

    /**
     * Sets the localization for the given key with the provided text. If a
     * localization with the same key already exists,
     * it is updated with the new text. Otherwise, a new localization is created and
     * added to the list.
     *
     * @param key  the identifier of the localization
     * @param text the text to set for the localization
     * @return the updated or newly created localization
     */
    public Localization setLocalization(String key, String text) {
        ArrayList<Localization> listTemp = new ArrayList<>(localizationList);

        for (Localization loc : localizationList) {
            if (loc.ID().equals(key)) {
                Localization newLocalization = new Localization(key, text, Localization.Status.UPDATED);

                listTemp.remove(loc); // record is final
                listTemp.add(newLocalization);
                localizationList = listTemp;

                return newLocalization;
            }
        }

        Localization newLocalization = new Localization(key, text, Localization.Status.NEW);
        localizationList.add(newLocalization);

        return newLocalization;
    }

    /**
     * Checks if the given localization ID is localized.
     *
     * @param localizationId the ID of the localization to check
     * @return true if the localization is localized, false otherwise
     */
    public boolean isLocalized(String localizationId) {
        Localization loc = getLocalization(localizationId);
        return (loc != null && loc.status() != Localization.Status.DEFAULT);
    }

    /**
     * Sets the localization for the given ID. If the localization already exists,
     * it is updated.
     * If it does not exist, it is added to the list.
     *
     * @param localization the localization to set or update
     */
    public void setLocalization(Localization localization) {
        int i = 0;
        for (Localization l : localizationList) {
            if (l.ID().equals(localization.ID())) {
                localizationList.remove(i);
                localizationList.add(i, localization);
                return;
            }
            i++;
        }

        localizationList.add(localization);
    }

    /**
     * Checks if this object is equal to the given object.
     *
     * @param obj the object to compare to
     * @return true if the objects are equal, false otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        LocalizationFile other = (LocalizationFile) obj;
        return Objects.equals(getAbsolutePath(), other.getAbsolutePath());
    }

    /**
     * Calculates and returns a hash code value for the object. The hash code is
     * generated using the absolute path of the object's file.
     *
     * @return the hash code value of the object's absolute path
     */
    @Override
    public int hashCode() {
        return Objects.hash(getAbsolutePath());
    }
}
