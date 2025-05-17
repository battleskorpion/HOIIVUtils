package com.hoi4utils

import com.hoi4utils.ui.MenuController
import com.typesafe.scalalogging.LazyLogging
import javafx.application.Application

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
object HOIIVUtils extends LazyLogging {
  val configManager = new ConfigManager
  val config: Config = configManager.createConfig
  val hInitializer: Initializer = new Initializer
  val hModLoader: ModLoader = new ModLoader
  val hProperties: Properties = config.getProperties

  def main(args: Array[String]): Unit = {
    val upr = new Updater
    hInitializer.initialize(config)
    val version = Version.getVersion(hProperties)
    upr.updateCheck(version, config.getDir)
    hModLoader.loadMod(hProperties)
    logger.info(s"HOIIVUtils $version launched successfully")
    val menuController = new MenuController
    Application.launch(classOf[MenuController], args*) //    menuController.launchMenuWindow(args)
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
        logger.error("Failed to get property {}: {}", key, e.getMessage)
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
        logger.error("Failed to set property {}: {}", key, e.getMessage)
        throw new RuntimeException(e)
  }

  def loadMod(): Unit = {
    try
      new ModLoader().loadMod(hProperties)
    catch
      case e: Exception =>
        logger.error("Failed to load mod: {}", e.getMessage)
        JOptionPane.showMessageDialog(null, "Failed to load mod: " + e.getMessage, "Critical Error", JOptionPane.ERROR_MESSAGE)
        System.exit(1)
  }

  def save(): Unit = {
    try
     new ConfigManager().saveProperties(config)
    catch
      case e: Exception =>
        logger.error("Failed to save configuration: {}", e.getMessage)
        JOptionPane.showMessageDialog(null, "Failed to save configuration: " + e.getMessage, "Critical Error", JOptionPane.ERROR_MESSAGE)
        System.exit(1)
  }
}