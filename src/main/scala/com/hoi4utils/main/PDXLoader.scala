package com.hoi4utils.main

import com.hoi4utils.databases.effect.EffectDatabase
import com.hoi4utils.databases.effect.EffectDatabase.effectErrors
import com.hoi4utils.databases.modifier.ModifierDatabase
import com.hoi4utils.file.file_listener.{FileAdapter, FileEvent, FileWatcher}
import com.hoi4utils.hoi4.common.country_tags.CountryTag
import com.hoi4utils.hoi4.common.idea.IdeasManager.ideaFileErrors
import com.hoi4utils.hoi4.common.idea.{IdeaFile, IdeasManager}
import com.hoi4utils.hoi4.common.national_focus.FocusTreeManager.focusTreeErrors
import com.hoi4utils.hoi4.common.national_focus.{FocusTree, FocusTreeManager}
import com.hoi4utils.hoi4.gfx.Interface
import com.hoi4utils.hoi4.gfx.Interface.interfaceErrors
import com.hoi4utils.hoi4.history.countries.CountryFile
import com.hoi4utils.hoi4.history.countries.CountryFile.countryErrors
import com.hoi4utils.hoi4.localization.LocalizationManager.localizationErrors
import com.hoi4utils.hoi4.localization.{EnglishLocalizationManager, LocalizationManager}
import com.hoi4utils.hoi4.map.resource.Resource.resourceErrors
import com.hoi4utils.hoi4.map.resource.ResourcesFile
import com.hoi4utils.hoi4.map.state.State
import com.hoi4utils.hoi4.map.state.State.stateErrors
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

  /* LOAD ORDER IMPORTANT (depending on the class) */
  val pdxList: List[PDXReadable] = List(
    Interface,
    ResourcesFile,
    State,
    CountryFile,
    CountryTag,
    IdeasManager,
    FocusTreeManager,
  )

  /**
   * Loads all HOI4 and mod data with optional timing callbacks for performance monitoring.
   *
   * Components are loaded in this order:
   * 1. ModifierDatabase - Effect modifier definitions
   * 2. EffectDatabase - Effect system initialization
   * 3. Paths - HOI4 and mod directory validation
   * 4. Localization - Text/translation files
   * 5. Interface - UI definitions
   * 6. Resources - Resource definitions
   * 7. State - Map state files
   * 8. Country - Country history files
   * 9. CountryTag - Country tag definitions
   * 10. Ideas - National ideas/spirits
   * 11. FocusTrees - National focus trees
   *
   * @param hProperties Configuration properties containing paths and settings
   * @param loadingLabel JavaFX label to update with loading status messages
   * @param isCancelled Callback to check if loading should be cancelled
   * @param onComponentComplete Callback invoked when a component finishes loading, receives (componentName, loadTimeSeconds)
   * @param onComponentStart Callback invoked when a component begins loading, receives componentName
   */
  def load(
            hProperties: Properties,
            loadingLabel: Label,
            isCancelled: () => Boolean = () => false,
            onComponentComplete: (String, Double) => Unit = (_, _) => (),
            onComponentStart: String => Unit = _ => ()
          ): Unit =
    implicit val properties: Properties = hProperties
    implicit val label: Label = loadingLabel
    if isCancelled() then return

    var startTime = System.nanoTime()
    onComponentStart("ModifierDatabase")
    MenuController.updateLoadingStatus(loadingLabel, "Initializing ModifierDatabase...")
    ModifierDatabase.init()
    onComponentComplete("ModifierDatabase", (System.nanoTime() - startTime) / 1_000_000_000.0)
    if isCancelled() then return

    startTime = System.nanoTime()
    onComponentStart("EffectDatabase")
    MenuController.updateLoadingStatus(loadingLabel, "Initializing EffectDatabase...")
    EffectDatabase.init()
    onComponentComplete("EffectDatabase", (System.nanoTime() - startTime) / 1_000_000_000.0)
    if isCancelled() then return

    MenuController.updateLoadingStatus(loadingLabel, "Finding Paths...")
    val hoi4Path = hProperties.getProperty("hoi4.path")
    val modPath = hProperties.getProperty("mod.path")
    if isCancelled() then return
    if validateDirectoryPath(hoi4Path, "hoi4.path") && validateDirectoryPath(modPath, "mod.path") then
      HOIIVFiles.setHoi4PathChildDirs(hoi4Path)
      HOIIVFiles.setModPathChildDirs(modPath)
      hProperties.setProperty("valid.HOIIVFilePaths", "true")
    else
      logger.error("Failed to create HOIIV file paths")
      hProperties.setProperty("valid.HOIIVFilePaths", "false")
    if isCancelled() then return
    changeNotifier.checkAndNotifyChanges()

    startTime = System.nanoTime()
    onComponentStart("Localization")
    MenuController.updateLoadingStatus(loadingLabel, "Loading Localization...")
    LocalizationManager.getOrCreate(() => new EnglishLocalizationManager).reload()
    onComponentComplete("Localization", (System.nanoTime() - startTime) / 1_000_000_000.0)
    if isCancelled() then return

    pdxList.foreach(p =>
      if !isCancelled() then
        val componentName = p.name match
          case "ResourcesFile$" => "Resources"
          case "CountryFile" => "Country"
          case "IdeasManager" => "Ideas"
          case "FocusTreeManager" => "FocusTrees"
          case other => other
        onComponentStart(componentName)
        val componentStart = System.nanoTime()
        readPDX(p, isCancelled)
        onComponentComplete(componentName, (System.nanoTime() - componentStart) / 1_000_000_000.0)
    )

  def readPDX(pdx: PDXReadable, isCancelled: () => Boolean = () => false)(implicit properties: Properties, label: Label): Unit =
    val property = s"valid.${pdx.name}"

    MenuController.updateLoadingStatus(label, s"Loading ${pdx.name} files...")
    if isCancelled() then return
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

  /** Clears loaded PDX data. */
  def clearPDX(): Unit = pdxList.foreach(_.clear())

  def clearLB(): Unit =
    ListBuffer(
      effectErrors,
      localizationErrors,
      interfaceErrors,
      countryErrors,
      focusTreeErrors,
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