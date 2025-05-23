package com.hoi4utils.ui

import com.hoi4utils.HOIIVUtils.config
import com.hoi4utils.{Config, HOIIVUtils, Initializer, ModLoader, Updater, Version}
import com.hoi4utils.clausewitz.HOIIVFiles
import com.hoi4utils.ui.buildings.BuildingsByCountryController
import com.hoi4utils.ui.focus_view.FocusTreeController
import com.hoi4utils.ui.hoi4localization.{FocusLocalizationController, IdeaLocalizationController, ManageFocusTreesController}
import com.hoi4utils.ui.map.{MapEditorController, MapGenerationController}
import com.hoi4utils.ui.parser.ParserViewerController
import com.typesafe.scalalogging.LazyLogging
import javafx.application.{Application, Platform}
import javafx.concurrent.Task
import javafx.fxml.{FXML, FXMLLoader}
import javafx.scene.{Parent, Scene}
import javafx.scene.control.{Button, Label}
import javafx.scene.layout.GridPane
import javafx.stage.Stage
import org.apache.logging.log4j.{LogManager, Logger}

import javax.swing.{BorderFactory, JButton, JDialog, JOptionPane, JPanel, JTextArea, UIManager}
import java.awt.{BorderLayout, Dialog, FlowLayout, Font}
import java.io.IOException
import java.util.{Locale, MissingResourceException, Objects, Optional, ResourceBundle}
import scala.util.{Failure, Success, Try}

class MenuController extends Application with JavaFXUIManager with LazyLogging {
  import MenuController._

  private var fxmlResource: String = "Menu.fxml"

  @FXML
  var settingsButton: Button = _

  @FXML
  var loadingLabel: Label = _

  @FXML
  var contentContainer: GridPane = _

  @FXML
  def initialize(): Unit = {
    (new Initializer).initialize(config, loadingLabel)
    logger.debug("MenuController initialized")

    if (contentContainer != null) {
      contentContainer.setVisible(false)
    }

    if (loadingLabel != null) {
      loadingLabel.setVisible(true)
      loadingLabel.setText(s"Starting HOIIVUtils ${Version.getVersion(config.getProperties)}...")
    }

    val task = new Task[Unit] {
      override def call(): Unit = {
        try {
          crazyUpdateLoadingStatus("Checking for Update...")
          (new Updater).updateCheck(Version.getVersion(config.getProperties), config.getDir)
          crazyUpdateLoadingStatus("Loading Files...")
          new ModLoader().loadMod(config.getProperties, loadingLabel)

          crazyUpdateLoadingStatus("Checking for bad files...")
          MenuController.checkForInvalidSettingsAndShowWarnings(settingsButton)
          crazyUpdateLoadingStatus("Showing Menu...")

          Platform.runLater(() => {
            if (loadingLabel != null) {
              loadingLabel.setVisible(false)
            }
            if (contentContainer != null) {
              contentContainer.setVisible(true)
            }
          })
        } catch {
          case e: Exception =>
            logger.error("Error during initialization", e)
            crazyUpdateLoadingStatus("Error loading application")
        }

        def crazyUpdateLoadingStatus(status: String): Unit = {
          updateLoadingStatus(loadingLabel, status)
        }
      }
    }

    new Thread(task).start()
  }

  override def start(stage: Stage): Unit = {
    try {
      val loader = new FXMLLoader(getClass.getResource(fxmlResource), getResourceBundle)
      val root = loader.load[Parent]()
      val scene = new Scene(root)
      if (HOIIVUtils.get("theme") == "dark")
        scene.getStylesheets.add("com/hoi4utils/ui/javafx_dark.css")
      else
        scene.getStylesheets.add("/com/hoi4utils/ui/highlight-background.css")
      val stage = new Stage()
      stage.setScene(scene)
      stage.setTitle(s"HOIIVUtils Menu ${Version.getVersion(config.getProperties)}")
      decideScreen(stage)
      stage.show()
      logger.debug(s"Stage created and shown: ${s"HOIIVUtils Menu ${Version.getVersion(config.getProperties)}"}")
    } catch {
      case e: IOException =>
        val errorMessage = s"Failed to open window\nError loading FXML: $fxmlResource"
        logger.error(s"Error loading FXML: $fxmlResource", e)
        JOptionPane.showMessageDialog(null, errorMessage, "Error", JOptionPane.ERROR_MESSAGE)
        throw new RuntimeException(errorMessage, e)
    }
  }

  def open(): Unit = start(new Stage())

  def openSettings(): Unit = {
    closeWindow(settingsButton) // closes the menu window
    new SettingsController().open()
  }
  def openLogViewer(): Unit = new LogViewerController().open()
  def openLocalizeFocusTree(): Unit = new FocusLocalizationController().open()
  def openLocalizeIdeaFile(): Unit = new IdeaLocalizationController().open()
  def openAllFocusesWindow(): Unit = new ManageFocusTreesController().open()
  def openCustomTooltip(): Unit = new CustomTooltipController().open()
  def openBuildingsByCountry(): Unit = new BuildingsByCountryController().open()
  def openInterfaceFileList(): Unit = new InterfaceFileListController().open()
  def openFocusTreeViewer(): Unit = new FocusTreeController().open()
  def openUnitComparisonView(): Unit = {
    if (!HOIIVFiles.isUnitsFolderValid) {
      logger.warn("Unit comparison view cannot open: missing base or mod units folder.")
      JOptionPane.showMessageDialog(
        null,
        "Unit folders not found. Please check your HOI4 installation or the chosen mod directory.",
        "Error",
        JOptionPane.WARNING_MESSAGE
      )
      return
    }
    new CompareUnitsController().open()
  }
  def openProvinceColors(): Unit = new ProvinceColorsController().open()
  def openMapGeneration(): Unit = new MapGenerationController().open()
  def openMapEditor(): Unit = new MapEditorController().open()
  def openParserView(): Unit = new ParserViewerController().open()
  override def setFxmlResource(fxmlResource: String): Unit = ???
  override def setTitle(title: String): Unit = ???
}

object MenuController extends LazyLogging {

  def updateLoadingStatus(loadingLabel: Label, status: String): Unit = {
    Platform.runLater(() => {
      if (loadingLabel != null) {
        val currentText = loadingLabel.getText
        loadingLabel.setText(if (currentText.isEmpty) status else s"$currentText\n$status")
      }
    })
  }

  def getResourceBundle: ResourceBundle = {
    val currentLocale = Locale.getDefault
    try {
      ResourceBundle.getBundle("menu", currentLocale)
    } catch {
      case _: MissingResourceException =>
        logger.warn(s"Could not find ResourceBundle for locale $currentLocale. Falling back to English.")
        ResourceBundle.getBundle("menu", Locale.ENGLISH)
    }
  }

  def checkForInvalidSettingsAndShowWarnings(button: Button): Boolean = {
    var hasInvalidPaths = false
    val warningMessage = new StringBuilder("The following settings need to be configured:\n\n")

    if (HOIIVUtils.get("valid.HOIIVFilePaths") == "false") {
      logger.warn("Invalid HOI IV file paths detected")
      warningMessage.append("• Hearts of Iron IV file paths\n")
      hasInvalidPaths = true
    }

    if (HOIIVUtils.get("valid.Interface") == "false") {
      logger.warn("Invalid GFX Interface file paths detected")
      warningMessage.append("• Interface file paths\n")
      hasInvalidPaths = true
    }

    if (HOIIVUtils.get("valid.State") == "false") {
      logger.warn("Invalid State paths detected")
      warningMessage.append("• State file paths\n")
      hasInvalidPaths = true
    }

    if (HOIIVUtils.get("valid.FocusTree") == "false") {
      logger.warn("Invalid Focus Tree paths detected")
      warningMessage.append("• Focus Tree file paths\n")
      hasInvalidPaths = true
    }

    // Show a single consolidated warning if any paths are invalid
    if (hasInvalidPaths) {
      warningMessage.append("\nPlease go to Settings to configure these paths.")

      // Create a custom dialog for better visual appearance
      val dialog = new JDialog()
      dialog.setTitle("Configuration Required")
      dialog.setModalityType(Dialog.ModalityType.APPLICATION_MODAL)
      dialog.setLayout(new BorderLayout())

      // Create panel with icon and message
      val panel = new JPanel(new BorderLayout(15, 15))
      panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20))

      // Add warning icon
      val iconLabel = new javax.swing.JLabel(UIManager.getIcon("OptionPane.warningIcon"))
      panel.add(iconLabel, BorderLayout.WEST)

      // Add message
      val messageArea = new JTextArea(warningMessage.toString)
      messageArea.setEditable(false)
      messageArea.setBackground(panel.getBackground)
      messageArea.setLineWrap(true)
      messageArea.setWrapStyleWord(true)
      messageArea.setFont(new Font("Dialog", Font.PLAIN, 14))
      panel.add(messageArea, BorderLayout.CENTER)

      // Add button panel
      val buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT))
      val settingsButton = new JButton("Open Settings")

      settingsButton.addActionListener(_ => {
        Platform.runLater(() => {
          try {
            (button.getScene.getWindow.asInstanceOf[Stage]).close()
          } catch {
            case ex: Exception => logger.error("Failed to close menu window", ex)
          }
        })
        Platform.runLater(() => new SettingsController().open())
        dialog.dispose()
      })

      buttonPanel.add(settingsButton)

      // Add panels to dialog
      dialog.add(panel, BorderLayout.CENTER)
      dialog.add(buttonPanel, BorderLayout.SOUTH)

      // Size and display the dialog
      dialog.pack()
      dialog.setSize(450, 300)
      dialog.setLocationRelativeTo(null)
      dialog.setVisible(true)
    }

    hasInvalidPaths
  }
}