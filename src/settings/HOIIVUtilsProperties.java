package settings;

import java.io.*;
import java.util.HashMap;
import java.util.Scanner;

public class HOIIVUtilsProperties {

    public static String get(Settings setting) { return settingValues.get(setting); }

    public enum Settings {
        MOD_DIRECTORY,
        CURRENT_MOD,        // todo not in use
        CIVILIAN_MILITARY_FACTORY_MAX_RATIO,            // ratio for civ/mil factories highlight in buildings view
        DARK_MODE {
            public Object getSetting() {
                return settingValues.get(this).equals("true");
            }
        },
        DEV_MODE {
            public Object getSetting() {
                return settingValues.get(this).equals("true");
            }
        },
        PREFERRED_SCREEN,
        ;

        public void setSetting(String set) {
            settingValues.put(this, String.valueOf(set));
            try {
                writeSettings();
                if ((boolean)DEV_MODE.getSetting()) {
                    System.out.println("Updated setting " + this.name() + ": " + settingValues.get(this));
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return;
        }

        public Object getSetting() {
            return settingValues.get(this);
        }
    }

    private static File settings_file;
    private static FileWriter settingsWriter;
    private static BufferedWriter settingsBWriter;
    private static PrintWriter settingsPWriter;// = new PrintWriter(settingsBWriter); 		        // for println syntax
    private static HashMap<Settings, String> settingValues = new HashMap<>();

    public HOIIVUtilsProperties() throws IOException {
        String user_docs_path = System.getProperty("user.home") + File.separator + "Documents";
        String hoi4UtilsPropertiesPath = user_docs_path + File.separator + "HOIIVUtils";
        new File(hoi4UtilsPropertiesPath).mkdir();
        settings_file = new File(hoi4UtilsPropertiesPath + File.separator + "HOIIVUtils_properties.txt");
        settings_file.createNewFile();

        readSettings();
    }

    public HOIIVUtilsProperties(Boolean firsttimeuser) throws IOException {
        if (firsttimeuser) {

        }
    }

    public static void readSettings() {
        try {
            Scanner settingReader = new Scanner(settings_file);
//            System.out.println(settingReader.nextLine());

            /* if file is empty then write blank settings to new settings file */
            if (!settingReader.hasNext()) {
                writeBlankSettings();
                return;
            }

            /* read settings */
            while (settingReader.hasNextLine()) {
                String[] readSetting = settingReader.nextLine().split(";");
                Settings setting = Settings.valueOf(readSetting[0]);

                settingValues.put(setting, readSetting[1]);
            }

            for (Settings setting : Settings.values()) {
                if (!settingValues.containsKey(setting)) {
                    writeBlankSetting(setting);
                }
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * @deprecated
     */
    public static void writeSettings() throws IOException {
        settingsWriter = new FileWriter(settings_file, false);		// true = append
        settingsBWriter = new BufferedWriter(settingsWriter);
        settingsPWriter = new PrintWriter(settingsBWriter);

        for (Settings setting : Settings.values()) {
            settingsPWriter.println(setting.name() + ";" + settingValues.get(setting));
        }

        settingsPWriter.close();
    }

    private static void writeBlankSetting(Settings setting) throws IOException {
        settingsWriter = new FileWriter(settings_file, true);		// true = append
        settingsBWriter = new BufferedWriter(settingsWriter);
        settingsPWriter = new PrintWriter(settingsBWriter);

        settingsPWriter.println(setting.name() + ";" + "null");

        settingsPWriter.close();
    }

    public static void writeBlankSettings() throws IOException {
        settingsWriter = new FileWriter(settings_file, false);		// true = append
        settingsBWriter = new BufferedWriter(settingsWriter);
        settingsPWriter = new PrintWriter(settingsBWriter);

        for (Settings setting : Settings.values()) {
            settingsPWriter.println(setting.name() + ";" + "null");

            settingValues.put(setting, "null");
        }

        settingsPWriter.close();

    }

    public static void saveSettings(Settings setting, String settingValue) throws IOException {
        settingsWriter = new FileWriter(settings_file, false);		// true = append
        settingsBWriter = new BufferedWriter(settingsWriter);
        settingsPWriter = new PrintWriter(settingsBWriter);

        settingValues.put(setting, settingValue);
        for (Settings s : Settings.values()) {
            settingsPWriter.println(s.name() + ";" + settingValues.get(s));
        }

        settingsPWriter.close();
    }

    public static boolean isNull(Settings modDirectory) {
        return settingValues.get(modDirectory).equals("null");
    }
}
