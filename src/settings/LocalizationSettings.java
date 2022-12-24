package settings;

import java.io.*;

public class LocalizationSettings {
    File settings_file = new File("src\\settings\\localization_settings");
    FileWriter settingsWriter = new FileWriter(settings_file, true);		// true = append
    BufferedWriter settingsBWriter = new BufferedWriter(settingsWriter);
    PrintWriter settingsPWriter = new PrintWriter(settingsBWriter); 		        // for println syntax


    public LocalizationSettings() throws IOException {

    }
}
