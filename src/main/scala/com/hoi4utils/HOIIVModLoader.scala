package com.hoi4utils

import com.hoi4utils.clausewitz.HOIIVFiles
import com.hoi4utils.gfx.Interface
import com.hoi4utils.hoi4.country.{Country, CountryTag}
import com.hoi4utils.hoi4.focus.FocusTree
import com.hoi4utils.hoi4.idea.IdeaFile
import com.hoi4utils.localization.LocalizationManager
import map.{ResourcesFile, State}
import org.apache.logging.log4j.LogManager

import java.io.File

object HOIIVModLoader {
  private val LOGGER = LogManager.getLogger(classOf[HOIIVModLoader])
}

class HOIIVModLoader(private var config: HOIIVUtilsConfig) {
  private[hoi4utils] def loadMod(): Unit = {
    if (createHOIIVFilePaths) config.setProperty("valid.HOIIVFilePaths", "true")
    else {
      HOIIVModLoader.LOGGER.error("Failed to create HOIIV file paths")
      config.setProperty("valid.HOIIVFilePaths", "false")
    }
    Interface.clear()
    State.clear()
    FocusTree.clear()
    try LocalizationManager.get.reload()
    catch {
      case e: Exception =>
        HOIIVModLoader.LOGGER.error("Failed to reload localization", e)
    }
    try if (Interface.read) config.setProperty("valid.Interface", "true")
    else {
      config.setProperty("valid.Interface", "false")
      HOIIVModLoader.LOGGER.error("Failed to read gfx interface files")
    }
    catch {
      case e: Exception =>
        config.setProperty("valid.Interface", "false")
        HOIIVModLoader.LOGGER.error("Exception while reading interface files", e)
    }
    try if (ResourcesFile.read) config.setProperty("valid.Resources", "true")
    else {
      config.setProperty("valid.Resources", "false")
      HOIIVModLoader.LOGGER.error("Failed to read resources")
    }
    catch {
      case e: Exception =>
        config.setProperty("valid.Resources", "false")
        HOIIVModLoader.LOGGER.error("Exception while reading resources", e)
    }
    try if (CountryTag.read) config.setProperty("valid.CountryTag", "true")
    else {
      config.setProperty("valid.CountryTag", "false")
      HOIIVModLoader.LOGGER.error("Failed to read country tags")
    }
    catch {
      case e: Exception =>
        config.setProperty("valid.CountryTag", "false")
        HOIIVModLoader.LOGGER.error("Exception while reading country tags", e)
    }
    try if (Country.read) config.setProperty("valid.Country", "true")
    else {
      config.setProperty("valid.Country", "false")
      HOIIVModLoader.LOGGER.error("Failed to read countries")
    }
    catch {
      case e: Exception =>
        config.setProperty("valid.Country", "false")
        HOIIVModLoader.LOGGER.error("Exception while reading countries", e)
    }
    try if (State.read) config.setProperty("valid.State", "true")
    else {
      config.setProperty("valid.State", "false")
      HOIIVModLoader.LOGGER.error("Failed to read states")
    }
    catch {
      case e: Exception =>
        config.setProperty("valid.State", "false")
        HOIIVModLoader.LOGGER.error("Exception while reading states", e)
    }
    try if (FocusTree.read) config.setProperty("valid.FocusTree", "true")
    else {
      config.setProperty("valid.FocusTree", "false")
      HOIIVModLoader.LOGGER.error("Failed to read focus trees")
    }
    catch {
      case e: Exception =>
        config.setProperty("valid.FocusTree", "false")
        HOIIVModLoader.LOGGER.error("Exception while reading focus trees", e)
    }
    try if (IdeaFile.read) config.setProperty("valid.IdeaFiles", "true")
    else {
      config.setProperty("valid.IdeaFiles", "false")
      HOIIVModLoader.LOGGER.error("Failed to read idea files")
    }
    catch {
      case e: Exception =>
        config.setProperty("valid.IdeaFiles", "false")
        HOIIVModLoader.LOGGER.error("Exception while reading idea files", e)
    }
  }

  private def createHOIIVFilePaths: Boolean = {
    if (!(createHOIIVPaths)) return false
    if (!(createModPaths)) return false
    HOIIVUtilsInitializer.changeNotifier.checkAndNotifyChanges()
    return true
  }

  private def createModPaths: Boolean = {
    val modPath = config.getProperty("mod.path")
    if (!(validateDirectoryPath(modPath, "mod.path"))) return false
    HOIIVFiles.setModPathChildDirs(modPath)
    return true
  }

  private def createHOIIVPaths: Boolean = {
    val hoi4Path = config.getProperty("hoi4.path")
    if (!(validateDirectoryPath(hoi4Path, "hoi4.path"))) return false
    HOIIVFiles.setHoi4PathChildDirs(hoi4Path)
    return true
  }

  /** Validates whether the provided directory path is valid */
  private def validateDirectoryPath(path: String, keyName: String): Boolean = {
    if (path == null || path.isEmpty) {
      HOIIVModLoader.LOGGER.error("{} is null or empty!", keyName)
      // Log but don't show popup - we'll show a consolidated warning later
      return false
    }
    val directory = new File(path)
    if (!(directory.exists) || !(directory.isDirectory)) {
      HOIIVModLoader.LOGGER.error("{} does not point to a valid directory: {}", keyName, path)
      // Log but don't show popup - we'll show a consolidated warning later
      return false
    }
    return true
  }
}