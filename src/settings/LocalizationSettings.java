package settings;

import java.io.*;
import java.util.HashMap;
import java.util.Scanner;

public class LocalizationSettings {

    public enum Settings {
        MOD_DIRECTORY,
        CURRENT_MOD,
    }

    private File settings_file = new File("src\\settings\\localization_settings.txt");
    private FileWriter settingsWriter = new FileWriter(settings_file, false);		// true = append
    private BufferedWriter settingsBWriter = new BufferedWriter(settingsWriter);
    private PrintWriter settingsPWriter;// = new PrintWriter(settingsBWriter); 		        // for println syntax
    private static HashMap<Settings, String> settingValues = new HashMap<>();

    public LocalizationSettings() throws IOException {
        readSettings();
    }

    public void readSettings() {
        try {
            Scanner settingReader = new Scanner(settings_file);

            /* if file is empty then write blank settings to new settings file */
            if (settings_file.length() == 0) {
                writeBlankSettings();
                return;
            }

            /* read settings */
            while(settingReader.hasNextLine()) {
                String[] readSetting = settingReader.nextLine().split(";");
                Settings setting = Settings.valueOf(readSetting[0]);

                settingValues.put(setting, readSetting[1]);
            }

        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public void writeSettings() {
        settingsPWriter = new PrintWriter(settingsBWriter);

        for (Settings setting : Settings.values()) {
            settingsPWriter.println(setting.name() + ";" + settingValues.get(setting));
        }

        settingsPWriter.close();
    }

    public void writeBlankSettings() {
        settingsPWriter = new PrintWriter(settingsBWriter);

        for (Settings setting : Settings.values()) {
            settingsPWriter.println(setting.name() + ";" + "null");
        }

        settingsPWriter.close();
    }

    public boolean isNull(Settings modDirectory) {
        return settingValues.get(modDirectory).equals("null");
    }
}
