package com.hoi4utils.ui.menus

import com.hoi4utils.extensions.validateFolder
import com.hoi4utils.main.HOIIVUtils.*
import com.hoi4utils.main.{HOIIVUtilsConfig, Initializer}
import com.hoi4utils.ui.javafx.application.{HOIIVUtilsAbstractController, HOIIVUtilsAbstractController2, JavaFXUIManager, RootWindows}
import com.typesafe.scalalogging.LazyLogging
import javafx.fxml.FXML
import javafx.scene.Node
import javafx.scene.control.*
import javafx.scene.layout.GridPane
import javafx.stage.Screen

import java.io.{File, FilenameFilter}
import java.net.JarURLConnection
import java.util.{Locale, Properties}
import javax.swing.JOptionPane
import scala.compiletime.uninitialized
import scala.jdk.CollectionConverters.*

/**
 * The SettingsController class is responsible for handling the program settings window and its
 * associated functionality. It provides methods to interact with the settings UI and update the
 * settings accordingly.
 *
 * @author thiccchris
 */
class SettingsController extends HOIIVUtilsAbstractController2 with RootWindows with LazyLogging:
  setFxmlFile("Settings.fxml")
  setTitle(s"HOIIVUtils Settings ${HOIIVUtilsConfig.get("version")}")

  @FXML var contentContainer: GridPane = uninitialized
  @FXML var versionLabel: Label = uninitialized
  @FXML var modPathTextField: TextField = uninitialized
  @FXML var hoi4PathTextField: TextField = uninitialized
  @FXML var modFolderBrowseButton: Button = uninitialized
  @FXML var hoi4FolderBrowseButton: Button = uninitialized
  @FXML var idOkButton: Button = uninitialized
  @FXML var darkTheme: RadioButton = uninitialized
  @FXML var lightTheme: RadioButton = uninitialized
  @FXML var preferredMonitorComboBox: ComboBox[Screen] = uninitialized
  @FXML var languageComboBox: ComboBox[Locale] = uninitialized
  @FXML var debugColorsTButton: ToggleButton = uninitialized
  @FXML var parserIgnoreCommentsCheckBox: CheckBox = uninitialized
  @FXML var maxYTF: TextField = uninitialized
  @FXML var maxXTF: TextField = uninitialized
  @FXML var errorLabel: Label = uninitialized
  @FXML var settingsTabPane: TabPane = uninitialized

  @FXML
  def initialize(): Unit =
    versionLabel.setText(HOIIVUtilsConfig.get("version"))
    loadMonitor()
    loadLanguages()
    // after loading, set saved settings
    loadUIWithSavedSettings()

  override def preSetup(): Unit = setupWindowControls(contentContainer, settingsTabPane)

  private def loadMonitor(): Unit =
    preferredMonitorComboBox.getItems.setAll(Screen.getScreens.asScala.toSeq*)
    preferredMonitorComboBox.setCellFactory: _ =>
      new ListCell[Screen]:
        /**
         * Updates the item in the list view to the given value. The text of the item is set to "Screen
         * <number>: <width>x<height>" if the item is not empty, and null if the item is empty.
         *
         * @param item the item to be updated
         * @param empty whether the item is empty
         */
        override protected def updateItem(item: Screen, empty: Boolean): Unit =
          super.updateItem(item, empty)
          if item == null || empty then setText(null)
          else setText(s"Screen ${getIndex + 1}: ${item.getBounds.getWidth.toInt}x${item.getBounds.getHeight.toInt}") // Set the text of the item to "Screen <number>: <width>x<height>"

  private def loadLanguages(): Unit =
    val locales = detectSupportedLocales()

    languageComboBox.getItems.setAll(locales*)

    //    languageComboBox.setCellFactory(_ => new ListCell[Locale] {
    //      override protected def updateItem(loc: Locale, empty: Boolean): Unit = {
    //        super.updateItem(loc, empty)
    //        setText(if (empty || loc == null) null else s"${loc.getDisplayLanguage(loc)} (${loc.toLanguageTag})")
    //      }
    //    })
    languageComboBox.setCellFactory: _ =>
      new ListCell[Locale]:
        override protected def updateItem(loc: Locale, empty: Boolean): Unit =
          super.updateItem(loc, empty)
          if empty || loc == null then
            setText(null)
            setGraphic(null)
          else
            val tag = loc.toLanguageTag // e.g. "en-GB"
            val emoji =
              val parts = tag.split("-")
              if parts.length == 2 then countryCodeToEmojiFlag(parts(1)) else ""
            setText(s"$emoji  ${loc.getDisplayLanguage(loc)} ($tag)")
            setGraphic(null) // we're embedding the flag via emoji

    languageComboBox.setButtonCell(languageComboBox.getCellFactory.call(null))

    // 3) pre-select saved or default Locale
    val savedTag = Option(HOIIVUtilsConfig.get("locale")).getOrElse(Locale.getDefault.toLanguageTag)
    locales.find(_.toLanguageTag == savedTag).foreach(languageComboBox.getSelectionModel.select)

  /**
   * Scan `resources/i18n` for ANY file matching `*_xx[_YY].properties`,
   * pull out the locale part, and build a unique Seq[Locale].
   */
  private def detectSupportedLocales(): Seq[Locale] =
    val scanDir = "i18n"
    val cl = getClass.getClassLoader

    Option(cl.getResource(scanDir)).toSeq.flatMap: url =>
      url.getProtocol match

        // exploded on disk
        case "file" =>
          val dir = new File(url.toURI)
          val props: Array[File] = dir.listFiles(new FilenameFilter {
            override def accept(dir: File, name: String): Boolean =
              name.matches(""".+_[a-z]{2}(?:_[A-Z]{2})?\.properties""")
          })
          props.toSeq.map(_.getName)

        // packaged inside a JAR
        case "jar" =>
          val conn = url.openConnection().asInstanceOf[JarURLConnection]
          conn.getJarFile.entries.asScala.toSeq.collect:
            case e if !e.isDirectory &&
              e.getName.startsWith(s"$scanDir/") &&
              e.getName.matches(s"""$scanDir/.+_[a-z]{2}(?:_[A-Z]{2})?\\.properties""") =>
              e.getName.substring(e.getName.lastIndexOf('/') + 1)

        case _ => Seq.empty
    .map: filename =>
      val code = filename
        .replaceFirst("^.+?_", "")            // drop up to first underscore
        .stripSuffix(".properties")           // yields "_xx[_YY]"
        .replace('_','-')                     // now "_xx[-YY]"
      Locale.forLanguageTag(code)

  private def countryCodeToEmojiFlag(cc: String): String =
    cc.toUpperCase
      .map(ch => 0x1F1E6 + (ch - 'A')) // U+1F1E6 is 🇦
      .map(cp => new String(Character.toChars(cp)))
      .mkString

  private def loadUIWithSavedSettings(): Unit =
    modPathTextField.clear()
    if !"null".equals(HOIIVUtilsConfig.get("mod.path")) then modPathTextField.setText(HOIIVUtilsConfig.get("mod.path"))

    hoi4PathTextField.clear()
    if !"null".equals(HOIIVUtilsConfig.get("hoi4.path")) then hoi4PathTextField.setText(HOIIVUtilsConfig.get("hoi4.path"))

    darkTheme.setSelected(HOIIVUtilsConfig.get("theme") == "dark")
    lightTheme.setSelected(HOIIVUtilsConfig.get("theme") == "light")

    preferredMonitorComboBox.getSelectionModel.select(validateAndGetPreferredScreen())

    //languageComboBox.getSelectionModel.select()

    debugColorsTButton.setSelected(HOIIVUtilsConfig.get("debug.colors").toBoolean)
    debugColorsTButton.setText(if debugColorsTButton.isSelected then "ON" else "OFF")

    // parser settings:
    parserIgnoreCommentsCheckBox.setSelected(HOIIVUtilsConfig.get("parser.ignore.comments").toBoolean)

  def handleModPathTextField(): Unit = validateAndSetPath(modPathTextField.getText, "mod.path")

  def handleHOIIVPathTextField(): Unit = validateAndSetPath(hoi4PathTextField.getText, "hoi4.path")

  /**
   * Handle mod folder browse button action.
   * Uses standard Paradox Interactive mod directory as preferred location.
   */
  def handleModFileBrowseAction(): Unit =
    val preferredDir = getParadoxModDirectory()
    selectAndUpdatePath(modPathTextField, modFolderBrowseButton, preferredDir, "mod.path")

  /**
   * Handle HOI4 installation folder browse button action.
   * Uses standard Steam installation path as preferred location.
   */
  def handleHOIIVFileBrowseAction(): Unit =
    val preferredDir = getSteamHOI4Directory()
    selectAndUpdatePath(hoi4PathTextField, hoi4FolderBrowseButton, preferredDir, "hoi4.path")

  /**
   * Generic method to select a directory and update the associated text field and config.
   *
   * @param textField    The text field to update with the selected path
   * @param browseButton The button that triggered the action (for stage ownership)
   * @param preferredDir Optional preferred initial directory
   * @param configKey    The configuration key to save the path to
   */
  private def selectAndUpdatePath(
                                   textField: TextField,
                                   browseButton: Node,
                                   preferredDir: Option[File],
                                   configKey: String
                                 ): Unit =
    selectDirectory(browseButton, preferredDir) match
      case Some(selectedDir) =>
        HOIIVUtilsConfig.set(configKey, selectedDir.getAbsolutePath)
        textField.setText(selectedDir.getAbsolutePath)
        logger.debug(s"Updated $configKey to: ${selectedDir.getAbsolutePath}")
      case None =>
        logger.debug(s"Directory selection cancelled for $configKey")

  /**
   * Validates and saves a path from a text field to config.
   * Called when user manually types a path.
   *
   * @param path      The path string to validate
   * @param configKey The configuration key to save to
   */
  private def validateAndSetPath(path: String, configKey: String): Unit =
    validateDirectoryPath(path) match
      case Some(validDir) =>
        HOIIVUtilsConfig.set(configKey, validDir.getAbsolutePath)
        logger.debug(s"Validated and saved $configKey: ${validDir.getAbsolutePath}")
      case None if path.nonEmpty =>
        logger.warn(s"Invalid directory path for $configKey: $path")
        errorLabel.setText(s"Invalid directory: $path")
      case None => // Empty path, do nothing

  /**
   * Gets the standard Paradox Interactive Hearts of Iron IV mod directory.
   * Falls back through common locations if primary location doesn't exist.
   *
   * @return Option containing the mod directory if found
   */
  private def getParadoxModDirectory(): Option[File] =
    val userHome = getSystemDirectory("user.home")
    val candidates = Seq(
      userHome.map(home => buildPath(home.getPath, "Documents", "Paradox Interactive", "Hearts of Iron IV", "mod")),
      userHome.map(home => buildPath(home.getPath, "OneDrive", "Documents", "Paradox Interactive", "Hearts of Iron IV", "mod")),
      userHome.map(home => buildPath(home.getPath, "Paradox Interactive", "Hearts of Iron IV", "mod"))
    ).flatten
    candidates.find(f => f.validateFolder("mod").isRight) match
      case found@Some(dir) =>
        logger.debug(s"Found mod directory: ${dir.getAbsolutePath}")
        found
      case None =>
        logger.debug("Could not find Paradox mod directory, will use default location")
        None

  /**
   * Gets the standard Steam Hearts of Iron IV installation directory.
   * Checks common Steam installation locations across different drive configurations.
   *
   * @return Option containing the HOI4 directory if found
   */
  private def getSteamHOI4Directory(): Option[File] =
    val steamPath = Seq("Steam", "steamapps", "common", "Hearts of Iron IV")
    val programFilesX86 = Option(System.getenv("ProgramFiles(x86)")).map(new File(_))
    val programFiles = Option(System.getenv("ProgramFiles")).map(new File(_))
    val candidates = Seq(
      programFilesX86.map(base => buildPath((base.getPath +: steamPath) *)),
      programFiles.map(base => buildPath((base.getPath +: steamPath) *)),
      Some(buildPath(("C:" +: steamPath) *)),
      Some(buildPath(("D:" +: steamPath) *))
    ).flatten
    candidates.find(f => f.validateFolder("HOI4").isRight) match
      case found@Some(dir) =>
        logger.debug(s"Found HOI4 installation: ${dir.getAbsolutePath}")
        found
      case None =>
        logger.debug("Could not find Steam HOI4 installation, will use default location")
        None

  def handleDarkThemeRadioAction(): Unit = if darkTheme.isSelected then HOIIVUtilsConfig.set("theme", "dark")

  def handleLightThemeRadioAction(): Unit = if lightTheme.isSelected then HOIIVUtilsConfig.set("theme", "light")

  /**
   * change preferred monitor setting.
   * location upon decision/etc?
   * monitors are labeled with ints, default being 0
   * interpret index of selection as monitor selection
   */
  def handlePreferredMonitorSelection(): Unit =
    HOIIVUtilsConfig.set("preferred.screen", preferredMonitorComboBox.getSelectionModel.getSelectedIndex.toString)

  def handleLanguageSelection(): Unit =
    val newLoc = languageComboBox.getValue
    if newLoc != null then
      HOIIVUtilsConfig.getConfig.getProperties.setProperty("locale", newLoc.toLanguageTag)
      logger.debug(s"Locale set to ${newLoc.toLanguageTag}")

  def handleDebugColorsAction(): Unit =
    if debugColorsTButton.isSelected then
      HOIIVUtilsConfig.set("debug.colors", "true")
      debugColorsTButton.setText("ON")
    else
      HOIIVUtilsConfig.set("debug.colors", "false")
      debugColorsTButton.setText("OFF")

  def handleParserIgnoreCommentsAction(): Unit = HOIIVUtilsConfig.set("parser.ignore.comments", parserIgnoreCommentsCheckBox.isSelected.toString)

  // This method is called when the user clicks on an empty area of the settings window.
  def handleEmptyClick(): Unit =
    handleHOIIVPathTextField()
    handleModPathTextField()

  /**
   * User Interactive Button in Settings Window Closes Settings Window Opens Menu Window
   */
  def handleOkButtonAction(): Unit =
    HOIIVUtilsConfig.save()
    new Initializer().initialize(HOIIVUtilsConfig.getConfig)
    hideWindow(idOkButton)
    new MenuController().open()
