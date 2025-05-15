package com.hoi4utils

import com.hoi4utils.ui.menu.MenuController
import org.apache.logging.log4j.{LogManager, Logger}

import java.io.File
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
  val LOGGER: Logger = LogManager.getLogger(classOf[HOIIVUtils])
  // Static references to application configuration
  var HOIIVUTILS_DIR: File = null
  var HOIIVUTILS_VERSION: Version = null
  var menuController: MenuController = null
  private var config: HOIIVUtilsConfig = null

  def main(args: Array[String]): Unit = {
    val upr = new Updater
    try {
      // Initialize the application using the new initializer
      val initializer = new HOIIVUtilsInitializer
      config = initializer.initialize
      HOIIVUTILS_DIR = config.getHoi4UtilsDir
      HOIIVUTILS_VERSION = config.getVersion
      upr.updateCheck(HOIIVUTILS_VERSION, HOIIVUTILS_DIR)
      initializer.loadMod()
      LOGGER.info("HOIIVUtils {} launched successfully", HOIIVUTILS_VERSION)
      menuController = new MenuController
      menuController.launchMenuWindow(args)
    } catch {
      case e: Exception =>
        LOGGER.fatal("Failed to initialize HOIIVUtils", e)
        JOptionPane.showMessageDialog(null, "Failed to initialize HOIIVUtils: " + e.getMessage, "Critical Error", JOptionPane.ERROR_MESSAGE)
        System.exit(1)
    }
  }

  /**
   * Gets a property from the configuration.
   *
   * @param key Property key
   * @return Property value or null if not found
   */
  def get(key: String): String = {
    if (config == null) {
      LOGGER.debug("Configuration not initialized!")
      return ""
    }
    val property = config.getProperty(key)
    if (property != null) return property
    else return ""
  }

  /**
   * Sets a property in the configuration.
   *
   * @param key   Property key
   * @param value Property value
   */
  def set(key: String, value: String): Unit = {
    config.setProperty(key, value)
  }

  def loadMod(): Unit = {
    // Delegate to HOIIVModLoader to reload mod data
    new HOIIVModLoader(config).loadMod()
  }

  def save(): Unit = {
    // Delegate to HOIIVConfigManager to save configuration
    new HOIIVConfigManager(config).saveConfiguration()
  }
}

class HOIIVUtils {}