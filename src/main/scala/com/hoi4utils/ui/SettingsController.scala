package com.hoi4utils.ui

import com.hoi4utils.{HOIIVFiles, HOIIVUtils}
import com.typesafe.scalalogging.LazyLogging
import javafx.fxml.FXML
import javafx.scene.Node
import javafx.scene.control.*
import javafx.stage.Screen

import java.io.{File, FilenameFilter}
import java.net.JarURLConnection
import java.util.Locale
import scala.compiletime.uninitialized
import scala.jdk.CollectionConverters.*

/**
 * The SettingsController class is responsible for handling the program settings window and its
 * associated functionality. It provides methods to interact with the settings UI and update the
 * settings accordingly.
 *
 * @author thiccchris
 */
class SettingsController extends HOIIVUtilsAbstractController with JavaFXUIManager with LazyLogging {
  setFxmlResource("Settings.fxml")
  setTitle(s"HOIIVUtils Settings ${HOIIVUtils.get("version")}")

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

  @FXML
  def initialize(): Unit = {
    versionLabel.setText(HOIIVUtils.get("version").toString)
    loadMonitor()
    loadLanguages()
    // after loading, set saved settings
    loadUIWithSavedSettings()
  }

  private def loadMonitor(): Unit = {
    preferredMonitorComboBox.getItems.setAll(Screen.getScreens.asScala.toSeq*)
    preferredMonitorComboBox.setCellFactory(_ => new ListCell[Screen] {
      /**
       * Updates the item in the list view to the given value. The text of the item is set to "Screen
       * <number>: <width>x<height>" if the item is not empty, and null if the item is empty.
       *
       * @param item the item to be updated
       * @param empty whether the item is empty
       */
      override protected def updateItem(item: Screen, empty: Boolean): Unit = {
        super.updateItem(item, empty)
        if (item == null || empty) {
          setText(null)
        } else {
          // Set the text of the item to "Screen <number>: <width>x<height>"
          setText(s"Screen ${getIndex + 1}: ${item.getBounds.getWidth.toInt}x${item.getBounds.getHeight.toInt}")
        }
      }
    })
  }

  private def loadLanguages(): Unit = {
    val locales = detectSupportedLocales()

    languageComboBox.getItems.setAll(locales: _*)

//    languageComboBox.setCellFactory(_ => new ListCell[Locale] {
//      override protected def updateItem(loc: Locale, empty: Boolean): Unit = {
//        super.updateItem(loc, empty)
//        setText(if (empty || loc == null) null else s"${loc.getDisplayLanguage(loc)} (${loc.toLanguageTag})")
//      }
//    })
    languageComboBox.setCellFactory { _ =>
      new ListCell[Locale] {
        override protected def updateItem(loc: Locale, empty: Boolean): Unit = {
          super.updateItem(loc, empty)
          if (empty || loc == null) {
            setText(null)
            setGraphic(null)
          } else {
            val tag = loc.toLanguageTag // e.g. "en-GB"
            val emoji = {
              val parts = tag.split("-")
              if (parts.length == 2) countryCodeToEmojiFlag(parts(1)) else ""
            }
            setText(s"$emoji  ${loc.getDisplayLanguage(loc)} ($tag)")
            setGraphic(null) // we’re embedding the flag via emoji
          }
        }
      }
    }
    languageComboBox.setButtonCell(languageComboBox.getCellFactory.call(null))

    // 3) pre-select saved or default Locale
    val savedTag = Option(HOIIVUtils.config.getProperties.getProperty("locale"))
      .getOrElse(Locale.getDefault.toLanguageTag)
    locales.find(_.toLanguageTag == savedTag)
      .foreach(languageComboBox.getSelectionModel.select)
  }
      
  /**
   * Scan `resources/i18n` for ANY file matching `*_xx[_YY].properties`,
   * pull out the locale part, and build a unique Seq[Locale].
   */
  private def detectSupportedLocales(): Seq[Locale] = {
    val scanDir = "i18n"
    val cl = getClass.getClassLoader

    Option(cl.getResource(scanDir)).toSeq.flatMap { url =>
        url.getProtocol match {

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
            conn.getJarFile.entries.asScala.toSeq.collect {
              case e if !e.isDirectory &&
                e.getName.startsWith(s"$scanDir/") &&
                e.getName.matches(s"""$scanDir/.+_[a-z]{2}(?:_[A-Z]{2})?\\.properties""") =>
                e.getName.substring(e.getName.lastIndexOf('/') + 1)
            }

          case _ => Seq.empty
        }
      }
      .map { filename =>
        val code = filename
          .replaceFirst("^.+?_", "")            // drop up to first underscore
          .stripSuffix(".properties")           // yields "_xx[_YY]"
          .replace('_','-')                     // now "_xx[-YY]"
        Locale.forLanguageTag(code)
      }

  }

  private def countryCodeToEmojiFlag(cc: String): String = {
    cc.toUpperCase
      .map(ch => 0x1F1E6 + (ch - 'A')) // U+1F1E6 is 🇦
      .map(cp => new String(Character.toChars(cp)))
      .mkString
  }

  private def loadUIWithSavedSettings(): Unit = {
    modPathTextField.clear()
    if (!"null".equals(HOIIVUtils.get("mod.path"))) {
      modPathTextField.setText(HOIIVUtils.get("mod.path"))
    }

    hoi4PathTextField.clear()
    if (!"null".equals(HOIIVUtils.get("hoi4.path"))) {
      hoi4PathTextField.setText(HOIIVUtils.get("hoi4.path"))
    }

    darkTheme.setSelected(HOIIVUtils.get("theme") == "dark")
    lightTheme.setSelected(HOIIVUtils.get("theme") == "light")

    preferredMonitorComboBox.getSelectionModel.select(validateAndGetPreferredScreen())

    //languageComboBox.getSelectionModel.select()

    debugColorsTButton.setSelected(HOIIVUtils.get("debug.colors").toBoolean)
    debugColorsTButton.setText(if (debugColorsTButton.isSelected) "ON" else "OFF")

    maxXTF.setText(HOIIVUtils.get("canvas.max.width"))
    maxYTF.setText(HOIIVUtils.get("canvas.max.height"))

    // parser settings:
    parserIgnoreCommentsCheckBox.setSelected(HOIIVUtils.get("parser.ignore.comments").toBoolean)
  }

  def handleModPathTextField(): Unit = {
    validateAndSetPath(modPathTextField.getText, "mod.path")
  }

  def handleHOIIVPathTextField(): Unit = {
    validateAndSetPath(hoi4PathTextField.getText, "hoi4.path")
  }

  def handleModFileBrowseAction(): Unit = {
    val usersDocuments = s"${System.getProperty("user.home")}${File.separator}Documents"
    val initialModDir = new File(s"$usersDocuments${File.separator}${File.separator + "Paradox Interactive" + File.separator + "Hearts of Iron IV" + File.separator + "mod"}")
    handleFileBrowseAction(modPathTextField, modFolderBrowseButton, initialModDir, "mod.path")
  }

  def handleHOIIVFileBrowseAction(): Unit = {
    val steamHOI4LocalPath = s"Steam${File.separator}steamapps${File.separator}common${File.separator}Hearts of Iron IV"
    val programFilesX86 = Option(System.getenv("ProgramFiles(x86)")).map(new File(_))
    val initialHoi4Dir = programFilesX86.map(file => new File(s"$file${File.separator}$steamHOI4LocalPath")).orNull
    handleFileBrowseAction(hoi4PathTextField, hoi4FolderBrowseButton, initialHoi4Dir, "hoi4.path")
  }

  // Generic method to validate a directory path and set it to settings
  private def validateAndSetPath(path: String, settingKey: String): Unit = {
    if (path.isEmpty) return

    val file = new File(path)

    val isValidFile = file.exists && file.isDirectory

    if (isValidFile) {
      HOIIVUtils.set(settingKey, file.toString)
    }
  }

  private def handleFileBrowseAction(textField: TextField, browseButton: Node,
                                     initialDirectory: File, settingKey: String): Unit = {
    if (initialDirectory == null || !initialDirectory.exists || !initialDirectory.isDirectory) {
      logger.warn(s"Initial directory for $settingKey is invalid: ${initialDirectory.getAbsolutePath}")
      return
    }

    val selectedFile = try JavaFXUIManager.openChooser(browseButton, initialDirectory, true)
    catch {
      case e: Exception =>
        logger.error(s"Error opening file chooser for $settingKey: ${e.getMessage}", e)
        JavaFXUIManager.openChooser(browseButton, true)
    }

    if (selectedFile == null) return

    val isValidFile = selectedFile.exists && selectedFile.isDirectory

    if (isValidFile) {
      HOIIVUtils.set(settingKey, selectedFile.toString)
    }

    textField.setText(selectedFile.getAbsolutePath)
  }

  def handleDarkThemeRadioAction(): Unit = {
    if (darkTheme.isSelected) {
      HOIIVUtils.set("theme", "dark")
    }
  }

  def handleLightThemeRadioAction(): Unit = {
    if (lightTheme.isSelected) {
      HOIIVUtils.set("theme", "light")
    }
  }

  /**
   * change preferred monitor setting.
   * location upon decision/etc?
   * monitors are labeled with ints, default being 0
   * interpret index of selection as monitor selection
   */
  def handlePreferredMonitorSelection(): Unit = {
    HOIIVUtils.set("preferred.screen", preferredMonitorComboBox.getSelectionModel.getSelectedIndex.toString)
  }

  def handleLanguageSelection(): Unit = {
    val newLoc = languageComboBox.getValue
    if (newLoc != null) {
      HOIIVUtils.config.getProperties.setProperty("locale", newLoc.toLanguageTag)
      logger.debug(s"Locale set to ${newLoc.toLanguageTag}")
    }
  }

  def handleDebugColorsAction(): Unit = {
    if (debugColorsTButton.isSelected) {
      HOIIVUtils.set("debug.colors", "true")
      debugColorsTButton.setText("ON")
    } else {
      HOIIVUtils.set("debug.colors", "false")
      debugColorsTButton.setText("OFF")
    }
  }

  def handleParserIgnoreCommentsAction(): Unit = {
    HOIIVUtils.set("parser.ignore.comments", parserIgnoreCommentsCheckBox.isSelected.toString)
  }

  def handleMaxXTF(): Unit = {
    var maxX = maxXTF.getText
    try {
      if (maxX == null || maxX == "") maxX = "0"
      val maxXValue = maxX.toDouble
      maxXTF.setText(maxXValue.toString)
      if (maxXValue < 1000 && maxXValue > -1000) throw new NumberFormatException("Max X value can't be between -1000 and 1000.")
      HOIIVUtils.set("canvas.max.width", maxXValue.toString)
      logger.debug(s"canvas.max.width set to $maxXValue")
      errorLabel.setVisible(false)
      idOkButton.setDisable(false)
    } catch {
      case e: NumberFormatException =>
        logger.error(s"Invalid canvas.max.width value: ${e.getMessage}")
        errorLabel.setVisible(true)
        idOkButton.setDisable(true)
        errorLabel.setText(s"Invalid canvas.max.width value. Please enter a valid integer. ${e.getMessage}")
    }
  }

  def handleMaxYTF(): Unit = {
    var maxY = maxYTF.getText
    try {
      if (maxY == null || maxY == "") maxY = "0"
      val maxYValue = maxY.toDouble
      maxYTF.setText(maxYValue.toString)
      if (maxYValue < 1000 && maxYValue > -1000) throw new NumberFormatException("Max Y value can't be between -1000 and 1000.")
      HOIIVUtils.set("canvas.max.height", maxYValue.toString)
      logger.debug(s"canvas.max.height set to $maxYValue")
      idOkButton.setDisable(false)
      errorLabel.setVisible(false)
    } catch {
      case e: NumberFormatException =>
        logger.error(s"Invalid canvas.max.height value: ${e.getMessage}")
        errorLabel.setVisible(true)
        errorLabel.setText(s"Invalid canvas.max.height value. Please enter a valid integer. ${e.getMessage}")
        idOkButton.setDisable(true)
    }
  }

  // This method is called when the user clicks on an empty area of the settings window.
  def handleEmptyClick(): Unit = {
    logger.debug("Empty click in settings window detected, handling all text fields.")
    handleMaxYTF()
    handleMaxXTF()
    handleHOIIVPathTextField()
    handleModPathTextField()
  }
  /**
   * User Interactive Button in Settings Window Closes Settings Window Opens Menu Window
   */
  def handleOkButtonAction(): Unit = {
    HOIIVUtils.save()
    hideWindow(idOkButton)
    new MenuController().open()
  }
}
