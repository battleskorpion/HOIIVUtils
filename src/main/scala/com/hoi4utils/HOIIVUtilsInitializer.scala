package com.hoi4utils

import com.hoi4utils.clausewitz.HOIIVFiles
import com.hoi4utils.fileIO.FileListener.{FileAdapter, FileEvent, FileWatcher}
import com.hoi4utils.gfx.Interface
import com.hoi4utils.hoi4.country.{Country, CountryTag}
import com.hoi4utils.hoi4.effect.EffectDatabase
import com.hoi4utils.hoi4.focus.FocusTree
import com.hoi4utils.hoi4.idea.IdeaFile
import com.hoi4utils.hoi4.modifier.ModifierDatabase
import com.hoi4utils.HOIIVUtilsConfig
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
class HOIIVUtilsInitializer {
  private val LOGGER = LogManager.getLogger(this.getClass)
  val changeNotifier = new PublicFieldChangeNotifier(this.getClass)

  // TODO: @Skorp I don't know the stateFileWatcher stuff, but since it's in scala now you can probably make it better easier
//  @SuppressWarnings(Array("exports")) private var stateFilesWatcher = null

  def initializer(config: HOIIVUtilsConfig): Unit = {
    ModifierDatabase.init()
    EffectDatabase.init()
    HOIIVConfigManager().loadConfiguration(config)
    autoSetHOIIVPath()
    autoSetDemoModPath()
    HOIIVConfigManager().saveConfiguration(config)
  }

  private def autoSetHOIIVPath(): Unit = {
    val hoi4Path = getProperty("hoi4.path")
    val hoi4PathNotSet = hoi4Path == null || hoi4Path.isBlank
    if (!(hoi4PathNotSet)) {
      LOGGER.debug("HOI4 path already set. Skipping auto-set.")
      return
    }
    for (path <- getPossibleHOIIVPaths) {
      val hoi4Dir = Paths.get(path).toAbsolutePath.toFile
      if (hoi4Dir.exists) {
        setProperty("hoi4.path", hoi4Dir.getAbsolutePath)
        LOGGER.debug("Auto-set HOI4 path: {}", hoi4Dir.getAbsolutePath)
        return
      }
    }
    LOGGER.warn("Couldn't find HOI4 install folder. User must set it manually.")
    JOptionPane.showMessageDialog(null, "Couldn't find HOI4 install folder, please go to settings and add it (REQUIRED)", "Error Message", JOptionPane.WARNING_MESSAGE)
  }

  private def getPossibleHOIIVPaths: util.List[String] = {
    val os = System.getProperty("os.name").toLowerCase
    val possibleHOIIVPaths = new util.ArrayList[String]
    if (os.contains("win")) {
      possibleHOIIVPaths.add("C:\\Program Files (x86)\\Steam\\steamapps\\common\\Hearts of Iron IV")
      possibleHOIIVPaths.add(System.getenv("ProgramFiles") + "\\Steam\\steamapps\\common\\Hearts of Iron IV")
    }
    else if (os.contains("nix") || os.contains("nux")) {
      possibleHOIIVPaths.add(System.getProperty("user.home") + "/.steam/steam/steamapps/common/Hearts of Iron IV")
      possibleHOIIVPaths.add(System.getProperty("user.home") + "/.local/share/Steam/steamapps/common/Hearts of Iron IV")
    }
    else if (os.contains("mac")) possibleHOIIVPaths.add(System.getProperty("user.home") + "/Library/Application Support/Steam/steamapps/common/Hearts of Iron IV")
    possibleHOIIVPaths
  }

  private def autoSetDemoModPath(): Unit = {
    val modPath = getProperty("mod.path")
    val modPathNotSet = modPath == null || modPath.isBlank
    if (modPathNotSet) {
      setProperty("mod.path", hDir + File.separator + "demo_mod")
      LOGGER.debug("Auto-set mod path to demo_mod")
      return
    }
    var modPathIsDemo = false
    try modPathIsDemo = Paths.get(modPath).getFileName.toString == ("demo_mod")
    catch {
      case e: Exception =>
        LOGGER.warn("Error checking mod path: {}", e.getMessage)
    }
    if (modPathIsDemo) {
      setProperty("mod.path", hDir + File.separator + "demo_mod")
      LOGGER.debug("Reset mod path to demo_mod")
    }
    else LOGGER.debug("Mod path already set. Skipping auto-set.")
  }

  def loadMod(): Unit = {
    try
    catch {
      case e: Exception =>
        LOGGER.error("Failed to reload localization", e)
    }
    try if (Interface.read()) setProperty("valid.Interface", "true")
    else {
      setProperty("valid.Interface", "false")
      LOGGER.error("Failed to read gfx interface files")
    }
    catch {
      case e: Exception =>
        setProperty("valid.Interface", "false")
        LOGGER.error("Exception while reading interface files", e)
    }
    try if (ResourcesFile.read()) setProperty("valid.Resources", "true")
    else {
      setProperty("valid.Resources", "false")
      LOGGER.error("Failed to read resources")
    }
    catch {
      case e: Exception =>
        setProperty("valid.Resources", "false")
        LOGGER.error("Exception while reading resources", e)
    }
    try if (CountryTag.read()) setProperty("valid.CountryTag", "true")
    else {
      setProperty("valid.CountryTag", "false")
      LOGGER.error("Failed to read country tags")
    }
    catch {
      case e: Exception =>
        setProperty("valid.CountryTag", "false")
        LOGGER.error("Exception while reading country tags", e)
    }
    try if (Country.read()) setProperty("valid.Country", "true")
    else {
      setProperty("valid.Country", "false")
      LOGGER.error("Failed to read countries")
    }
    catch {
      case e: Exception =>
        setProperty("valid.Country", "false")
        LOGGER.error("Exception while reading countries", e)
    }
    try if (State.read()) setProperty("valid.State", "true")
    else {
      setProperty("valid.State", "false")
      LOGGER.error("Failed to read states")
    }
    catch {
      case e: Exception =>
        setProperty("valid.State", "false")
        LOGGER.error("Exception while reading states", e)
    }
    try if (FocusTree.read()) setProperty("valid.FocusTree", "true")
    else {
      setProperty("valid.FocusTree", "false")
      LOGGER.error("Failed to read focus trees")
    }
    catch {
      case e: Exception =>
        setProperty("valid.FocusTree", "false")
        LOGGER.error("Exception while reading focus trees", e)
    }
    try if (IdeaFile.read()) setProperty("valid.IdeaFiles", "true")
    else {
      setProperty("valid.IdeaFiles", "false")
      LOGGER.error("Failed to read idea files")
    }
    catch {
      case e: Exception =>
        setProperty("valid.IdeaFiles", "false")
        LOGGER.error("Exception while reading idea files", e)
    }
  }

  /**
   * Watches the state files in the given directory.
   * TODO: @Skorp
   * @param stateFiles The directory containing state files.
   */
//  def watchStateFiles(stateFiles: File): Unit = {
//    if (stateFiles == null || !validateDirectoryPath(stateFiles.getPath, "State files directory")) return
//
//    stateFilesWatcher = new FileWatcher(stateFiles)
//    stateFilesWatcher.addListener(new FileAdapter {
//      override def onCreated(event: FileEvent): Unit = {
//        handleStateFileEvent(event, "created/loaded", file => State.readState(file))
//      }
//
//      override def onModified(event: FileEvent): Unit = {
//        handleStateFileEvent(event, "modified", file => State.readState(file))
//      }
//
//      override def onDeleted(event: FileEvent): Unit = {
//        handleStateFileEvent(event, "deleted", file => State.removeState(file))
//      }
//    })
//    stateFilesWatcher.watch()
//  }

  /**
   * Handles state file events.
   * TODO: @Skorp
   * @param event       File event that occurred.
   * @param actionName  Name of the action performed.
   * @param stateAction Function to apply to the file.
   */
//  private def handleStateFileEvent(event: FileEvent, actionName: String, stateAction: File => Unit): Unit = {
//    EventQueue.invokeLater(() => {
//      stateFilesWatcher.listenerPerformAction = stateFilesWatcher.listenerPerformAction + 1
//      val file = event.getFile
//      if (file != null) {
//        stateAction(file)
//      }
//      stateFilesWatcher.listenerPerformAction = stateFilesWatcher.listenerPerformAction - 1
//      LOGGER.debug(s"State was $actionName: ${State.get(file)}")
//    })
//  }

  def addPropertyChangeListener(listener: PropertyChangeListener): Unit = {
    HOIIVUtilsInitializer.changeNotifier.addPropertyChangeListener(listener)
  }

  def removePropertyChangeListener(listener: PropertyChangeListener): Unit = {
    HOIIVUtilsInitializer.changeNotifier.removePropertyChangeListener(listener)
  }
}