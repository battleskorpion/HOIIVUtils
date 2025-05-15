package com.hoi4utils

import com.hoi4utils.clausewitz.HOIIVFiles
import com.hoi4utils.fileIO.FileListener.{FileAdapter, FileEvent, FileWatcher}
import com.hoi4utils.gfx.Interface
import com.hoi4utils.hoi4.country.{Country, CountryTag}
import com.hoi4utils.hoi4.focus.FocusTree
import com.hoi4utils.hoi4.idea.IdeaFile
import com.hoi4utils.localization.{EnglishLocalizationManager, LocalizationManager}
import map.{ResourcesFile, State}
import org.apache.logging.log4j.LogManager

import java.awt.*
import java.beans.PropertyChangeListener
import java.io.*
import java.net.URISyntaxException
import java.nio.file.Paths
import java.util
import java.util.{Optional, Properties}
import javax.swing.*

/**
 * Handles initialization of the HOIIVUtils application.
 * Separates initialization logic from utility functions.
 */
object HOIIVUtilsInitializer {
  private val LOGGER = LogManager.getLogger(classOf[HOIIVUtilsInitializer])
  val changeNotifier = new PublicFieldChangeNotifier(classOf[HOIIVUtilsInitializer])
}

class HOIIVUtilsInitializer {
  final private val properties = new Properties
  @SuppressWarnings(Array("exports")) private var stateFilesWatcher = null
  private var hoi4UtilsDir: File = null
  private var propertiesFile: String = null
  private var defaultProperties: InputStream = null
  private var version: Version = null

  /**
   * Initializes the HOIIVUtils application.
   *
   * @return Configured HOIIVUtils configuration for use by the application
   */
  def initialize: HOIIVUtilsConfig = {
    HOIIVUtilsInitializer.LOGGER.info("Initializing HOIIVUtils")
    // Load databases first
    com.hoi4utils.hoi4.modifier.ModifierDatabase.init()
    com.hoi4utils.hoi4.effect.EffectDatabase.init()
    // Configure application directories
    resolveApplicationDirectory()
    // Load configuration
    initializeConfiguration()
    // Configure paths
    autoSetHOIIVPath()
    autoSetDemoModPath()
    // Save configuration after initialization
    saveConfiguration()
    // Set version
    version = Version.apply(getProperty("version"))
    HOIIVUtilsInitializer.LOGGER.info("HOIIVUtils {} initialized", version)
    // Return configuration for use by application
    return createConfig
  }

  private def resolveApplicationDirectory(): Unit = {
    try {
      val sourceLocation = classOf[HOIIVUtils].getProtectionDomain.getCodeSource.getLocation.toURI
      val sourceFile = new File(sourceLocation)
      // Check if parent exists
      val parentDir = new File(sourceFile.getParent)
      if (!(parentDir.exists)) {
        HOIIVUtilsInitializer.LOGGER.warn("Parent directory does not exist: {}", parentDir)
        throw new RuntimeException("Failed to determine application parent directory")
      }
      hoi4UtilsDir = new File(parentDir.getParent)
      val isInvalidDir = !(hoi4UtilsDir.exists) || !(hoi4UtilsDir.isDirectory) || hoi4UtilsDir == null
      if (isInvalidDir) {
        HOIIVUtilsInitializer.LOGGER.warn("Invalid HOIIVUTILS_DIR: {}", hoi4UtilsDir)
        throw new RuntimeException("Invalid HOIIVUtils directory")
      }
    } catch {
      case e: URISyntaxException =>
        HOIIVUtilsInitializer.LOGGER.error("Failed to determine application directory", e)
        throw new RuntimeException("Failed to determine application directory", e)
      case e: NullPointerException =>
        HOIIVUtilsInitializer.LOGGER.error("Null reference encountered while determining application directory", e)
        throw new RuntimeException("Null reference in application directory resolution", e)
      case e: Exception =>
        HOIIVUtilsInitializer.LOGGER.error("Unexpected error while determining application directory", e)
        throw new RuntimeException("Unexpected error determining application directory", e)
    }
    HOIIVUtilsInitializer.LOGGER.debug("HOIIVUtils Directory: {}", hoi4UtilsDir)
  }

  private def initializeConfiguration(): Unit = {
    propertiesFile = hoi4UtilsDir + File.separator + "HOIIVUtils.properties"
    defaultProperties = classOf[HOIIVUtils].getClassLoader.getResourceAsStream("HOIIVUtils.properties")
    loadConfiguration()
  }

  private def loadConfiguration(): Unit = {
    val externalFile = new File(propertiesFile)
    if (!(externalFile.exists)) {
      HOIIVUtilsInitializer.LOGGER.warn("External configuration file not found: {}", propertiesFile)
      loadDefaultProperties()
    }
    try {
      val input = new FileInputStream(externalFile)
      try {
        HOIIVUtilsInitializer.LOGGER.debug("External configuration loaded from: {}", propertiesFile)
        properties.load(input)
      } catch {
        case e: IOException =>
          HOIIVUtilsInitializer.LOGGER.fatal("Error loading external properties from: {}", propertiesFile, e)
          loadDefaultProperties()
      } finally if (input != null) input.close()
    }
  }

  private def loadDefaultProperties(): Unit = {
    val externalFile = new File(propertiesFile)
    try {
      val externalOut = new FileOutputStream(externalFile)
      try {
        val buffer = new Array[Byte](8192)
        var bytesRead = 0
        while ((bytesRead = defaultProperties.read(buffer)) != -(1)) externalOut.write(buffer, 0, bytesRead)
        HOIIVUtilsInitializer.LOGGER.debug("Default properties copied to: {}", externalFile.getAbsolutePath)
      } catch {
        case e: IOException =>
          HOIIVUtilsInitializer.LOGGER.error("Failed to copy default properties to external file", e)
      } finally if (externalOut != null) externalOut.close()
    }
  }

  def saveConfiguration(): Unit = {
    val externalFile = new File(propertiesFile)
    val noSavedSettings = !(externalFile.exists) && defaultProperties != null
    if (noSavedSettings) loadDefaultProperties()
    try {
      val output = new FileOutputStream(externalFile)
      try {
        HOIIVUtilsInitializer.LOGGER.debug("Configuration saved to: {}", externalFile.getAbsolutePath)
        properties.store(output, "HOIIVUtils Configuration")
      } catch {
        case e: IOException =>
          HOIIVUtilsInitializer.LOGGER.error("Failed to save configuration", e)
          throw new RuntimeException(e)
      } finally if (output != null) output.close()
    }
  }

  private def autoSetHOIIVPath(): Unit = {
    val hoi4Path = getProperty("hoi4.path")
    val hoi4PathNotSet = hoi4Path == null || hoi4Path.isBlank
    if (!(hoi4PathNotSet)) {
      HOIIVUtilsInitializer.LOGGER.debug("HOI4 path already set. Skipping auto-set.")
      return
    }
    for (path <- getPossibleHOIIVPaths) {
      val hoi4Dir = Paths.get(path).toAbsolutePath.toFile
      if (hoi4Dir.exists) {
        setProperty("hoi4.path", hoi4Dir.getAbsolutePath)
        HOIIVUtilsInitializer.LOGGER.debug("Auto-set HOI4 path: {}", hoi4Dir.getAbsolutePath)
        return
      }
    }
    HOIIVUtilsInitializer.LOGGER.warn("Couldn't find HOI4 install folder. User must set it manually.")
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
    return possibleHOIIVPaths
  }

  private def autoSetDemoModPath(): Unit = {
    val modPath = getProperty("mod.path")
    val modPathNotSet = modPath == null || modPath.isBlank
    if (modPathNotSet) {
      setProperty("mod.path", hoi4UtilsDir + File.separator + "demo_mod")
      HOIIVUtilsInitializer.LOGGER.debug("Auto-set mod path to demo_mod")
      return
    }
    var modPathIsDemo = false
    try modPathIsDemo = Paths.get(modPath).getFileName.toString == ("demo_mod")
    catch {
      case e: Exception =>
        HOIIVUtilsInitializer.LOGGER.warn("Error checking mod path: {}", e.getMessage)
    }
    if (modPathIsDemo) {
      setProperty("mod.path", hoi4UtilsDir + File.separator + "demo_mod")
      HOIIVUtilsInitializer.LOGGER.debug("Reset mod path to demo_mod")
    }
    else HOIIVUtilsInitializer.LOGGER.debug("Mod path already set. Skipping auto-set.")
  }

  def loadMod(): Unit = {
    if (!(createHOIIVFilePaths)) {
      HOIIVUtilsInitializer.LOGGER.error("Failed to create HOIIV file paths")
      setProperty("valid.HOIIVFilePaths", "false")
    }
    else setProperty("valid.HOIIVFilePaths", "true")
    try LocalizationManager.getOrCreate(EnglishLocalizationManager.`new`).reload()
    catch {
      case e: Exception =>
        HOIIVUtilsInitializer.LOGGER.error("Failed to reload localization", e)
    }
    try if (Interface.read) setProperty("valid.Interface", "true")
    else {
      setProperty("valid.Interface", "false")
      HOIIVUtilsInitializer.LOGGER.error("Failed to read gfx interface files")
    }
    catch {
      case e: Exception =>
        setProperty("valid.Interface", "false")
        HOIIVUtilsInitializer.LOGGER.error("Exception while reading interface files", e)
    }
    try if (ResourcesFile.read) setProperty("valid.Resources", "true")
    else {
      setProperty("valid.Resources", "false")
      HOIIVUtilsInitializer.LOGGER.error("Failed to read resources")
    }
    catch {
      case e: Exception =>
        setProperty("valid.Resources", "false")
        HOIIVUtilsInitializer.LOGGER.error("Exception while reading resources", e)
    }
    try if (CountryTag.read) setProperty("valid.CountryTag", "true")
    else {
      setProperty("valid.CountryTag", "false")
      HOIIVUtilsInitializer.LOGGER.error("Failed to read country tags")
    }
    catch {
      case e: Exception =>
        setProperty("valid.CountryTag", "false")
        HOIIVUtilsInitializer.LOGGER.error("Exception while reading country tags", e)
    }
    try if (Country.read) setProperty("valid.Country", "true")
    else {
      setProperty("valid.Country", "false")
      HOIIVUtilsInitializer.LOGGER.error("Failed to read countries")
    }
    catch {
      case e: Exception =>
        setProperty("valid.Country", "false")
        HOIIVUtilsInitializer.LOGGER.error("Exception while reading countries", e)
    }
    try if (State.read) setProperty("valid.State", "true")
    else {
      setProperty("valid.State", "false")
      HOIIVUtilsInitializer.LOGGER.error("Failed to read states")
    }
    catch {
      case e: Exception =>
        setProperty("valid.State", "false")
        HOIIVUtilsInitializer.LOGGER.error("Exception while reading states", e)
    }
    try if (FocusTree.read) setProperty("valid.FocusTree", "true")
    else {
      setProperty("valid.FocusTree", "false")
      HOIIVUtilsInitializer.LOGGER.error("Failed to read focus trees")
    }
    catch {
      case e: Exception =>
        setProperty("valid.FocusTree", "false")
        HOIIVUtilsInitializer.LOGGER.error("Exception while reading focus trees", e)
    }
    try if (IdeaFile.read) setProperty("valid.IdeaFiles", "true")
    else {
      setProperty("valid.IdeaFiles", "false")
      HOIIVUtilsInitializer.LOGGER.error("Failed to read idea files")
    }
    catch {
      case e: Exception =>
        setProperty("valid.IdeaFiles", "false")
        HOIIVUtilsInitializer.LOGGER.error("Exception while reading idea files", e)
    }
  }

  private def createHOIIVFilePaths: Boolean = {
    if (!(createHOIIVPaths)) return false
    if (!(createModPaths)) return false
    HOIIVUtilsInitializer.changeNotifier.checkAndNotifyChanges()
    return true
  }

  private def createModPaths: Boolean = {
    val modPath = getProperty("mod.path")
    if (!(validateDirectoryPath(modPath, "mod.path"))) return false
    HOIIVFiles.setModPathChildDirs(modPath)
    return true
  }

  private def createHOIIVPaths: Boolean = {
    val hoi4Path = getProperty("hoi4.path")
    if (!(validateDirectoryPath(hoi4Path, "hoi4.path"))) return false
    HOIIVFiles.setHoi4PathChildDirs(hoi4Path)
    return true
  }

  /** Validates whether the provided directory path is valid */
  private def validateDirectoryPath(path: String, keyName: String): Boolean = {
    if (path == null || path.isEmpty) {
      HOIIVUtilsInitializer.LOGGER.error("{} is null or empty!", keyName)
      // Log but don't show popup - we'll show a consolidated warning later
      return false
    }
    val directory = new File(path)
    if (!(directory.exists) || !(directory.isDirectory)) {
      HOIIVUtilsInitializer.LOGGER.error("{} does not point to a valid directory: {}", keyName, path)
      // Log but don't show popup - we'll show a consolidated warning later
      return false
    }
    return true
  }

  private def createConfig: HOIIVUtilsConfig = return new HOIIVUtilsConfig(hoi4UtilsDir, propertiesFile, defaultProperties, version, properties)

  def getProperty(key: String): String = return properties.getProperty(key)

  def setProperty(key: String, value: String): Unit = {
    properties.setProperty(key, value)
  }

  /**
   * Watches the state files in the given directory.
   *
   * @param stateFiles The directory containing state files.
   */
  def watchStateFiles(stateFiles: File): Unit = {
    if (!(validateDirectoryPath(Optional.ofNullable(stateFiles).map(File.getPath).orElse(null), "State files directory"))) return
    stateFilesWatcher = new FileWatcher(stateFiles)
    stateFilesWatcher.addListener(new FileAdapter() {
      override def onCreated(event: FileEvent): Unit = {
        handleStateFileEvent(event, "created/loaded", State.readState)
      }

      override def onModified(event: FileEvent): Unit = {
        handleStateFileEvent(event, "modified", State.readState)
      }

      override def onDeleted(event: FileEvent): Unit = {
        handleStateFileEvent(event, "deleted", State.removeState)
      }
    }).watch()
  }

  /**
   * Handles state file events.
   *
   * @param event       File event that occurred.
   * @param actionName  Name of the action performed.
   * @param stateAction Function to apply to the file.
   */
  private def handleStateFileEvent(event: FileEvent, actionName: String, stateAction: Consumer[File]): Unit = {
    EventQueue.invokeLater(() => {
      stateFilesWatcher.listenerPerformAction += 1
      val file = event.getFile
      stateAction.accept(file)
      stateFilesWatcher.listenerPerformAction -= 1
      HOIIVUtilsInitializer.LOGGER.debug("State was {}: {}", actionName, State.get(file))
    })
  }

  def addPropertyChangeListener(listener: PropertyChangeListener): Unit = {
    HOIIVUtilsInitializer.changeNotifier.addPropertyChangeListener(listener)
  }

  def removePropertyChangeListener(listener: PropertyChangeListener): Unit = {
    HOIIVUtilsInitializer.changeNotifier.removePropertyChangeListener(listener)
  }
}