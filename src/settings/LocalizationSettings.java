package settings;

import java.io.*;
import java.util.HashMap;
import java.util.Scanner;

public class LocalizationSettings {

    public String get(Settings setting) {
        return settingValues.get(setting);
    }

    public enum Settings {
        MOD_DIRECTORY,
        CURRENT_MOD,        // todo not in use
    }

    private File settings_file;
    private FileWriter settingsWriter;
    private BufferedWriter settingsBWriter;
    private PrintWriter settingsPWriter;// = new PrintWriter(settingsBWriter); 		        // for println syntax
    private static HashMap<Settings, String> settingValues = new HashMap<>();

    public LocalizationSettings() throws IOException {
        settings_file = new File("src\\settings\\localization_settings.txt");

        readSettings();
    }

    public void readSettings() {
        try {
            Scanner settingReader = new Scanner(settings_file);
//            System.out.println(settingReader.nextLine());

            /* if file is empty then write blank settings to new settings file */
            if (!settingReader.hasNext()) {
                writeBlankSettings();
                return;
            }

            /* read settings */
            while(settingReader.hasNextLine()) {
                String[] readSetting = settingReader.nextLine().split(";");
                Settings setting = Settings.valueOf(readSetting[0]);

                settingValues.put(setting, readSetting[1]);
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * @deprecated
     */
    public void writeSettings() throws IOException {
        settingsWriter = new FileWriter(settings_file, false);		// true = append
        settingsBWriter = new BufferedWriter(settingsWriter);
        settingsPWriter = new PrintWriter(settingsBWriter);

        for (Settings setting : Settings.values()) {
            settingsPWriter.println(setting.name() + ";" + settingValues.get(setting));
        }

        settingsPWriter.close();
    }

    public void writeBlankSettings() throws IOException {
        settingsWriter = new FileWriter(settings_file, false);		// true = append
        settingsBWriter = new BufferedWriter(settingsWriter);
        settingsPWriter = new PrintWriter(settingsBWriter);

        for (Settings setting : Settings.values()) {
            settingsPWriter.println(setting.name() + ";" + "null");

            settingValues.put(setting, "null");
        }

        settingsPWriter.close();

    }

    public void saveSettings(Settings setting, String settingValue) throws IOException {
        settingsWriter = new FileWriter(settings_file, false);		// true = append
        settingsBWriter = new BufferedWriter(settingsWriter);
        settingsPWriter = new PrintWriter(settingsBWriter);

        settingValues.put(setting, settingValue);
        for (Settings s : Settings.values()) {
            settingsPWriter.println(s.name() + ";" + settingValues.get(s));
        }

        settingsPWriter.close();
    }

    public boolean isNull(Settings modDirectory) {
        return settingValues.get(modDirectory).equals("null");
    }
}
