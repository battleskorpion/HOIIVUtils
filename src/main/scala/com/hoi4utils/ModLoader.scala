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
    implicit val properties: Properties = hProperties
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
    
    List (
      Interface,
      State,
      Country,
      CountryTag,
      FocusTree,
      IdeaFile,
      ResourcesFile
    ).foreach(readPDXData)
  }
  
  def readPDXData(clazz: PDXReading)(implicit properties: Properties): Unit = {
    val property = s"valid.${clazz.validPropertyName}"
    try
      if (clazz.read()) properties.setProperty(property, "true")
      else
        properties.setProperty(property, "false")
        logger.error(s"Exception while reading for ${clazz.validPropertyName}")
    catch
      case e: Exception =>
        properties.setProperty(property, "false")
        logger.error(s"Exception while reading for ${clazz.validPropertyName}", e)
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