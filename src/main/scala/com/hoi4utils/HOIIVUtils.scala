package com.hoi4utils

import com.hoi4utils.ui.MenuController
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
object HOIIVUtils extends LazyLogging {
  val config: Config = new ConfigManager().createConfig

  def main(args: Array[String]): Unit = {
    Application.launch(classOf[MenuController], args*) //    menuController.launchMenuWindow(args)
  }

  /**
   * @param key Property name
   * @return Property value or null if not found
   */
  def get(key: String): String = {
    try
      config.getProperties.getProperty(key)
    catch
      case e: Exception =>
        logger.error(s"v${config.getProperties.getProperty("version")} Failed to get property: $key, ; ${e.getMessage}")
        throw new RuntimeException(e)
  }

  /**
   * @param key   Property key
   * @param value Property value
   */
  def set(key: String, value: String): Unit = {
    try
      config.getProperties.setProperty(key, value)
    catch
      case e: Exception =>
        logger.error(s"v${config.getProperties.getProperty("version")} Failed to set property: $key, ; ${e.getMessage}")
        throw new RuntimeException(e)
  }

  def save(): Unit = {
    try
     new ConfigManager().saveProperties(config)
    catch
      case e: Exception =>
        logger.error(s"v${config.getProperties.getProperty("version")} Failed to save configuration: ${e.getMessage}")
        JOptionPane.showMessageDialog(null, s"version: ${config.getProperties.getProperty("version")} Failed to save configuration: " + e.getMessage, "Critical Error", JOptionPane.ERROR_MESSAGE)
        System.exit(1)
  }
}