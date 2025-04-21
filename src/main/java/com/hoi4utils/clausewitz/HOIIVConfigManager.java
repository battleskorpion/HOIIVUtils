package com.hoi4utils.clausewitz;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;

public class HOIIVConfigManager {
	private static final Logger LOGGER = LogManager.getLogger(HOIIVConfigManager.class);
	private HOIIVUtilsConfig config;
	private String propertiesFile;
	private InputStream defaultProperties;
	
	public HOIIVConfigManager(HOIIVUtilsConfig config) {
		this.config = config;
		this.propertiesFile = config.getPropertiesFile();
		this.defaultProperties = config.getDefaultProperties();
	}
	
	// TODO: make these the methods that HOIIVUtilsInitializer use instead of duplicates

	public void saveConfiguration() {
		File externalFile = new File(propertiesFile);

		boolean noSavedSettings = !externalFile.exists() && defaultProperties != null;
		if (noSavedSettings) {
			loadDefaultProperties();
		}

		try (OutputStream output = new FileOutputStream(externalFile)) {
			LOGGER.debug("Configuration saved to: {}", externalFile.getAbsolutePath());
			config.store(output, "HOIIVUtils Configuration");
		} catch (IOException e) {
			LOGGER.error("Failed to save configuration", e);
			throw new RuntimeException(e);
		}
	}

	public void loadConfiguration() {
		File externalFile = new File(propertiesFile);

		if (!externalFile.exists()) {
			LOGGER.warn("External configuration file not found: {}", propertiesFile);
			loadDefaultProperties();
		}

		try (FileInputStream input = new FileInputStream(externalFile)) {
			LOGGER.debug("External configuration loaded from: {}", propertiesFile);
			config.load(input);
		} catch (IOException e) {
			LOGGER.fatal("Error loading external properties from: {}", propertiesFile, e);
			loadDefaultProperties();
		}
	}
	
	public void loadDefaultProperties() {
		File externalFile = new File(propertiesFile);
		try (OutputStream externalOut = new FileOutputStream(externalFile)) {
			byte[] buffer = new byte[8192];
			int bytesRead;
			while ((bytesRead = defaultProperties.read(buffer)) != -1) {
				externalOut.write(buffer, 0, bytesRead);
			}
			LOGGER.debug("Default properties copied to: {}", externalFile.getAbsolutePath());
		} catch (IOException e) {
			LOGGER.error("Failed to copy default properties to external file", e);
		}
	}
}
