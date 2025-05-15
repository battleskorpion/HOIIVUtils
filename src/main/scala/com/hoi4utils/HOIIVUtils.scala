package com.hoi4utils

import com.hoi4utils.ui.menu.MenuController
import org.apache.logging.log4j.{LogManager, Logger}

import java.io.File
import java.util.Properties
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
object HOIIVUtils {
  val LOGGER: Logger = LogManager.getLogger(this.getClass)
  val configManager = new ConfigManager
  val config: Config = configManager.createConfig
  val hInitializer: Initializer = new Initializer
  val hModLoader: ModLoader = new ModLoader
  val hProperties: Properties = config.getProperties

  def main(args: Array[String]): Unit = {
    val upr = new Updater
//    upr.updateCheck(, config.getDir)
    hModLoader.loadMod(hProperties)
//    LOGGER.info("HOIIVUtils {} launched successfully", config.getVersion)
    val menuController = new MenuController
    menuController.launchMenuWindow(args)
  }

  /**
   * @param key Property name
   * @return Property value or null if not found
   */
  def get(key: String): String = {
    try
      hProperties.getProperty(key)
    catch
      case e: Exception =>
        LOGGER.error("Failed to get property {}: {}", key, e.getMessage)
        throw new RuntimeException(e)
  }

  /**
   * @param key   Property key
   * @param value Property value
   */
  def set(key: String, value: String): Unit = {
    try
      hProperties.setProperty(key, value)
    catch
      case e: Exception =>
        LOGGER.error("Failed to set property {}: {}", key, e.getMessage)
        throw new RuntimeException(e)
  }

  def loadMod(): Unit = {
    try
      new ModLoader().loadMod(hProperties)
    catch
      case e: Exception =>
        LOGGER.error("Failed to load mod: {}", e.getMessage)
        JOptionPane.showMessageDialog(null, "Failed to load mod: " + e.getMessage, "Critical Error", JOptionPane.ERROR_MESSAGE)
        System.exit(1)
  }

  def save(): Unit = {
    try
     new ConfigManager().saveConfiguration(config)
    catch
      case e: Exception =>
        LOGGER.error("Failed to save configuration: {}", e.getMessage)
        JOptionPane.showMessageDialog(null, "Failed to save configuration: " + e.getMessage, "Critical Error", JOptionPane.ERROR_MESSAGE)
        System.exit(1)
  }
}