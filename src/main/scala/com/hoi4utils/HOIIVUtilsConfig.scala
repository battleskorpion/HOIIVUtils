package com.hoi4utils

import java.io.*
import java.util.Properties

/**
 * Configuration class for HOIIVUtils application.
 * Holds all initialized resources and settings.
 */
class HOIIVUtilsConfig(private val hoi4UtilsDir: File, private val propertiesFile: String, private val defaultProperties: InputStream, private val version: Version, private val properties: Properties) {
  def getHoi4UtilsDir: File = return hoi4UtilsDir

  def getPropertiesFile: String = return propertiesFile

  def getDefaultProperties: InputStream = return defaultProperties

  def getVersion: Version = return version

  def getProperty(key: String): String = return properties.getProperty(key)

  def setProperty(key: String, value: String): Unit = {
    properties.setProperty(key, value)
  }

  @throws[IOException]
  private[hoi4utils] def load(input: FileInputStream): Unit = {
    properties.load(input)
  }

  @throws[IOException]
  private[hoi4utils] def store(output: OutputStream, hoiivUtilsConfiguration: String): Unit = {
    properties.store(output, hoiivUtilsConfiguration)
  }
}