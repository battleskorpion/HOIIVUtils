package hoi4_localization.localization;

import java.io.File;

public final class LocalizationFile {
    private final File localizationFile;

    public LocalizationFile(File file) {
        localizationFile = file;
    }

    public File getLocalizationFile() { return localizationFile; }

    public String toString() {
        if (localizationFile != null) {
            return localizationFile.toString();
        }
        return super.toString();
    }
}
