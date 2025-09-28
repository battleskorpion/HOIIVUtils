package com.hoi4utils.ui

import com.hoi4utils.*
import com.hoi4utils.HOIIVUtils.config
import com.hoi4utils.ui.buildings.BuildingsByCountryController
import com.hoi4utils.ui.focus_view.FocusTreeController
import com.hoi4utils.ui.hoi4localization.*
import com.hoi4utils.ui.map.{MapEditorController, MapGenerationController}
import com.hoi4utils.ui.parser.ParserViewerController
import com.typesafe.scalalogging.LazyLogging
import javafx.application.{Application, Platform}
import javafx.event.EventHandler
import javafx.fxml.{FXML, FXMLLoader}
import javafx.scene.control.{Button, Label}
import javafx.scene.image.Image
import javafx.scene.layout.GridPane
import javafx.scene.{Parent, Scene}
import javafx.stage.{Stage, StageStyle}

import java.awt.{BorderLayout, Dialog, FlowLayout, Font}
import scala.util.boundary
import java.io.IOException
import java.util.{Locale, MissingResourceException, ResourceBundle}
import javax.swing.{BorderFactory, JButton, JDialog, JLabel, JOptionPane, JPanel, JTextArea, UIManager}
import scala.collection.mutable.ListBuffer
import scala.compiletime.uninitialized

class MenuController extends Application with JavaFXUIManager with LazyLogging:
  import MenuController.*

  private var fxmlResource: String = "Menu.fxml"

  @FXML var contentContainer: GridPane = uninitialized
  private var xOffset: Double = 0
  private var yOffset: Double = 0

  /* buttons */
  @FXML var mClose: Button = uninitialized
  @FXML var mSquare: Button = uninitialized
  @FXML var mMinimize: Button = uninitialized

  @FXML var vSettings: Button = uninitialized
  @FXML var vLogs: Button = uninitialized
  @FXML var vFocusTree: Button = uninitialized
  @FXML var vFocusTreeLoc: Button = uninitialized
  @FXML var vManageFocusTrees: Button = uninitialized
  @FXML var vIdeasLoc: Button = uninitialized
  @FXML var vCustomTooltipLoc: Button = uninitialized
  @FXML var vBuildingsByCountry: Button = uninitialized
  @FXML var vGFX: Button = uninitialized
  @FXML var vUnitComparison: Button = uninitialized
  @FXML var vProvinceColors: Button = uninitialized
  @FXML var vMapGeneration: Button = uninitialized
  @FXML var vMapEditor: Button = uninitialized
  @FXML var vParserView: Button = uninitialized
  @FXML var vErrors: Button = uninitialized

  @FXML var loadingLabel: Label = uninitialized
  @FXML var mTitle: Label = uninitialized
  @FXML var mVersion: Label = uninitialized

  var primaryStage: Stage = uninitialized

  @FXML
  def initialize(): Unit = {
    val task = new javafx.concurrent.Task[Unit] {
      override def call(): Unit = {
        loadProgram()
      }

      private def loadProgram(): Unit = {
        val pdxLoader = new PDXLoader()

        pdxLoader.clearPDX()
        pdxLoader.clearLB()
        pdxLoader.closeDB()

        /* ! loads whole program ! */
        pdxLoader.load(config.getProperties, loadingLabel)

        MenuController.updateLoadingStatus(loadingLabel, "Checking for bad files...")
        val badFiles = ListBuffer(
          "localization",
          "HOIIVFilePaths",
          "Interface",
          "State",
          "Country",
          "CountryTag",
          "FocusTree",
          "IdeaFile",
          "ResourcesFile$"
        ).flatMap(MenuController.checkFileError)
        if (badFiles.isEmpty) {
          MenuController.updateLoadingStatus(loadingLabel, "All files loaded successfully")
        } else {
          MenuController.updateLoadingStatus(loadingLabel, "Some files are not loaded correctly, please check the settings")
          logger.warn(s"version: ${Version.getVersion(config.getProperties)} Some files are not loaded correctly:\n${badFiles.mkString("\n")}")
          showFilesErrorDialog(badFiles, vSettings)
          blockButtons(true)
        }

        HOIIVUtils.save()
        MenuController.updateLoadingStatus(loadingLabel, "Showing Menu...")
      }
    }

    task.setOnSucceeded(_ => {
      contentContainer.setVisible(true)
      blockButtons(false)
      loadingLabel.setVisible(false)
      vFocusTree.requestFocus()
    })

    /* INITIALIZATION */
    contentContainer.setVisible(false)
    loadingLabel.setVisible(true)

    try
      new Initializer().initialize(config)
    catch case e: Exception => handleMInitError("Skipping mod loading because of unsuccessful initialization", e)

    try
      val version = Version.getVersion(config.getProperties)
      new Updater().updateCheck(version, config.getDir)
      logger.info(s"Loading mod: ${config.getProperties.getProperty("mod.path")}")
      updateLoadingStatus(loadingLabel, s"Starting HOIIVUtils $version, Loading mod: \"${config.getProperties.getProperty("mod.path")}\"")
      mVersion.setText(s"v${config.getProperties.getProperty("version")}")
      mTitle.setText(s"HOIIVUtils Menu")
      new Thread(task).start()
    catch case e: Exception => handleMInitError("Error starting program", e)
  }

  private def handleMInitError(msg: String, exception: Exception): Unit = {
    logger.error(msg, exception)
    updateLoadingStatus(loadingLabel, msg)
    mTitle.setText(s"HOIIVUtils - Error\n$msg\n$exception")
    contentContainer.setVisible(true)
    blockButtons(true)
    loadingLabel.setVisible(false)
    vSettings.requestFocus()
  }

  // Setup window controls AFTER primaryStage is available
  private def setupWindowControls(): Unit = {

    // Verify all components are available
    if contentContainer == null then
      logger.error("contentContainer is null - FXML injection failed!")
      return ()

    if primaryStage == null then
      logger.error("primaryStage is null - called too early!")
      return ()

    // Setup window dragging
    contentContainer.setOnMousePressed { event =>
      if event != null then
        xOffset = event.getSceneX
        yOffset = event.getSceneY
    }

    contentContainer.setOnMouseDragged { event =>
      if event != null && primaryStage != null then
        val newX = event.getScreenX - xOffset
        val newY = event.getScreenY - yOffset
        primaryStage.setX(newX)
        primaryStage.setY(newY)
    }

    // Setup window control buttons
    if mClose != null then
      mClose.setOnAction(_ => {
        try
          Option(JOptionPane.getRootFrame).foreach(_.dispose())
          System.exit(0)
        catch
          case e: Exception =>
            logger.error("Error during application shutdown", e)
            System.exit(1)
      })
    else
      logger.warn("mClose button is null!")

    if mSquare != null then
      mSquare.setOnAction(_ => {
        try
          if primaryStage != null then
            primaryStage.setMaximized(!primaryStage.isMaximized)
        catch
          case e: Exception =>
            logger.error("Error toggling window maximized state", e)
      })
    else
      logger.warn("mSquare button is null!")

    if mMinimize != null then
      mMinimize.setOnAction(_ => {
        try
          if primaryStage != null then
            primaryStage.setIconified(true)
        catch
          case e: Exception =>
            logger.error("Error minimizing window", e)
      })
    else
      logger.warn("mMinimize button is null!")
  }

  override def start(stage: Stage): Unit = {
    primaryStage = stage
    primaryStage.initStyle(StageStyle.UNDECORATED)

    primaryStage.getIcons.addAll(
      new Image(getClass.getResourceAsStream("/icons/settings-icon-gray-gear16.png")),
      new Image(getClass.getResourceAsStream("/icons/settings-icon-gray-gear32.png")),
      new Image(getClass.getResourceAsStream("/icons/settings-icon-gray-gear48.png")),
      new Image(getClass.getResourceAsStream("/icons/settings-icon-gray-gear64.png")),
      new Image(getClass.getResourceAsStream("/icons/settings-icon-gray-gear128.png"))
    )

    try
      // Validate FXML resource exists
      Option(getClass.getResource(fxmlResource)) match
        case None =>
          throw new IllegalArgumentException(s"FXML resource not found: $fxmlResource")
        case Some(resource) =>

      val fxml = new FXMLLoader(getClass.getResource(fxmlResource), getResourceBundleM)
      // CRITICAL: Set this instance as the controller so FXML injection works
      fxml.setController(this)
      val root = fxml.load[Parent]()

      if root == null then
        throw new IllegalStateException(s"Failed to load FXML root from: $fxmlResource")

      val scene = new Scene(root)

      // Apply theme with fallback
      val theme = Option(get("theme")).getOrElse("light")
      val cssPath = theme match
        case "dark" => "/com/hoi4utils/ui/javafx_dark.css"
        case _ => "/com/hoi4utils/ui/highlight-background.css"

      // Validate CSS resource exists before adding
      Option(getClass.getResource(cssPath)) match
        case Some(_) =>
          scene.getStylesheets.add(cssPath)
        case None =>
          logger.warn(s"CSS file not found: $cssPath, continuing without theme")

      primaryStage.setScene(scene)
      primaryStage.setTitle("HOIIVUtils")

      // Configure screen positioning
      decideScreen(primaryStage)

      // NOW setup window controls - both primaryStage and FXML fields are available
      setupWindowControls()

      primaryStage.show()

    catch
      case e: IOException =>
        val errorMsg = s"IO Error loading FXML resource: $fxmlResource"
        logger.error(s"version: ${Version.getVersion(config.getProperties)} $errorMsg", e)
        JOptionPane.showMessageDialog(
          null,
          s"version: ${Version.getVersion(config.getProperties)} $errorMsg\nCheck if the file exists and is accessible.\nPlease go to our Discord for help.",
          "File Loading Error",
          JOptionPane.ERROR_MESSAGE
        )
        System.exit(1)

      case e: IllegalArgumentException =>
        val errorMsg = s"Invalid FXML resource: $fxmlResource"
        logger.error(s"version: ${Version.getVersion(config.getProperties)} $errorMsg", e)
        JOptionPane.showMessageDialog(
          null,
          s"version: ${Version.getVersion(config.getProperties)} $errorMsg\nThe specified resource path is invalid.\nPlease go to our Discord for help.",
          "Configuration Error",
          JOptionPane.ERROR_MESSAGE
        )
        System.exit(1)

      case e: IllegalStateException =>
        val errorMsg = s"Failed to initialize UI components from: $fxmlResource"
        logger.error(s"version: ${Version.getVersion(config.getProperties)} $errorMsg", e)
        JOptionPane.showMessageDialog(
          null,
          s"version: ${Version.getVersion(config.getProperties)} $errorMsg\nThe FXML file may be corrupted or invalid.\nPlease go to our Discord for help.",
          "UI Initialization Error",
          JOptionPane.ERROR_MESSAGE
        )
        System.exit(1)

      case e: Exception =>
        val errorMsg = s"Unexpected error during application startup"
        logger.error(s"version: ${Version.getVersion(config.getProperties)} $errorMsg with FXML: $fxmlResource", e)
        JOptionPane.showMessageDialog(
          null,
          s"version: ${Version.getVersion(config.getProperties)} $errorMsg\nError: ${e.getClass.getSimpleName}: ${Option(e.getMessage).getOrElse("Unknown error")}\nPlease go to our Discord for help.",
          "Startup Error",
          JOptionPane.ERROR_MESSAGE
        )
        System.exit(1)
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
          logger.error(s"Could not find ResourceBundle for locale $currentLocale. Falling back to English.")
          val fallbackBundle = ResourceBundle.getBundle("i18n.menu", Locale.US)
          logger.error(s"Fallback ResourceBundle loaded: ${fallbackBundle.getLocale}")
          fallbackBundle
      }
    if resourceBundle == null then
      logger.error("ResourceBundle is null, cannot load FXML.")
      throw new RuntimeException("ResourceBundle is null, cannot load FXML.")
    resourceBundle
  }

  def open(): Unit = start(new Stage())

  def openSettings(): Unit = {
    closeWindow(vSettings) // closes the menu window
    new SettingsController().open()
  }
  def openFocusTreeViewer(): Unit = new FocusTreeController().open()
  def openFocusTreeLoc(): Unit = new FocusTreeLocalizationController().open()
  def openLocalizeIdeaFile(): Unit = new IdeaLocalizationController().open()
  def openManageFocusTrees(): Unit = new ManageFocusTreesController().open()
  def openCustomTooltip(): Unit = new CustomTooltipController().open()
  def openBuildingsByCountry(): Unit = new BuildingsByCountryController().open()
  def openInterfaceFileList(): Unit = new InterfaceFileListController().open()
  def openUnitComparisonView(): Unit = {
    if (!HOIIVFiles.isUnitsFolderValid) {
      logger.warn("Unit comparison view cannot open: missing base or mod units folder.")
      JOptionPane.showMessageDialog(
        null,
        s"version: ${config.getProperties.getProperty("version")} Unit folders not found. Please check your HOI4 installation or the chosen mod directory.",
        "Error",
        JOptionPane.WARNING_MESSAGE
      )
      return ()
    }
    new CompareUnitsController().open()
  }
  def openProvinceColors(): Unit = new ProvinceColorsController().open()
  def openMapGeneration(): Unit = new MapGenerationController().open()
  def openMapEditor(): Unit = new MapEditorController().open()
  def openParserView(): Unit = new ParserViewerController().open()
  def openErrorsW(): Unit = new ErrorListController().open()
  override def setFxmlResource(fxmlResource: String): Unit =
    this.fxmlResource = fxmlResource
  override def setTitle(title: String): Unit =
    primaryStage.setTitle(title)

  // TODO: @skorp remove buttons you think don't need any files (hoi4, mod, valid, etc) cuz it is trigger happy and will disable on any at startup issue's
  private def blockButtons(b: Boolean): Unit =
    List(
      vFocusTree,
      vFocusTreeLoc,
      vManageFocusTrees,
      vIdeasLoc,
      vCustomTooltipLoc,
      vBuildingsByCountry,
      vGFX,
      vUnitComparison,
      vMapEditor,
      vParserView
    ).foreach { button =>
      if button != null then
        button.setDisable(b)
    }

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