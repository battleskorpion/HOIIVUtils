package hoi4_localization.localization;

import java.io.File;

public final class LocalizationFile {
    private static File localizationFile = null;

    public LocalizationFile(File file) {
        localizationFile = file;
    }

    public String toString() {
        if (localizationFile != null) {
            return localizationFile.toString();
        }
        return super.toString();
    }
}
