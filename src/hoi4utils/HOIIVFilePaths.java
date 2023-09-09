package hoi4utils;

import static hoi4utils.Settings.MOD_PATH;

import java.io.File;

/**
 * HOIIV File Paths
 * A Everything to do with the files that are found in a HOI 4 mod
 */
public class HOIIVFilePaths {

	public static String modPath = SettingsManager.get(MOD_PATH);

	public static void createHOIIVFilePaths() {

		if (Settings.DEV_MODE.enabled()) {
			System.out.println(modPath);
		}
		
		HOIIVUtils.common_folder = new File(modPath + "\\common");
		HOIIVUtils.states_folder = new File(modPath + "\\history\\states");
		HOIIVUtils.strat_region_dir =  new File(modPath + "\\map\\strategicregions");
		HOIIVUtils.localization_eng_folder =  new File(modPath + "\\localisation\\english");
		HOIIVUtils.focus_folder = new File(modPath + "\\common\\national_focus");
	}
	
}
