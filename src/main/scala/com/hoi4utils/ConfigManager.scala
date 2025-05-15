package com.hoi4utils

import org.apache.logging.log4j.LogManager

import java.io.*
import java.nio.file.{Path, Paths}
import java.util.Properties

class ConfigManager {
  private val LOGGER = LogManager.getLogger(this.getClass)
  val changeNotifier = new PublicFieldChangeNotifier(this.getClass)

  /**
   * @return Configured HOIIVUtils configuration for use by the application
   */
  def createConfig: Config = {
    val jarPath = Paths.get(this.getClass.getProtectionDomain.getCodeSource.getLocation.toURI).toAbsolutePath
    val hDir = jarPath.getParent.getParent
    val hPropertiesPath = Paths.get {
      s"$hDir${File.separator}HOIIVUtils.properties"
    }.toAbsolutePath
    val hPropertiesJarResource = this.getClass.getClassLoader.getResourceAsStream("HOIIVUtils.properties")
    val hProperties = new Properties()
    new Config(hDir, hPropertiesPath, hPropertiesJarResource, hProperties)
  }

  def saveConfiguration(config: Config): Unit = {
    val propertiesPath = config.getPropertiesPath
    val defaultProperties = config.getPropertiesJarResource
    val hProperties = config.getProperties
    val externalFile = propertiesPath.toFile
    val noSavedSettings = !(externalFile.exists) && defaultProperties != null
    if (noSavedSettings) loadDefaultProperties(config)
    try {
      val output = new FileOutputStream(externalFile)
      try {
        LOGGER.debug("Configuration saved to: {}", externalFile.getAbsolutePath)
        hProperties.store(output, "HOIIVUtils Configuration")
      } catch {
        case e: IOException =>
          LOGGER.error("Failed to save configuration", e)
          throw new RuntimeException(e)
      } finally if (output != null) output.close()
    }
  }

  def loadConfiguration(config: Config): Unit = {
    val propertiesPath = config.getPropertiesPath
    val externalFile = propertiesPath.toFile

    if (!externalFile.exists) {
      LOGGER.warn("External configuration file not found: {}", propertiesPath)
      loadDefaultProperties(config)
    }
    try {
      val input = new FileInputStream(externalFile)
      try {
        LOGGER.debug("External configuration loaded from: {}", propertiesPath)
        config.getProperties.load(input)
      } catch {
        case e: IOException =>
          LOGGER.fatal("Error loading external properties from: {}", propertiesPath, e)
          loadDefaultProperties(config)
      } finally if (input != null) input.close()
    }
  }

  def loadDefaultProperties(config: Config): Unit = {
    val propertiesPath = config.getPropertiesPath
    val defaultProperties = config.getPropertiesJarResource
    val externalFile = propertiesPath.toFile
    try {
      val externalOut = new FileOutputStream(externalFile)
      try {
        val buffer = new Array[Byte](8192)
        var bytesRead = 0
        while ({bytesRead = defaultProperties.read(buffer); bytesRead != -1}) {
          externalOut.write(buffer, 0, bytesRead)
        }
        LOGGER.debug("Default properties copied to: {}", externalFile.getAbsolutePath)
      } catch {
        case e: IOException =>
          LOGGER.error("Failed to copy default properties to external file", e)
      } finally {
        externalOut.close()
      }
    } catch {
      case e: IOException =>
        LOGGER.error("Failed to create output file: {}", externalFile.getAbsolutePath, e)
    }
  }
}
