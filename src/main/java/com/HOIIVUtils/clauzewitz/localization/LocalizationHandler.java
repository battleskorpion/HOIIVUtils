package com.HOIIVUtils.clauzewitz.localization;

import com.HOIIVUtils.clauzewitz.exceptions.IllegalLocalizationFileTypeException;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class LocalizationHandler {
    public static final String LOCALIZATION_FILE_EXTENSION = ".yml";
    private List<LocalizationFile> locFiles;

    private LocalizationHandler() {
        locFiles = new ArrayList<>();
    }

    private static final class InstanceHolder {
        private static final LocalizationHandler instance = new LocalizationHandler();
    }

    public static LocalizationHandler getInstance() {
        return InstanceHolder.instance;
    }

    public void readLocalization(File localizationFolder) {
        if (!localizationFolder.exists() || !localizationFolder.isDirectory()) {
            System.err.println("Localization folder is not a directory or does not exist: " + localizationFolder.getAbsolutePath());
            return;
        }

        File[] files = localizationFolder.listFiles((dir, name) -> name.endsWith(LOCALIZATION_FILE_EXTENSION));
        if (files == null) {
            System.err.println("Localization folder is not a directory: " + localizationFolder.getAbsolutePath());
            return;
        }

        for (File file : files) {
            try {
                LocalizationFile localizationFile = new LocalizationFile(file);
                localizationFile.read();
                //List<Localization> localizations = localizationFile.localizationList;
                locFiles.add(localizationFile);
            } catch (IllegalLocalizationFileTypeException e) {
                System.err.println(
                        "Possibly invalid localization file type: " + file.getName());
            }
        }
    }

//    public String getLocalizedString(String key) {
//        return localizationData.getOrDefault(key, key);
//    }
//
//    public Map<String, String> getAllLocalizationData() {
//        return new HashMap<>(localizationData);
//    }

}
