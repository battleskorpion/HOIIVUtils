package com.hoi4utils

import com.hoi4utils.ui.menu.MenuController
import org.apache.logging.log4j.{LogManager, Logger}
import java.util.Properties
import javax.swing.JOptionPane

// 1) Top-level @main with varargs for Scala 3
@main def hoi4utils(args: String*): Unit =
  val LOGGER: Logger = LogManager.getLogger("Main")
  val configManager     = new ConfigManager
  val config: Config    = configManager.createConfig
  val initializer       = new Initializer
  val modLoader         = new ModLoader
  val properties: Properties = config.getProperties
  val updater           = new Updater

  // initialize & update
  initializer.initialize(config)
  val version = Version.getVersion
  updater.updateCheck(version, config.getDir)

  // load mod
  try
    modLoader.loadMod(properties)
  catch
    case e: Exception =>
      LOGGER.error("Failed to load mod: {}", e.getMessage)
      JOptionPane.showMessageDialog(null, s"Failed to load mod: ${e.getMessage}", "Critical Error", JOptionPane.ERROR_MESSAGE)
      System.exit(1)

  LOGGER.info(s"HOIIVUtils $version launched successfully")

  // launch the Swing menu (Scala Array ⇄ Java String[] is seamless)
  new MenuController().launchMenuWindow(args.toArray)

// 2) Keep this object purely as utility storage (no more .main clash)
object HOIIVUtils {
  private val LOGGER: Logger = LogManager.getLogger(this.getClass)
  private val manager = new ConfigManager
  private val config: Config = manager.createConfig

  def get(key: String): String =
    try config.getProperties.getProperty(key)
    catch
      case e: Exception =>
        LOGGER.error("Failed to get property {}: {}", key, e.getMessage)
        throw RuntimeException(e)

  def set(key: String, value: String): Unit =
    try config.getProperties.setProperty(key, value)
    catch
      case e: Exception =>
        LOGGER.error("Failed to set property {}: {}", key, e.getMessage)
        throw RuntimeException(e)

  def save(): Unit =
    try manager.saveProperties(config)
    catch
      case e: Exception =>
        LOGGER.error("Failed to save configuration: {}", e.getMessage)
        JOptionPane.showMessageDialog(null, s"Failed to save configuration: ${e.getMessage}", "Critical Error", JOptionPane.ERROR_MESSAGE)
        System.exit(1)
}
