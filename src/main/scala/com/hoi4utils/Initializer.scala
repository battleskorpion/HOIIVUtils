package com.hoi4utils

import com.hoi4utils.clausewitz.HOIIVFiles
import com.hoi4utils.fileIO.FileListener.{FileAdapter, FileEvent, FileWatcher}
import com.hoi4utils.gfx.Interface
import com.hoi4utils.hoi4.country.{Country, CountryTag}
import com.hoi4utils.hoi4.effect.EffectDatabase
import com.hoi4utils.hoi4.focus.FocusTree
import com.hoi4utils.hoi4.idea.IdeaFile
import com.hoi4utils.hoi4.modifier.ModifierDatabase
import com.hoi4utils.Config
import com.hoi4utils.localization.{EnglishLocalizationManager, LocalizationManager}
import map.{ResourcesFile, State}
import org.apache.logging.log4j.LogManager

import java.awt.*
import java.beans.PropertyChangeListener
import java.io.*
import java.net.URISyntaxException
import java.nio.file.{Path, Paths}
import java.util
import java.util.{Optional, Properties}
import javax.swing.*
import scala.::

/**
 * Handles initialization of the HOIIVUtils application.
 * Separates initialization logic from utility functions.
 */
class Initializer {
  private val LOGGER = LogManager.getLogger(this.getClass)

  def initialize(config: Config): Unit = {
    ModifierDatabase.init()
    EffectDatabase.init()
    ConfigManager().loadProperties(config)
    autoSetHOIIVPath(config.getProperties)
    autoSetDemoModPath(config.getProperties, config.getDir)
    ConfigManager().saveProperties(config)
  }

  private def autoSetHOIIVPath(p: Properties): Unit = {
    val hoi4Path = Option(p.getProperty("hoi4.path")).getOrElse("")
    if (hoi4Path.nonEmpty && hoi4Path.trim.nonEmpty) {
      LOGGER.debug("HOI4 path already set. Skipping auto-set.")
      return
    }

    getPossibleHOIIVPaths.find { path =>
      val hoi4Dir = Paths.get(path).toAbsolutePath.toFile
      hoi4Dir.exists
    } match {
      case Some(validPath) =>
        val hoi4Dir = Paths.get(validPath).toAbsolutePath.toFile
        p.setProperty("hoi4.path", hoi4Dir.getAbsolutePath)
        LOGGER.debug("Auto-set HOI4 path: {}", hoi4Dir.getAbsolutePath)
      case None =>
        LOGGER.warn("Couldn't find HOI4 install folder. User must set it manually.")
        JOptionPane.showMessageDialog(
          null,
          "Couldn't find HOI4 install folder, please go to settings and add it (REQUIRED)",
          "Error Message",
          JOptionPane.WARNING_MESSAGE
        )
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
      LOGGER.debug("Auto-set mod path to demo_mod")
      return
    }

    val isDemoMod = try {
      Paths.get(modPath).getFileName.toString == "demo_mod"
    } catch {
      case e: Exception =>
        LOGGER.warn("Error checking mod path: {}", e.getMessage)
        false
    }

    if (isDemoMod) {
      p.setProperty("mod.path", demoModPath)
      LOGGER.debug("Reset mod path to demo_mod")
    } else {
      LOGGER.debug("Mod path already set. Skipping auto-set.")
    }
  }
}