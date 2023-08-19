package settings;

import java.io.*;
import java.util.HashMap;
import java.util.Scanner;

import hoi4utils.HOIIVUtils;
/*
 * MapgenSettings File
 */
public class MapgenSettings {

    public String get(Settings setting) { return settingValues.get(setting); }

    public enum Settings {
        HEIGHTMAP_NAME,         // "heightmap.bmp";
        STATE_BORDERS_NAME,     // "state_borders.bmp";
        GENERATION_TYPE,
        HEIGHTMAP_SEA_LEVEL,    // 95
        IMAGE_WIDTH,            // 4608, 5632 default
        IMAGE_HEIGHT,           // 2816, 2048 - default
        NUM_SEEDS_Y,            // 64
        NUM_SEEDS_X,            // 64
    }

    private File settings_file;
    private FileWriter settingsWriter;
    private BufferedWriter settingsBWriter;
    private PrintWriter settingsPWriter;// = new PrintWriter(settingsBWriter); 		        // for println syntax
    private static HashMap<MapgenSettings.Settings, String> settingValues = new HashMap<>();

    public MapgenSettings() throws IOException {
        String user_docs_path = System.getProperty("user.home") + File.separator + "Documents";
        String hoi4localizer_path = user_docs_path + File.separator + "hoi4localizer";
        new File(hoi4localizer_path).mkdir();
        settings_file = new File(hoi4localizer_path + File.separator + "mapgen_settings.txt");
        settings_file.createNewFile();

        readSettings();
    }

    public void readSettings() {
        Scanner settingReader = null;
        try {
            settingReader = new Scanner(settings_file);
//            System.out.println(settingReader.nextLine());

            /* if file is empty then write blank settings to new settings file */
            if (!settingReader.hasNext()) {
                writeBlankSettings();
                settingReader.close();
                return;
            }

            /* read settings */
            while(settingReader.hasNextLine()) {
                String[] readSetting = settingReader.nextLine().split(";");
                MapgenSettings.Settings setting = MapgenSettings.Settings.valueOf(readSetting[0]);

                settingValues.put(setting, readSetting[1]);
            }

        } catch (Exception exception) {
            HOIIVUtils.openError(exception);
            return;
        }
        settingReader.close();

    }

    public void writeBlankSettings() throws IOException {
        settingsWriter = new FileWriter(settings_file, false);		// true = append
        settingsBWriter = new BufferedWriter(settingsWriter);
        settingsPWriter = new PrintWriter(settingsBWriter);

        for (MapgenSettings.Settings setting : MapgenSettings.Settings.values()) {
            settingsPWriter.println(setting.name() + ";" + "null");

            settingValues.put(setting, "null");
        }

        settingsPWriter.close();

    }

    public void saveSettings(MapgenSettings.Settings setting, String settingValue) throws IOException {
        settingsWriter = new FileWriter(settings_file, false);		// true = append
        settingsBWriter = new BufferedWriter(settingsWriter);
        settingsPWriter = new PrintWriter(settingsBWriter);

        settingValues.put(setting, settingValue);
        for (HOIIVUtilsProperties.Settings s : HOIIVUtilsProperties.Settings.values()) {
            settingsPWriter.println(s.name() + ";" + settingValues.get(s));
        }

        settingsPWriter.close();
    }

}
