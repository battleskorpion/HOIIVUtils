package com.HOIIVUtils.hoi4utils.clausewitz_data.localization;

import com.HOIIVUtils.hoi4utils.FileUtils;
import com.HOIIVUtils.ui.FXWindow;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class LocalizationFile_new extends File {
    private static final String l_english = "l_english:";
    private static String loc_key = ":0";
    private static String language_def = l_english;
    protected List<Localization> localizationList;

    public LocalizationFile_new(@NotNull String pathname) {
        super(pathname);

        if (!pathname.endsWith(".yml")) {
            throw new IllegalArgumentException("Localization files can only be of type .yml");
        }

        localizationList = new ArrayList<>();
    }

    public LocalizationFile_new(@NotNull URI uri) {
        super(uri);

        if (!uri.getPath().endsWith(".yml")) {
            throw new IllegalArgumentException("Localization files can only be of type .yml");
        }

        localizationList = new ArrayList<>();
    }

    public LocalizationFile_new(File file) throws IllegalArgumentException {
        super(file.toURI());

        if (!file.getPath().endsWith(".yml")) {
            throw new IllegalArgumentException("Localization files can only be of type .yml");
        }

        localizationList = new ArrayList<>();
    }

    public File getFile() { return this; }

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
        while(!found_lang) {
            if (reader.hasNextLine()) {
                String data = reader.nextLine().replaceAll("\\s", "");
//				line++;
                if (FileUtils.usefulData(data)) {
                    if (data.trim().contains("l_")) {
                        if(!data.contains(language_def)) {
                            // todo
                            System.err.println("language not english, not presently supported.");
                            return;
                        }
                        found_lang = true;
                    }
                }
            }
            else {
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
                if (data.contains(loc_key)) {
                    String id = data.substring(0, data.indexOf(loc_key)).trim();
                    String text = data.substring(data.indexOf("\""), data.lastIndexOf("\"") + 1).trim();
                    if (text.charAt(text.length() - 1) == '\n') {
                        text = text.substring(0, text.length() - 1);
                    }
                    if (text.isEmpty()) {
                        continue;
                    }
                    if (text.charAt(0) == '\"') {
                        text = text.substring(1);
                    }
                    if (text.isEmpty()) {
                        continue;
                    }
                    if (text.charAt(text.length() - 1) == '\"') {
                        text = text.substring(0, text.length() - 1);
                    }
                    if (text.isEmpty()) {
                        //continue;                   // NO WHY WOULD WE DO THIS AAAAAAAAAA EMPTY LOCALIZATION SOMETIMES IS FINE*
                    }
                    text = text.replaceAll("(Â§)", "§");
                    Localization localization = new Localization(id, text, Localization.Status.EXISTS);
                    localizationList.add(localization);
                    // print to statistics?
//					System.out.println("localization: " + text);
                } else if (data.contains(":")) {
                    System.err.println("Fix incorrect loc key: " + data);
                }
            }
        }
        reader.close();
    }
}
