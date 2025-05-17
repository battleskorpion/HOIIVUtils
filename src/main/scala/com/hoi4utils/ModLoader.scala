package com.hoi4utils

import com.hoi4utils.clausewitz.HOIIVFiles
import com.hoi4utils.fileIO.FileListener.{FileAdapter, FileEvent, FileWatcher}
import com.hoi4utils.gfx.Interface
import com.hoi4utils.hoi4.country.{Country, CountryTag}
import com.hoi4utils.hoi4.focus.FocusTree
import com.hoi4utils.hoi4.idea.IdeaFile
import com.hoi4utils.localization.{EnglishLocalizationManager, LocalizationManager}
import map.{ResourcesFile, State}

import com.typesafe.scalalogging.LazyLogging

import java.awt.EventQueue
import java.beans.PropertyChangeListener
import java.io.File
import java.util.Properties

class ModLoader extends LazyLogging {
  val changeNotifier = new PublicFieldChangeNotifier(this.getClass)

  // TODO: @Skorp I don't know the stateFileWatcher stuff, but since it's in scala now you can probably make it better easier

  private var stateFilesWatcher: FileWatcher = null
  
  def loadMod(hProperties: Properties): Unit = {
    val hoi4Path = hProperties.getProperty("hoi4.path")
    val modPath = hProperties.getProperty("mod.path")
    if (validateDirectoryPath(hoi4Path, "hoi4.path") && validateDirectoryPath(modPath, "mod.path"))
      HOIIVFiles.setHoi4PathChildDirs(hoi4Path)
      HOIIVFiles.setModPathChildDirs(modPath)
      hProperties.setProperty("valid.HOIIVFilePaths", "true")
    else
      logger.error("Failed to create HOIIV file paths")
      hProperties.setProperty("valid.HOIIVFilePaths", "false")

    changeNotifier.checkAndNotifyChanges()

    LocalizationManager.getOrCreate(() => new EnglishLocalizationManager).reload()

    try if (Interface.read()) hProperties.setProperty("valid.Interface", "true")
    else {
      hProperties.setProperty("valid.Interface", "false")
      logger.error("Failed to read gfx interface files")
    }
    catch {
      case e: Exception =>
        hProperties.setProperty("valid.Interface", "false")
        logger.error("Exception while reading interface files", e)
    }
    try if (ResourcesFile.read()) hProperties.setProperty("valid.Resources", "true")
    else {
      hProperties.setProperty("valid.Resources", "false")
      logger.error("Failed to read resources")
    }
    catch {
      case e: Exception =>
        hProperties.setProperty("valid.Resources", "false")
        logger.error("Exception while reading resources", e)
    }
    try if (CountryTag.read()) hProperties.setProperty("valid.CountryTag", "true")
    else {
      hProperties.setProperty("valid.CountryTag", "false")
      logger.error("Failed to read country tags")
    }
    catch {
      case e: Exception =>
        hProperties.setProperty("valid.CountryTag", "false")
        logger.error("Exception while reading country tags", e)
    }
    try if (Country.read()) hProperties.setProperty("valid.Country", "true")
    else {
      hProperties.setProperty("valid.Country", "false")
      logger.error("Failed to read countries")
    }
    catch {
      case e: Exception =>
        hProperties.setProperty("valid.Country", "false")
        logger.error("Exception while reading countries", e)
    }
    try if (State.read()) hProperties.setProperty("valid.State", "true")
    else {
      hProperties.setProperty("valid.State", "false")
      logger.error("Failed to read states")
    }
    catch {
      case e: Exception =>
        hProperties.setProperty("valid.State", "false")
        logger.error("Exception while reading states", e)
    }
    try if (FocusTree.read()) hProperties.setProperty("valid.FocusTree", "true")
    else {
      hProperties.setProperty("valid.FocusTree", "false")
      logger.error("Failed to read focus trees")
    }
    catch {
      case e: Exception =>
        hProperties.setProperty("valid.FocusTree", "false")
        logger.error("Exception while reading focus trees", e)
    }
    try if (IdeaFile.read()) hProperties.setProperty("valid.IdeaFiles", "true")
    else {
      hProperties.setProperty("valid.IdeaFiles", "false")
      logger.error("Failed to read idea files")
    }
    catch {
      case e: Exception =>
        hProperties.setProperty("valid.IdeaFiles", "false")
        logger.error("Exception while reading idea files", e)
    }
  }

  /** Validates whether the provided directory path is valid */
  private def validateDirectoryPath(path: String, keyName: String): Boolean = {
    if (path == null || path.isEmpty) {
      logger.error("{} is null or empty!", keyName)
      return false
    }
    val directory = new File(path)
    if (!(directory.exists) || !(directory.isDirectory)) {
      logger.error("{} does not point to a valid directory: {}", keyName, path)
      return false
    }
    true
  }

  def deleteMod(): Unit = {
    Interface.clear()
    State.clear()
    FocusTree.clear()
  }

  /**
   * Watches the state files in the given directory.
   * TODO: @Skorp
   * @param stateFiles The directory containing state files.
   */
  def watchStateFiles(stateFiles: File): Unit = {
    if (stateFiles == null || !validateDirectoryPath(stateFiles.getPath, "State files directory")) return

    stateFilesWatcher = new FileWatcher(stateFiles)
    stateFilesWatcher.addListener(new FileAdapter {
      override def onCreated(event: FileEvent): Unit = {
        handleStateFileEvent(event, "created/loaded", file => State.readState(file))
      }

      override def onModified(event: FileEvent): Unit = {
        handleStateFileEvent(event, "modified", file => State.readState(file))
      }

      override def onDeleted(event: FileEvent): Unit = {
        handleStateFileEvent(event, "deleted", file => State.removeState(file))
      }
    })
    stateFilesWatcher.watch()
  }

  /**
   * Handles state file events.
   * TODO: @Skorp
   * @param event       File event that occurred.
   * @param actionName  Name of the action performed.
   * @param stateAction Function to apply to the file.
   */
  private def handleStateFileEvent(event: FileEvent, actionName: String, stateAction: File => Unit): Unit = {
    EventQueue.invokeLater(() => {
      stateFilesWatcher.listenerPerformAction = stateFilesWatcher.listenerPerformAction + 1
      val file = event.getFile
      if (file != null) {
        stateAction(file)
      }
      stateFilesWatcher.listenerPerformAction = stateFilesWatcher.listenerPerformAction - 1
      logger.debug(s"State was $actionName: ${State.get(file)}")
    })
  }

  def addPropertyChangeListener(listener: PropertyChangeListener): Unit = {
    changeNotifier.addPropertyChangeListener(listener)
  }

  def removePropertyChangeListener(listener: PropertyChangeListener): Unit = {
    changeNotifier.removePropertyChangeListener(listener)
  }
}