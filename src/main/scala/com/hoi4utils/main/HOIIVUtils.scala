package com.hoi4utils.main

import com.typesafe.scalalogging.LazyLogging
import javafx.application.Application

import javax.swing.*

/**
 * HOIIVUtils.java main method is here
 * <p>
 * HOIIVUTILS Directory Layout:
 * <p>
 * HOIIVUtils\\target\\HOIIVUtils.jar
 * <p>
 * HOIIVUtils\\demo_mod\\*
 * <p>
 * HOIIVUtils\\HOIIVUtils.bat
 * <p>
 * HOIIVUtils\\HOIIVUtils.properties
 * <p>
 * HOIIVUtils\\HOIIVUtils.sh
 */
object HOIIVUtils extends LazyLogging:
  var config: Option[Config] = None
  def main(args: Array[String]): Unit = Application.launch(classOf[App], args*)

  def getConfig: Config =
    config match
      case Some(c) => c
      case None => throw new RuntimeException("Configuration not initialized")
      
  def setConfig(newConfig: Config): Unit = config = Some(newConfig)
  
  /**
   * Get a user saved property from property class saved properties, NOT from HOIIVUtils.properties.
   * We get saved HOIIVUtils.properties data only when the Menu is opened
   * @param key Property name
   * @return Property value or null if not found
   */
  def get(key: String): String = getConfig.getProperties.getProperty(key)

  /**
   * Set a user saved property that will be saved to HOIIVUtils.properties on save() call
   * @param key   Property key
   * @param value Property value
   */
  def set(key: String, value: String): Unit = getConfig.getProperties.setProperty(key, value)

  /**
   * Save the current configuration to HOIIVUtils.properties
   */
  def save(): Unit =
    try ConfigManager().saveProperties(getConfig)
    catch case e: Exception =>
        logger.error(s"v${getConfig.getProperties.getProperty("version")} Failed to save configuration: ${e.getMessage}")
        JOptionPane.showMessageDialog(null, s"version: ${getConfig.getProperties.getProperty("version")} Failed to save configuration: " + e.getMessage, "Critical Error", JOptionPane.ERROR_MESSAGE)
        throw new Exception(e)