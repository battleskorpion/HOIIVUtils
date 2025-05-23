package com.hoi4utils.ui

import com.hoi4utils.clausewitz.HOIIVFiles
import com.hoi4utils.{Config, HOIIVUtils}
import com.typesafe.scalalogging.LazyLogging
import javafx.application.Application
import javafx.fxml.{FXML, FXMLLoader}
import javafx.scene.{Node, Parent, Scene}
import javafx.scene.control.*
import javafx.stage.{Screen, Stage}

import javax.swing.JOptionPane
import java.io.{File, IOException}
import scala.jdk.CollectionConverters.*
import scala.util.{Failure, Success, Try}
import scala.util.matching.Regex

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

  @FXML
  var versionLabel: Label = _
  @FXML
  var modPathTextField: TextField = _
  @FXML
  var hoi4PathTextField: TextField = _
  @FXML
  var modFolderBrowseButton: Button = _
  @FXML
  var hoi4FolderBrowseButton: Button = _
  @FXML
  var idOkButton: Button = _
  @FXML
  var darkTheme: RadioButton = _
  @FXML
  var lightTheme: RadioButton = _
  @FXML
  var preferredMonitorComboBox: ComboBox[Screen] = _
  @FXML
  var debugColorsTButton: ToggleButton = _
  @FXML
  var parserIgnoreCommentsCheckBox: CheckBox = _

  @FXML
  def initialize(): Unit = {
    versionLabel.setText(HOIIVUtils.get("version").toString)
    loadUIWithSavedSettings()
    loadMonitor()
  }

  private def loadMonitor(): Unit = {
    preferredMonitorComboBox.getItems.setAll(Screen.getScreens.asScala.toSeq: _*)
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

    debugColorsTButton.setSelected(HOIIVUtils.get("debug.colors").toBoolean)
    debugColorsTButton.setText(if (debugColorsTButton.isSelected) "ON" else "OFF")

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
    val initialModDir = new File(s"$usersDocuments${File.separator}${HOIIVFiles.usersParadoxHOIIVModFolder}")
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

  // Generic method to handle file browser actions
  private def handleFileBrowseAction(textField: TextField, browseButton: Node,
                                     initialDirectory: File, settingKey: String): Unit = {
    val selectedFile = JavaFXUIManager.openChooser(browseButton, initialDirectory, true)

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

  /**
   * User Interactive Button in Settings Window Closes Settings Window Opens Menu Window
   */
  def handleOkButtonAction(): Unit = {
    HOIIVUtils.save()
    hideWindow(idOkButton)
    new MenuController().open()
  }
}
