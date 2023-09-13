package hoi4utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

/**
 * A class for handleing a properties file that will hold the stuff we want to save from the users input
 * todo WIP!!!
 * ! ALL HAIL DOCUMENTATION -> https://www.codejava.net/coding/reading-and-writing-configuration-for-java-application-using-properties-class
 */
public class HOIIVUtilsProp {
	public static final File configFile = new File(HOIIVFile.usersDocuments + File.separator + "HOIIVUtils" + File.separator + "config.properties");
	public static final File configFolder = new File(HOIIVFile.usersDocuments + File.separator + "HOIIVUtils");

	/**
	 * creates the folder HOIIVUtils in Documents
	 * todo create the config file for the Properties Class to use?
	 *  
	 */
	void createHOIIVUtilsConfig() {
		if (!configFolder.exists() || !configFolder.isDirectory()) {
			configFolder.mkdir();
		}

	}

	/**
	 * sets the default for all properties when first time setup is complete and the user has saved data
	 * aka we will be using the setProperty method to take the users input on the settings window and updating the file property by proptery until all proptery are set
	 * but before running those we will initialize so when we do use setProperty it can't be null unless we don't add a default here in init 
	 * todo
	 */
	void initializeHOIIVUtilsConfig() {
		try {
			Properties props = new Properties(); 
			// todo these will setup default keys and values so add them in a list here
			props.setProperty("DEV_MODE", "false");
			props.setProperty("SKIP_SETTINGS", "false");

			FileWriter writer = new FileWriter(configFile);
			
			props.store(writer, "Dev Mode, Displays dev stuff");
			
			writer.close();
		} catch (FileNotFoundException ex) {
			// file does not exist
		} catch (IOException ex) {
			// I/O erros
		}
	}


	/** 
	 * todo Updates   a    property in the file base on the injected
	 * @param key
	 * @param value
	 */
	void setProperty(String key, String value) {
		try {
			Properties props = new Properties();

			props.setProperty(key, value);

			FileWriter writer = new FileWriter(configFile);
			
			props.store(writer, "host settings");
			
			writer.close();
		} catch (FileNotFoundException ex) {
			// file does not exist
		} catch (IOException ex) {
			// I/O erros
		}
	}

	/**
	 * todo gets   a   property
	 * @param key
	 * @param value
	 */
	void getProperty(String key, String value) {
		try {
			Properties props = new Properties();

			props.setProperty(key, value);
			
			FileWriter writer = new FileWriter(configFile);

			props.getProperty(key);

			writer.close();
		} catch (FileNotFoundException ex) {
			// file does not exist
		} catch (IOException ex) {
			// I/O erros
		}
	}
}
