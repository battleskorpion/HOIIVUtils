package hoi4_coding.localization;

import java.io.File;

public final class LocalizationFile extends File {

    public LocalizationFile(File file) {
        super(file.toURI());
    }

    public File getFile() { return this; }

    public String toString() {
        if (super.isFile()) {
            return super.toString();
        }
        return super.toString();
    }
}
