package com.hoi4utils.ui

import com.hoi4utils.HOIIVUtils
import com.hoi4utils.clausewitz.HOIIVFiles
import com.hoi4utils.ui.buildings.BuildingsByCountryController
import com.hoi4utils.ui.focus_view.FocusTreeController
import com.hoi4utils.ui.hoi4localization.{ManageFocusTreesController, FocusLocalizationController, IdeaLocalizationController}
import com.hoi4utils.ui.map.{MapEditorController, MapGenerationController}
import com.hoi4utils.ui.parser.ParserViewerController

import javafx.application.{Application, Platform}
import javafx.concurrent.Task
import javafx.fxml.{FXML, FXMLLoader}
import javafx.scene.{Parent, Scene}
import javafx.scene.control.Button
import javafx.stage.Stage

import org.apache.logging.log4j.{LogManager, Logger}

import javax.swing.{JDialog, JOptionPane, UIManager, JPanel, JTextArea, JButton, BorderFactory}
import java.awt.{Dialog, BorderLayout, Font, FlowLayout}
import java.io.IOException
import java.util.{Objects, Optional, ResourceBundle, MissingResourceException, Locale}
import scala.util.{Try, Success, Failure}

class MenuController extends Application with JavaFXUIManager {
  import MenuController._

  private var fxmlResource: String = "Menu.fxml"
  private var title: String = s"HOIIVUtils Menu ${HOIIVUtils.get("version")}"
  private var stage: Stage = _

  @FXML
  var settingsButton: Button = _

  @FXML
  def initialize(): Unit = {
    logger.debug("MenuController initialized")

    // Check for invalid folder paths and show appropriate warnings
    val task = new Task[Unit] {
      override def call(): Unit = {
        MenuController.checkForInvalidSettingsAndShowWarnings(settingsButton)
      }
    }

    new Thread(task).start()
  }

  override def start(stage: Stage): Unit = {
    try {
      val root = loadFXML()
      setupAndShowStage(root)
    } catch {
      case e: IOException => handleFXMLLoadError(e)
    }
  }

  def open(): Unit = {
    if (stage != null) {
      showExistingStage()
      return
    }

    if (fxmlResource == null) {
      handleMissingFXMLResource()
      return
    }

    start(new Stage())
  }

  private def showExistingStage(): Unit = {
    stage.show()
    logger.info(s"Stage already exists, showing: $title")
  }

  private def handleMissingFXMLResource(): Unit = {
    val errorMessage = "Failed to open window\nError: FXML resource is null."
    logger.error(errorMessage)
    JOptionPane.showMessageDialog(null, errorMessage, "Error", JOptionPane.ERROR_MESSAGE)
  }

  private def loadFXML(): Parent = {
    val launchLoader = new FXMLLoader(getClass.getResource(fxmlResource), getResourceBundle())
    try {
      launchLoader.load()
    } catch {
      case e: IOException => throw new IOException(s"Failed to load FXML: $fxmlResource", e)
    }
  }

  private def setupAndShowStage(root: Parent): Unit = {
    val scene = new Scene(root)
    addSceneStylesheets(scene)
    this.stage = createLaunchStage(scene)
    logger.debug(s"Stage created and shown: $title")
  }

  private def addSceneStylesheets(scene: Scene): Unit = {
    scene.getStylesheets.add("com/hoi4utils/ui/javafx_dark.css")

    Try {
      scene.getStylesheets.add(
        Objects.requireNonNull(
          getClass.getResource("/com/hoi4utils/ui/highlight-background.css")
        ).toExternalForm
      )
    } match {
      case Failure(_) => System.err.println("Warning: Stylesheet 'highlight-background.css' not found!")
      case _ => // Success, do nothing
    }
  }

  private def createLaunchStage(scene: Scene): Stage = {
    Option(stage).foreach(_.close())

    val launchStage = new Stage()
    launchStage.setScene(scene)
    launchStage.setTitle(title)
    decideScreen(launchStage)
    launchStage.show()

    launchStage
  }

  private def handleFXMLLoadError(e: IOException): Unit = {
    val errorMessage = s"Failed to open window\nError loading FXML: $fxmlResource"
    logger.error(s"Error loading FXML: $fxmlResource", e)
    JOptionPane.showMessageDialog(null, errorMessage, "Error", JOptionPane.ERROR_MESSAGE)
    throw new RuntimeException(errorMessage, e)
  }

  def openSettings(): Unit = {
    closeWindow(settingsButton) // closes the menu window
    openUtilsWindow(new SettingsController())
  }

  def openLogViewer(): Unit = {
    openUtilsWindow(new LogViewerController())
  }

  def openLocalizeFocusTree(): Unit = {
    openUtilsWindow(new FocusLocalizationController())
  }

  def openLocalizeIdeaFile(): Unit = {
    openUtilsWindow(new IdeaLocalizationController())
  }

  def openAllFocusesWindow(): Unit = {
    openUtilsWindow(new ManageFocusTreesController())
  }

  def openCustomTooltip(): Unit = {
    openUtilsWindow(new CustomTooltipController())
  }

  def openBuildingsByCountry(): Unit = {
    openUtilsWindow(new BuildingsByCountryController())
  }

  def openInterfaceFileList(): Unit = {
    openUtilsWindow(new InterfaceFileListController())
  }

  def openFocusTreeViewer(): Unit = {
    openUtilsWindow(new FocusTreeController())
  }

  def openUnitComparisonView(): Unit = {
    if (!HOIIVFiles.isUnitsFolderValid()) {
      logger.warn("Unit comparison view cannot open: missing base or mod units folder.")
      JOptionPane.showMessageDialog(
        null,
        "Unit folders not found. Please check your HOI4 installation or the chosen mod directory.",
        "Error",
        JOptionPane.WARNING_MESSAGE
      )
      return
    }
    openUtilsWindow(new CompareUnitsController())
  }

  def openProvinceColors(): Unit = {
    openUtilsWindow(new ProvinceColorsController())
  }

  def openMapGeneration(): Unit = {
    openUtilsWindow(new MapGenerationController())
  }

  def openMapEditor(): Unit = {
    openUtilsWindow(new MapEditorController())
  }

  def openParserView(): Unit = {
    openUtilsWindow(new ParserViewerController())
  }

  private def openUtilsWindow(utilsWindow: HOIIVUtilsAbstractController): Unit = {
    utilsWindow.open()
  }

  override def setFxmlResource(fxmlResource: String): Unit = {
    this.fxmlResource = fxmlResource
  }

  override def setTitle(title: String): Unit = {
    this.title = title
  }
}

object MenuController {
  val logger: Logger = LogManager.getLogger(classOf[MenuController])

  def getResourceBundle(): ResourceBundle = {
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