package com.hoi4utils.main

import com.hoi4utils.databases.effect.EffectDatabase
import com.hoi4utils.databases.effect.EffectDatabase.effectErrors
import com.hoi4utils.databases.modifier.ModifierDatabase
import com.hoi4utils.file.file_listener.{FileAdapter, FileEvent, FileWatcher}
import com.hoi4utils.hoi4mod.common.country_tags.CountryTag
import com.hoi4utils.hoi4mod.common.idea.IdeaFile
import com.hoi4utils.hoi4mod.common.idea.IdeaFile.ideaFileErrors
import com.hoi4utils.hoi4mod.common.national_focus.FocusTreeFile
import com.hoi4utils.hoi4mod.common.national_focus.FocusTreeFile.focusTreeFileErrors
import com.hoi4utils.hoi4mod.gfx.Interface
import com.hoi4utils.hoi4mod.gfx.Interface.interfaceErrors
import com.hoi4utils.hoi4mod.history.countries.CountryFile
import com.hoi4utils.hoi4mod.history.countries.CountryFile.countryErrors
import com.hoi4utils.hoi4mod.localization.LocalizationManager.localizationErrors
import com.hoi4utils.hoi4mod.localization.{EnglishLocalizationManager, LocalizationManager}
import com.hoi4utils.hoi4mod.map.resource.Resource.resourceErrors
import com.hoi4utils.hoi4mod.map.resource.ResourcesFile
import com.hoi4utils.hoi4mod.map.state.State
import com.hoi4utils.hoi4mod.map.state.State.stateErrors
import com.hoi4utils.main.HOIIVFiles
import com.hoi4utils.script.PDXReadable
import com.hoi4utils.ui.menus.MenuController
import com.typesafe.scalalogging.LazyLogging
import javafx.scene.control.Label

import java.awt.EventQueue
import java.beans.PropertyChangeListener
import java.io.File
import java.util.Properties
import scala.collection.mutable.ListBuffer

/**
 * Loads in the mod and hoi4 together
 *
 * TODO: @Skorp Update the ChangeNotifier and FileWatcher or delete this todo if working as intended
 */
class PDXLoader extends LazyLogging:
  val changeNotifier = new PublicFieldChangeNotifier(this.getClass)
  private var stateFilesWatcher: FileWatcher = null

  def load(hProperties: Properties, loadingLabel: Label): Unit =
    implicit val properties: Properties = hProperties
    implicit val label: Label = loadingLabel

    MenuController.updateLoadingStatus(loadingLabel, "Initializing ModifierDatabase...")
    ModifierDatabase.init()
    MenuController.updateLoadingStatus(loadingLabel, "Initializing EffectDatabase...")
    EffectDatabase.init()

    MenuController.updateLoadingStatus(loadingLabel, "Finding Paths...")
    val hoi4Path = hProperties.getProperty("hoi4.path")
    val modPath = hProperties.getProperty("mod.path")
    if validateDirectoryPath(hoi4Path, "hoi4.path") && validateDirectoryPath(modPath, "mod.path") then
      HOIIVFiles.setHoi4PathChildDirs(hoi4Path)
      HOIIVFiles.setModPathChildDirs(modPath)
      hProperties.setProperty("valid.HOIIVFilePaths", "true")
    else
      logger.error("Failed to create HOIIV file paths")
      hProperties.setProperty("valid.HOIIVFilePaths", "false")

    changeNotifier.checkAndNotifyChanges()

    MenuController.updateLoadingStatus(loadingLabel, "Loading Localization...")
    LocalizationManager.getOrCreate(() => new EnglishLocalizationManager).reload()

    /* LOAD ORDER IMPORTANT (depending on the class) */
    List (
      Interface,
      ResourcesFile,
      State,
      CountryFile,
      CountryTag,
      IdeaFile,
      FocusTreeFile,
    ).foreach(readPDX)

  def readPDX(pdx: PDXReadable)(implicit properties: Properties, label: Label): Unit =
    val property = s"valid.${pdx.name}"

    MenuController.updateLoadingStatus(label, s"Loading ${pdx.name} files...")
    try
      if pdx.read() then properties.setProperty(property, "true")
      else
        properties.setProperty(property, "false")
        logger.error(s"Exception while reading for ${pdx.name}")
    catch
      case e: Exception =>
        properties.setProperty(property, "false")
        logger.error(s"Exception while reading for ${pdx.name}", e)

  /** Validates whether the provided directory path is valid */
  private def validateDirectoryPath(path: String, keyName: String): Boolean =
    if path == null || path.isEmpty then
      logger.error("{} is null or empty!", keyName)
      return false
    val directory = new File(path)
    if !directory.exists || !directory.isDirectory then
      logger.error("{} does not point to a valid directory: {}", keyName, path)
      return false
    true

  def clearPDX(): Unit =
    IdeaFile.clear()
    FocusTreeFile.clear()
    CountryTag.clear()
    CountryFile.clear()
    State.clear()
    ResourcesFile.clear()
    Interface.clear()

  def clearLB(): Unit =
    ListBuffer(
      effectErrors,
      localizationErrors,
      interfaceErrors,
      countryErrors,
      focusTreeFileErrors,
      ideaFileErrors,
      resourceErrors,
      stateErrors
    ).foreach(_.clear())

  def closeDB(): Unit =
    ModifierDatabase.close()
    EffectDatabase.close()

  /**
   * Watches the state files in the given directory.
   *
   * @param stateFiles The directory containing state files.
   */
  def watchStateFiles(stateFiles: File): Unit =
    if stateFiles == null || !validateDirectoryPath(stateFiles.getPath, "State files directory") then return

    stateFilesWatcher = new FileWatcher(stateFiles)
    stateFilesWatcher.addListener(new FileAdapter:
      override def onCreated(event: FileEvent): Unit =
        handleStateFileEvent(event, "created/loaded", file => State.readState(file))

      override def onModified(event: FileEvent): Unit =
        handleStateFileEvent(event, "modified", file => State.readState(file))

      override def onDeleted(event: FileEvent): Unit =
        handleStateFileEvent(event, "deleted", file => State.removeState(file))
    )
    stateFilesWatcher.watch()

  /**
   * Handles state file events.
   *
   * @param event       File event that occurred.
   * @param actionName  Name of the action performed.
   * @param stateAction Function to apply to the file.
   */
  private def handleStateFileEvent(event: FileEvent, actionName: String, stateAction: File => Unit): Unit =
    EventQueue.invokeLater(() =>
      stateFilesWatcher.listenerPerformAction = stateFilesWatcher.listenerPerformAction + 1
      val file = event.getFile
      if file != null then
        stateAction(file)
      stateFilesWatcher.listenerPerformAction = stateFilesWatcher.listenerPerformAction - 1
      logger.debug(s"State was $actionName: ${State.get(file)}")
    )

  def addPropertyChangeListener(listener: PropertyChangeListener): Unit =
    changeNotifier.addPropertyChangeListener(listener)

  def removePropertyChangeListener(listener: PropertyChangeListener): Unit =
    changeNotifier.removePropertyChangeListener(listener)