package com.hoi4utils.clausewitz;

import java.io.*;
import java.util.Properties;

/**
 * Configuration class for HOIIVUtils application.
 * Holds all initialized resources and settings.
 */
public class HOIIVUtilsConfig {
	private final File hoi4UtilsDir;
	private final String propertiesFile;
	private final InputStream defaultProperties;
	private final String version;
	private final Properties properties;

	public HOIIVUtilsConfig(
		File hoi4UtilsDir, 
		String propertiesFile, 
		InputStream defaultProperties,
		String version,
		Properties properties) {
		this.hoi4UtilsDir = hoi4UtilsDir;
		this.propertiesFile = propertiesFile;
		this.defaultProperties = defaultProperties;
		this.version = version;
		this.properties = properties;
	}

	public File getHoi4UtilsDir() {
		return hoi4UtilsDir;
	}
	
	public String getPropertiesFile() {
		return propertiesFile;
	}
	
	public InputStream getDefaultProperties() {
		return defaultProperties;
	}

	public String getVersion() {
		return version;
	}

	public String getProperty(String key) {
		return properties.getProperty(key);
	}

	public void setProperty(String key, String value) {
		properties.setProperty(key, value);
	}

	void load(FileInputStream input) throws IOException {
		properties.load(input);
	}

	void store(OutputStream output, String hoiivUtilsConfiguration) throws IOException {
		properties.store(output, hoiivUtilsConfiguration);
	}
}