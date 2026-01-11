package com.hoi4utils.main

import java.io.*
import java.nio.file.Path
import java.util.Properties

/**
 * Configuration class for HOIIVUtils application.
 * Holds all initialized resources and settings.
 */
class Config(
  private val hDir: Path,
  private val hPropertiesPath: Path,
  private val hPropertiesJarResource: InputStream,
  private val hVersionTempPath: Path,
  private val hVersionJarResource: InputStream,
  private val hProperties: Properties
            ):
  def getDir: Path = hDir
  def getPropertiesPath: Path = hPropertiesPath
  def getPropertiesJarResource: InputStream = hPropertiesJarResource
  def getVersionTempPath: Path = hVersionTempPath
  def getVersionJarResource: InputStream = hVersionJarResource
  def getProperties: Properties = hProperties

  /**
   * Get a user saved property from property class saved properties, NOT from HOIIVUtils.properties.
   * We get saved HOIIVUtils.properties data only when the Menu is opened
   *
   * @param key Property name
   * @return Property value or null if not found
   */
  def getProperty(key: String): String = getProperties.getProperty(key)

  /**
   * Set a user saved property that will be saved to HOIIVUtils.properties on save() call
   *
   * @param key   Property key
   * @param value Property value
   */
  def setProperty(key: String, value: String) = getProperties.setProperty(key, value)