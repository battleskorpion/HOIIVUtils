package com.hoi4utils.ui.menus

import com.hoi4utils.*
import com.hoi4utils.main.*
import com.hoi4utils.main.HOIIVUtils.config
import com.hoi4utils.ui.countries.BuildingsByCountryController2
import com.hoi4utils.ui.javafx.application.{HOIIVUtilsAbstractController2, RootWindows}
import com.hoi4utils.ui.focus.FocusTree2Controller
import com.hoi4utils.ui.focus_view.FocusTreeController
import com.hoi4utils.ui.gfx.InterfaceFileListController
import com.hoi4utils.ui.localization.*
import com.hoi4utils.ui.localization.CustomTooltipController
import com.hoi4utils.ui.map.{MapEditorController, MapGenerationController, ProvinceColorsController}
import com.hoi4utils.ui.menus.SettingsController
import com.hoi4utils.ui.parser.ParserViewerController
import com.hoi4utils.ui.units.CompareUnitsController
import com.typesafe.scalalogging.LazyLogging
import javafx.application.Platform
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.scene.control.*
import javafx.scene.layout.*
import javafx.stage.Stage
import javafx.scene.input.MouseEvent

import java.awt.{BorderLayout, Dialog, FlowLayout, Font}
import javax.swing.*
import scala.collection.mutable.ListBuffer
import scala.compiletime.uninitialized

class MenuController extends HOIIVUtilsAbstractController2 with RootWindows with LazyLogging:
  import MenuController.*
  setFxmlFile("Menu2.fxml")
  setTitle("HOIIVUtils")

  @FXML var mRoot: VBox = uninitialized
  @FXML var contentStack: StackPane = uninitialized
  @FXML var contentGrid: GridPane = uninitialized
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
  @FXML var detailContentPane: StackPane = uninitialized

  @FXML var loadingLabel: Label = uninitialized
  @FXML var mTitle: Label = uninitialized
  @FXML var mVersion: Label = uninitialized

  @FXML var errorList: BorderPane = uninitialized
  @FXML var errorListController: ErrorListController = uninitialized

  private var currentTask: javafx.concurrent.Task[Unit] = null

  private var detailPanelManager: DetailPanelManager = uninitialized

  @FXML
  def initialize(): Unit =
    val task = new javafx.concurrent.Task[Unit]:
      override def call(): Unit =
        if isCancelled then return

        val pdxLoader = new PDXLoader()
        if isCancelled then return

        pdxLoader.clearPDX()
        pdxLoader.clearLB()
        pdxLoader.closeDB()
        if isCancelled then return

        /* ! loads whole program ! */
        // If PDXLoader.load is long/blocking you should make it responsive to interruption.
        // At minimum, check cancelled both before and after the call.
        pdxLoader.load(config.getProperties, loadingLabel, () => isCancelled)
        if isCancelled then return

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
        if isCancelled then return

        if badFiles.isEmpty then
          MenuController.updateLoadingStatus(loadingLabel, "All files loaded successfully")
        else
          MenuController.updateLoadingStatus(loadingLabel, "Some files are not loaded correctly, please check the settings")
          logger.warn(s"version: ${Version.getVersion(config.getProperties)} Some files are not loaded correctly:\n${badFiles.mkString("\n")}")
          showFilesErrorDialog(badFiles, vSettings)
          blockButtons(true)
        if isCancelled then return

        HOIIVUtils.save()
        MenuController.updateLoadingStatus(loadingLabel, "Showing Menu...")

    task.setOnSucceeded: _ =>
      contentGrid.setVisible(true)
      blockButtons(false)
      loadingLabel.setVisible(false)
      vFocusTree.requestFocus()

    task.setOnCancelled: _ =>
      Platform.runLater: () =>
        if loadingLabel != null then
          loadingLabel.setText("Operation cancelled")
          loadingLabel.setVisible(false)
        if contentGrid != null then
          contentGrid.setVisible(true)
        blockButtons(false)

    /* INITIALIZATION */
    detailPanelManager = new DetailPanelManager(detailContentPane)
    currentTask = task
    contentGrid.setVisible(false)
    loadingLabel.setVisible(true)

    try new Initializer().initialize(config)
    catch case e: Exception => handleMInitError("Skipping mod loading because of unsuccessful initialization", e)

    try
      val version = Version.getVersion(config.getProperties)
      new Updater().updateCheck(version, config.getDir)
      logger.info(s"Loading mod: ${config.getProperties.getProperty("mod.path")}")
      updateLoadingStatus(loadingLabel, s"Starting HOIIVUtils $version, Loading mod: \"${config.getProperties.getProperty("mod.path")}\"")
      mVersion.setText(s"v${config.getProperties.getProperty("version")}")
      mTitle.setText(s"HOIIVUtils")
      new Thread(task).start()
    catch case e: Exception => handleMInitError("Error starting program", e)

  private def handleMInitError(msg: String, exception: Exception): Unit =
    logger.error(msg, exception)
    updateLoadingStatus(loadingLabel, msg)
    mTitle.setText(s"HOIIVUtils - Error\n$msg\n$exception")
    contentGrid.setVisible(true)
    blockButtons(true)
    loadingLabel.setVisible(false)
    vSettings.requestFocus()

  def start(stage: Stage): Unit =
    primaryStage = stage
    open()
  
  override def fxmlSetResource(): Unit = fxmlLoader.setResources(getResourceBundle("i18n.menu"))

  override def preSetup(): Unit = setupWindowControls(mRoot, mClose, mSquare, mMinimize, contentGrid)

  /* fxml menu buttons */
  def openSettings(): Unit =
    cancelTask()
    closeWindow(vSettings) // closes the menu window
    new SettingsController().open()

  // TODO 6 out of 13 views are embedded in detail panel, rest open new windows
  @FXML
  def handleFocusTreeViewerClick(event: MouseEvent): Unit =
    if event.isControlDown then new FocusTree2Controller().open()
    else detailPanelManager.switchToView("/com/hoi4utils/ui/focus/FocusTree2.fxml")

  def openFocusTreeLoc(): Unit = new FocusTreeLocalizationController().open() // TODO embed
  def openLocalizeIdeaFile(): Unit = new IdeaLocalizationController().open() // TODO embed
  def openManageFocusTrees(): Unit = new ManageFocusTreesController().open() // TODO embed

  @FXML
  def handleCustomTooltipClick(event: MouseEvent): Unit =
    if event.isControlDown then new CustomTooltipController().open()
    else detailPanelManager.switchToView("/com/hoi4utils/ui/localization/CustomTooltip.fxml")


  def openBuildingsByCountry(): Unit = new BuildingsByCountryController2().open() // TODO embed

  @FXML
  def handleGFXInterfaceFileListClick(event: MouseEvent): Unit =
    if event.isControlDown then new InterfaceFileListController().open()
    else detailPanelManager.switchToView("/com/hoi4utils/ui/gfx/InterfaceFileList.fxml")

  @FXML
  def handleUnitComparisonClick(event: MouseEvent): Unit =
    if !HOIIVFiles.isUnitsFolderValid then
      logger.warn("Unit comparison view cannot open: missing base or mod units folder.")
      JOptionPane.showMessageDialog(
        null,
        s"version: ${config.getProperties.getProperty("version")} Unit folders not found. Please check your HOI4 installation or the chosen mod directory.",
        "Error",
        JOptionPane.WARNING_MESSAGE
      )
    else
      if event.isControlDown then new CompareUnitsController().open()
      else detailPanelManager.switchToView("/com/hoi4utils/ui/units/CompareUnits.fxml")

  @FXML
  def handleProvinceColorsClick(event: MouseEvent): Unit =
    if event.isControlDown then new ProvinceColorsController().open()
    else detailPanelManager.switchToView("/com/hoi4utils/ui/map/ProvinceColors.fxml")

  def openMapGeneration(): Unit = new MapGenerationController().open() // TODO embed
  def openMapEditor(): Unit = new MapEditorController().open() // TODO embed
  def openParserView(): Unit = new ParserViewerController().open() // TODO embed

  @FXML
  def handleErrorsClick(event: MouseEvent): Unit =
    if event.isControlDown then new ErrorListController().open()
    else detailPanelManager.switchToView("/com/hoi4utils/ui/menus/ErrorList.fxml")

  private def cancelTask(): Unit =
    if currentTask != null && !currentTask.isDone then
      try currentTask.cancel(true)
      catch case ex: Exception => logger.debug("Failed to cancel task", ex)

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
    ).foreach: button =>
      if button != null then
        button.setDisable(b)

object MenuController extends LazyLogging:

  def updateLoadingStatus(loadingLabel: Label, status: String): Unit =
    Platform.runLater: () =>
      if loadingLabel != null then
        val currentText = loadingLabel.getText
        loadingLabel.setText(if currentText.isEmpty then status else s"$currentText\n$status")

  private def checkFileError(file: String): Option[String] =
    if get(s"valid.$file") == "false" then Some(s"• $file") else None

  /**
   * Shows a dialog listing the bad files and provides a button to open the settings.
   *
   * @param badFiles List of bad files to display in the dialog
   * @param button Button to close the menu window when opening settings
   */
  private def showFilesErrorDialog(badFiles: ListBuffer[String], button: Button): Unit =
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

    settingsButton.addActionListener: _ =>
      Platform.runLater: () =>
        try button.getScene.getWindow.asInstanceOf[Stage].close()
        catch case ex: Exception => logger.error("Failed to close menu window", ex)
      Platform.runLater: () =>
        new SettingsController().open()
      dialog.dispose()

    buttonPanel.add(settingsButton)

    // Add panels to dialog
    dialog.add(panel, BorderLayout.CENTER)
    dialog.add(buttonPanel, BorderLayout.SOUTH)

    // Size and display the dialog
    dialog.pack()
    dialog.setSize(450, 300)
    dialog.setLocationRelativeTo(null)
    dialog.setVisible(true)

  private def get(prop: String): String = HOIIVUtils.get(prop)