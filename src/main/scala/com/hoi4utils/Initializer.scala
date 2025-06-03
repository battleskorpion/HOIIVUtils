package com.hoi4utils

import com.hoi4utils.ui.MenuController
import com.typesafe.scalalogging.LazyLogging
import javafx.scene.control.Label

import java.io.*
import java.nio.file.{Path, Paths}
import java.util.Properties
import javax.swing.*

/**
 * Handles initialization of the HOIIVUtils application.
 * Separates initialization logic from utility functions.
 */
class Initializer extends LazyLogging {

  // TODO: add success to some of these, this is so if something fails that we skip loading the mod and tell the user to go to setting and try again with new settings
  def initialize(config: Config, loadingLabel: Label): Boolean = {
    var initFailed: Boolean = false


    MenuController.updateLoadingStatus(loadingLabel, "Loading Properties / Creating Properties...")
    ConfigManager().loadProperties(config)

    initFailed = autoSetHOIIVPath(config.getProperties)

    autoSetDemoModPath(config.getProperties, config.getDir)

    config.getProperties.setProperty("hDir", config.getDir.toString)

    MenuController.updateLoadingStatus(loadingLabel, "Saving Properties...")
    ConfigManager().saveProperties(config)

    initFailed
  }

  private def autoSetHOIIVPath(p: Properties): Boolean = {
    val hoi4Path = Option(p.getProperty("hoi4.path")).getOrElse("")
    if (hoi4Path.nonEmpty && hoi4Path.trim.nonEmpty) {
      logger.debug("HOI4 path already set. Skipping auto-set.")
      return false
    }

    getPossibleHOIIVPaths.find { path =>
      val hoi4Dir = Paths.get(path).toAbsolutePath.toFile
      hoi4Dir.exists
    } match {
      case Some(validPath) =>
        val hoi4Dir = Paths.get(validPath).toAbsolutePath.toFile
        p.setProperty("hoi4.path", hoi4Dir.getAbsolutePath)
        logger.debug("Auto-set HOI4 path: {}", hoi4Dir.getAbsolutePath)
        false
      case None =>
        logger.warn("Couldn't find HOI4 install folder. User must set it manually.")
        JOptionPane.showMessageDialog(
          null,
          s"version: ${Version.getVersion(p)} Couldn't find Hearts Of Iron 4 default steam installation folder, please go to the settings page and add it (REQUIRED)",
          "Error Message",
          JOptionPane.WARNING_MESSAGE
        )
        true
    }
  }

  private def getPossibleHOIIVPaths: Seq[String] = {
    val os = System.getProperty("os.name").toLowerCase
    if (os.contains("win")) {
      Seq(
        "C:\\Program Files (x86)\\Steam\\steamapps\\common\\Hearts of Iron IV",
        System.getenv("ProgramFiles") + "\\Steam\\steamapps\\common\\Hearts of Iron IV"
      )
    } else if (os.contains("nix") || os.contains("nux")) {
      Seq(
        System.getProperty("user.home") + "/.steam/steam/steamapps/common/Hearts of Iron IV",
        System.getProperty("user.home") + "/.local/share/Steam/steamapps/common/Hearts of Iron IV"
      )
    } else if (os.contains("mac")) {
      Seq(
        System.getProperty("user.home") + "/Library/Application Support/Steam/steamapps/common/Hearts of Iron IV"
      )
    } else {
      Seq.empty
    }
  }

  private def autoSetDemoModPath(p: Properties, d: Path): Unit = {
    val modPath = Option(p.getProperty("mod.path")).getOrElse("")
    val demoModPath = s"$d${File.separator}demo_mod"

    if (modPath.isBlank) {
      p.setProperty("mod.path", demoModPath)
      logger.debug("Auto-set mod path to demo_mod")
      return
    }

    val isDemoMod = try {
      Paths.get(modPath).getFileName.toString == "demo_mod"
    } catch {
      case e: Exception =>
        logger.warn("Error checking mod path: {}", e.getMessage)
        false
    }

    if (isDemoMod) {
      p.setProperty("mod.path", demoModPath)
      logger.debug("Reset mod path to demo_mod")
    } else {
      logger.debug("Mod path already set. Skipping auto-set.")
    }
  }
}