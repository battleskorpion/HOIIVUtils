package com.hoi4utils

import com.hoi4utils.clausewitz.HOIIVFiles
import com.hoi4utils.gfx.Interface
import com.hoi4utils.hoi4.country.{Country, CountryTag}
import com.hoi4utils.hoi4.focus.FocusTree
import com.hoi4utils.hoi4.idea.IdeaFile
import com.hoi4utils.localization.{EnglishLocalizationManager, LocalizationManager}
import map.{ResourcesFile, State}
import org.apache.logging.log4j.LogManager

import java.io.File
import java.util.Properties

class HOIIVModLoader {
  private val LOGGER = LogManager.getLogger(this.getClass)
  
  def loadMod(hProperties: Properties): Unit = {
    val hoi4Path = hProperties.getProperty("hoi4.path")
    val modPath = hProperties.getProperty("mod.path")
    if (validateDirectoryPath(hoi4Path, "hoi4.path") && validateDirectoryPath(modPath, "mod.path"))
      HOIIVFiles.setHoi4PathChildDirs(hoi4Path)
      HOIIVFiles.setModPathChildDirs(modPath)
      hProperties.setProperty("valid.HOIIVFilePaths", "true")
    else
      LOGGER.error("Failed to create HOIIV file paths")
      hProperties.setProperty("valid.HOIIVFilePaths", "false")

//  HOIIVUtilsInitializer.changeNotifier.checkAndNotifyChanges()

    LocalizationManager.getOrCreate(() => new EnglishLocalizationManager).reload()

    try if (Interface.read()) hProperties.setProperty("valid.Interface", "true")
    else {
      hProperties.setProperty("valid.Interface", "false")
      LOGGER.error("Failed to read gfx interface files")
    }
    catch {
      case e: Exception =>
        hProperties.setProperty("valid.Interface", "false")
        LOGGER.error("Exception while reading interface files", e)
    }
    try if (ResourcesFile.read()) hProperties.setProperty("valid.Resources", "true")
    else {
      hProperties.setProperty("valid.Resources", "false")
      LOGGER.error("Failed to read resources")
    }
    catch {
      case e: Exception =>
        hProperties.setProperty("valid.Resources", "false")
        LOGGER.error("Exception while reading resources", e)
    }
    try if (CountryTag.read()) hProperties.setProperty("valid.CountryTag", "true")
    else {
      hProperties.setProperty("valid.CountryTag", "false")
      LOGGER.error("Failed to read country tags")
    }
    catch {
      case e: Exception =>
        hProperties.setProperty("valid.CountryTag", "false")
        LOGGER.error("Exception while reading country tags", e)
    }
    try if (Country.read()) hProperties.setProperty("valid.Country", "true")
    else {
      hProperties.setProperty("valid.Country", "false")
      LOGGER.error("Failed to read countries")
    }
    catch {
      case e: Exception =>
        hProperties.setProperty("valid.Country", "false")
        LOGGER.error("Exception while reading countries", e)
    }
    try if (State.read()) hProperties.setProperty("valid.State", "true")
    else {
      hProperties.setProperty("valid.State", "false")
      LOGGER.error("Failed to read states")
    }
    catch {
      case e: Exception =>
        hProperties.setProperty("valid.State", "false")
        LOGGER.error("Exception while reading states", e)
    }
    try if (FocusTree.read()) hProperties.setProperty("valid.FocusTree", "true")
    else {
      hProperties.setProperty("valid.FocusTree", "false")
      LOGGER.error("Failed to read focus trees")
    }
    catch {
      case e: Exception =>
        hProperties.setProperty("valid.FocusTree", "false")
        LOGGER.error("Exception while reading focus trees", e)
    }
    try if (IdeaFile.read()) hProperties.setProperty("valid.IdeaFiles", "true")
    else {
      hProperties.setProperty("valid.IdeaFiles", "false")
      LOGGER.error("Failed to read idea files")
    }
    catch {
      case e: Exception =>
        hProperties.setProperty("valid.IdeaFiles", "false")
        LOGGER.error("Exception while reading idea files", e)
    }
  }

  /** Validates whether the provided directory path is valid */
  private def validateDirectoryPath(path: String, keyName: String): Boolean = {
    if (path == null || path.isEmpty) {
      LOGGER.error("{} is null or empty!", keyName)
      return false
    }
    val directory = new File(path)
    if (!(directory.exists) || !(directory.isDirectory)) {
      LOGGER.error("{} does not point to a valid directory: {}", keyName, path)
      return false
    }
    true
  }

  def deleteMod(): Unit = {
    Interface.clear()
    State.clear()
    FocusTree.clear()
  }
}