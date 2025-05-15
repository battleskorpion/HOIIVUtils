package com.hoi4utils

import org.apache.logging.log4j.LogManager

import java.io.*

object HOIIVConfigManager {
  private val LOGGER = LogManager.getLogger(classOf[HOIIVConfigManager])
}

class HOIIVConfigManager(private var config: HOIIVUtilsConfig) {
  this.propertiesFile = config.getPropertiesFile
  this.defaultProperties = config.getDefaultProperties
  private var propertiesFile: String = null
  private var defaultProperties: InputStream = null

  private[hoi4utils] def saveConfiguration(): Unit = {
    val externalFile = new File(propertiesFile)
    val noSavedSettings = !(externalFile.exists) && defaultProperties != null
    if (noSavedSettings) loadDefaultProperties()
    try {
      val output = new FileOutputStream(externalFile)
      try {
        HOIIVConfigManager.LOGGER.debug("Configuration saved to: {}", externalFile.getAbsolutePath)
        config.store(output, "HOIIVUtils Configuration")
      } catch {
        case e: IOException =>
          HOIIVConfigManager.LOGGER.error("Failed to save configuration", e)
          throw new RuntimeException(e)
      } finally if (output != null) output.close()
    }
  }

  private def loadConfiguration(): Unit = {
    val externalFile = new File(propertiesFile)
    if (!(externalFile.exists)) {
      HOIIVConfigManager.LOGGER.warn("External configuration file not found: {}", propertiesFile)
      loadDefaultProperties()
    }
    try {
      val input = new FileInputStream(externalFile)
      try {
        HOIIVConfigManager.LOGGER.debug("External configuration loaded from: {}", propertiesFile)
        config.load(input)
      } catch {
        case e: IOException =>
          HOIIVConfigManager.LOGGER.fatal("Error loading external properties from: {}", propertiesFile, e)
          loadDefaultProperties()
      } finally if (input != null) input.close()
    }
  }

  private def loadDefaultProperties(): Unit = {
    val externalFile = new File(propertiesFile)
    try {
      val externalOut = new FileOutputStream(externalFile)
      try {
        val buffer = new Array[Byte](8192)
        var bytesRead = 0
        while ({bytesRead = defaultProperties.read(buffer); bytesRead != -1}) {
          externalOut.write(buffer, 0, bytesRead)
        }
        HOIIVConfigManager.LOGGER.debug("Default properties copied to: {}", externalFile.getAbsolutePath)
      } catch {
        case e: IOException =>
          HOIIVConfigManager.LOGGER.error("Failed to copy default properties to external file", e)
      } finally {
        externalOut.close()
      }
    } catch {
      case e: IOException =>
        HOIIVConfigManager.LOGGER.error("Failed to create output file: {}", externalFile.getAbsolutePath, e)
    }
  }
}