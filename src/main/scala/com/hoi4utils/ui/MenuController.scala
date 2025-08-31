package com.hoi4utils.ui

import com.hoi4utils.*
import com.hoi4utils.HOIIVUtils.config
import com.hoi4utils.ui.buildings.BuildingsByCountryController
import com.hoi4utils.ui.focus_view.FocusTreeController
import com.hoi4utils.ui.hoi4localization.{FocusLocalizationController, IdeaLocalizationController, ManageFocusTreesController}
import com.hoi4utils.ui.map.{MapEditorController, MapGenerationController}
import com.hoi4utils.ui.parser.ParserViewerController
import com.typesafe.scalalogging.LazyLogging
import javafx.application.{Application, Platform}
import javafx.fxml.{FXML, FXMLLoader}
import javafx.scene.control.{Button, Label}
import javafx.scene.layout.GridPane
import javafx.scene.{Parent, Scene}
import javafx.stage.Stage

import java.awt.{BorderLayout, Dialog, FlowLayout, Font}
import java.util.{Locale, MissingResourceException, ResourceBundle}
import javax.swing.*
import scala.collection.mutable.ListBuffer
import scala.compiletime.uninitialized

class MenuController extends Application with JavaFXUIManager with LazyLogging:
  import MenuController.*

  private var fxmlResource: String = "Menu.fxml"

  @FXML var settingsButton: Button = uninitialized
  @FXML var loadingLabel: Label = uninitialized
  @FXML var contentContainer: GridPane = uninitialized

  var primaryStage: Stage = uninitialized

  @FXML
  def initialize(): Unit = {

    logger.debug("Menu init 1")
    contentContainer.setVisible(false)
    loadingLabel.setVisible(true)

    var initFailed: Boolean = false
    logger.debug("Menu init 2")

    initFailed = new Initializer().initialize(config)
    logger.debug("Menu init 3")

    val hProperties = config.getProperties
    val version = Version.getVersion(hProperties)

    new Updater().updateCheck(version, config.getDir)
    logger.info(s"Starting HOIIVUtils $version, Loading mod: ${hProperties.getProperty("mod.path")}")
    updateLoadingStatus(loadingLabel, s"Starting HOIIVUtils $version, Loading mod: \"${hProperties.getProperty("mod.path")}\"")


    val task = new javafx.concurrent.Task[Unit] {
      override def call(): Unit = {
        try {
          val pdxLoader = new PDXLoader()
          pdxLoader.clearPDX()
          pdxLoader.clearLB()
          pdxLoader.closeDB()
          if (initFailed) {
            logger.info("Skipping mod loading because of unsuccessful initialization")
            updateLoadingStatus("Skipping loading!!!")
          } else {
            pdxLoader.load(hProperties, loadingLabel)
            updateLoadingStatus("Checking for bad files...")
            val badFiles = checkBadFiles()
            if (badFiles.isEmpty) {
              updateLoadingStatus("All files loaded successfully")
            } else {
              updateLoadingStatus("Some files are not loaded correctly, please check the settings")
              logger.warn(s"version: ${Version.getVersion(config.getProperties)} Some files are not loaded correctly:\n${badFiles.mkString("\n")}")
              showFilesErrorDialog(badFiles, settingsButton)
            }
          }
          HOIIVUtils.save()
          updateLoadingStatus("Showing Menu...")
        } catch {
          case e: Exception =>
            updateLoadingStatus("Error loading application")
            logger.error(s"${Version.getVersion(config.getProperties)} Error during initialization", e)
            Platform.runLater(() => handleFatalCrash())
        }
      }

      private def updateLoadingStatus(status: String): Unit =
        MenuController.updateLoadingStatus(loadingLabel, status)

      private def checkBadFiles(): ListBuffer[String] =
        ListBuffer(
          "localization",
          "HOIIVFilePaths",
          "Interface",
          "State",
//          "Country", // TODO: re-enable when Country and CountryTag have checks for bad files
//          "CountryTag",
          "FocusTree",
          "IdeaFile",
          "ResourcesFile$"
        ).flatMap(MenuController.checkFileError)

      private def handleFatalCrash(): Unit =
        try {
          loadingLabel.setVisible(false)
          contentContainer.setVisible(true)
        } catch {
          case _: Exception =>
            logger.error(s"version ${Version.getVersion(config.getProperties)} fatal crash: can't show menu buttons, please go to our discord")
            JOptionPane.showMessageDialog(
              null,
              s"Version: ${Version.getVersion(config.getProperties)}\nFatal crash: can't show menu buttons, please go to our Discord.",
              "Fatal Error",
              JOptionPane.ERROR_MESSAGE
            )
            System.exit(1)
        }
    }

    task.setOnSucceeded(_ => {
      contentContainer.setVisible(true)
      loadingLabel.setVisible(false)
      logger.info(s"Loading completed successfully")
    })

    new Thread(task).start()
  }

  override def start(stage: Stage): Unit = {
    primaryStage = stage
    openM()

    // Add a listener to handle the "X" button click
    primaryStage.setOnCloseRequest(_ => {
      JOptionPane.getRootFrame.dispose()
      System.exit(0)
    })
  }

  private def getResourceBundleM = {
    val resourceBundle: ResourceBundle =
      //    val currentLocale = new Locale("tr", "TR") // Turkish locale
      val currentLocale = Locale.getDefault
      try {
        val bundle = ResourceBundle.getBundle("i18n.menu", currentLocale)
        bundle
      } catch {
        case _: MissingResourceException =>
          logger.warn(s"Could not find ResourceBundle for locale $currentLocale. Falling back to English.")
          val fallbackBundle = ResourceBundle.getBundle("i18n.menu", Locale.US)
          logger.info(s"Fallback ResourceBundle loaded: ${fallbackBundle.getLocale}")
          fallbackBundle
      }
    if resourceBundle == null then
      logger.error("ResourceBundle is null, cannot load FXML.")
      throw new RuntimeException("ResourceBundle is null, cannot load FXML.")
    logger.info(s"Language loaded: ${resourceBundle.getLocale}")
    resourceBundle
  }

  /**
   * Opens the stage with the specified FXMLLoader.
   * This method is used internally to set up the scene and stage.
   *
   */
  private def openM(): Unit = {
    try
      logger.debug(s"Loading FXML resource0: $fxmlResource")
      val fxml = new FXMLLoader(getClass.getResource(fxmlResource), getResourceBundleM)
      logger.debug(s"Loading FXML resource1: $fxmlResource")
      val root = fxml.load[Parent]()
      logger.debug(s"Loading FXML resource2: $fxmlResource")
      val scene = new Scene(root)
      logger.debug(s"Loading FXML resource3: $fxmlResource")
      get("theme") match
        case "dark" => scene.getStylesheets.add("com/hoi4utils/ui/javafx_dark.css")
        case _ => scene.getStylesheets.add("/com/hoi4utils/ui/highlight-background.css")
      logger.debug(s"Loading FXML resource4: $fxmlResource")
      primaryStage.setScene(scene)
      logger.debug(s"Loading FXML resource5: $fxmlResource")
      primaryStage.setTitle(s"HOIIVUtils Menu ") // ${Version.getVersion(config.getProperties)}
      logger.debug(s"Loading FXML resource6: $fxmlResource")
      decideScreen(primaryStage)
      logger.debug(s"Loading FXML resource7: $fxmlResource")
      primaryStage.show()
      logger.debug(s"Loading FXML resource5: $fxmlResource")
    catch
      case e: Exception =>
        logger.error(s"version: ${Version.getVersion(config.getProperties)} Failed to load FXML resource: $fxmlResource", e)
        JOptionPane.showMessageDialog(
          null,
          s"version: ${Version.getVersion(config.getProperties)} Failed to load FXML resource: $fxmlResource\nPlease go to our Discord for help.",
          "Error",
          JOptionPane.ERROR_MESSAGE
        )
        System.exit(1)
    ()
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
        s"version: ${config.getProperties.getProperty("version")} Unit folders not found. Please check your HOI4 installation or the chosen mod directory.",
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
  def openLB(): Unit = new LBReaderController().open()
  override def setFxmlResource(fxmlResource: String): Unit =
    this.fxmlResource = fxmlResource
  override def setTitle(title: String): Unit =
    primaryStage.setTitle(title)


object MenuController extends LazyLogging:

  def updateLoadingStatus(loadingLabel: Label, status: String): Unit = {
    Platform.runLater(() => {
      if (loadingLabel != null) {
        val currentText = loadingLabel.getText
        loadingLabel.setText(if (currentText.isEmpty) status else s"$currentText\n$status")
      }
    })
  }

  private def checkFileError(file: String): Option[String] = {
    if get(s"valid.$file") == "false" then Some(s"• $file") else None
  }

  /**
   * Shows a dialog listing the bad files and provides a button to open the settings.
   *
   * @param badFiles List of bad files to display in the dialog
   * @param button Button to close the menu window when opening settings
   */
  private def showFilesErrorDialog(badFiles: ListBuffer[String], button: Button): Unit = {
    val warningMessageBuffer = new StringBuilder("")
    warningMessageBuffer.append(badFiles.mkString(
      "The following settings need to be configured:\n\n",
      " directory(ies)\n",
      " directory(ies)\n\nPlease go to Settings to configure these paths."
    ))
    warningMessageBuffer.append(get("hoi4.path.status") match
      case "failed" => "\n• HOI4 installation path not found (REQUIRED)"
    )

    // Create a custom dialog for better visual appearance
    val dialog = new JDialog()
    dialog.setTitle("Configuration Required")
    dialog.setModalityType(Dialog.ModalityType.APPLICATION_MODAL)
    dialog.setLayout(new BorderLayout())

    // Create panel with icon and message
    val panel = new JPanel(new BorderLayout(15, 15))
    panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20))

    // Add warning icon
    val iconLabel = new JLabel(UIManager.getIcon("OptionPane.warningIcon"))
    panel.add(iconLabel, BorderLayout.WEST)

    // Add message
    val messageArea = new JTextArea(warningMessageBuffer.toString)
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
        try button.getScene.getWindow.asInstanceOf[Stage].close()
        catch case ex: Exception => logger.error("Failed to close menu window", ex)
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

  def get(prop: String): String = {
    HOIIVUtils.get(prop)
  }