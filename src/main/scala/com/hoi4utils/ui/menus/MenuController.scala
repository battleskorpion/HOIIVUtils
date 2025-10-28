package com.hoi4utils.ui.menus

import com.hoi4utils.*
import com.hoi4utils.main.*
import com.hoi4utils.main.HOIIVUtils.config
import com.hoi4utils.ui.countries.BuildingsByCountryController2
import com.hoi4utils.ui.javafx.application.{HOIIVUtilsAbstractController, HOIIVUtilsAbstractController2, RootWindows}
import com.hoi4utils.ui.focus.FocusTree2Controller
import com.hoi4utils.ui.focus_view.FocusTreeController
import com.hoi4utils.ui.gfx.InterfaceController
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
import javafx.animation.{Animation, Timeline, KeyFrame}
import javafx.util.Duration

import java.awt.{BorderLayout, Dialog, FlowLayout, Font}
import javax.swing.*
import scala.collection.mutable.{ListBuffer, LinkedHashMap}
import scala.compiletime.uninitialized

class MenuController extends HOIIVUtilsAbstractController2 with RootWindows with LazyLogging:
  import MenuController.*
  setFxmlFile("/com/hoi4utils/ui/menus/Menu2.fxml")
  setTitle("HOIIVUtils")
  
  private val focusTreeViewer       = "/com/hoi4utils/ui/focus/FocusTree2.fxml"
  private val focusTreeLocalization = "/com/hoi4utils/ui/localization/FocusTreeLocalization.fxml" // todo scala, redesign
  private val ideaLocalization      = "/com/hoi4utils/ui/localization/IdeaLocalization.fxml" // todo scala, redesign
  private val manageFocusTrees      = "/com/hoi4utils/ui/localization/ManageFocusTrees.fxml"
  private val customTooltip         = "/com/hoi4utils/ui/localization/CustomTooltip.fxml"
  private val buildingsByCountry    = "/com/hoi4utils/ui/countries/BuildingsByCountry.fxml"
  private val interface             = "/com/hoi4utils/ui/gfx/Interface.fxml"
  private val compareUnits          = "/com/hoi4utils/ui/units/CompareUnits.fxml"
  private val provinceColors        = "/com/hoi4utils/ui/map/ProvinceColors.fxml"
  private val mapGeneration         = "/com/hoi4utils/ui/map/MapGeneration.fxml" // todo scala, redesign
  private val mapEditor             = "/com/hoi4utils/ui/map/MapEditor.fxml" // todo scala, redesign
  private val parserViewer          = "/com/hoi4utils/ui/parser/ParserViewer.fxml" // todo scala, redesign WIP
  private val errors                = "/com/hoi4utils/ui/menus/ErrorList.fxml"

  @FXML var mRoot: AnchorPane = uninitialized
  @FXML var mVBox: VBox = uninitialized
  @FXML var contentStack: StackPane = uninitialized
  @FXML var contentGrid: GridPane = uninitialized
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
  @FXML var timer: Label = uninitialized

  @FXML var errorList: BorderPane = uninitialized
  @FXML var errorListController: ErrorListController = uninitialized

  private var currentTask: javafx.concurrent.Task[Unit] = null

  private var detailPanelManager: DetailPanelManager = uninitialized
  
  @FXML
  def initialize(): Unit =
    val initStartTime = System.nanoTime()
    var modLoadStartTime: Long = 0
    var modLoadEndTime: Long = 0
    var initEndTime: Long = 0

    @volatile var isModLoading = false
    @volatile var isComplete = false

    val componentTimes = LinkedHashMap[String, Double]()
    val componentOrder = List(
      "ModifierDatabase", "EffectDatabase", "Paths", "Localization",
      "Interface", "Resources", "State", "Country", "CountryTag", "Ideas", "FocusTrees"
    )
    @volatile var currentComponent: String = ""
    var currentComponentStartTime: Long = 0

    def buildTimerDisplay(currentTime: Long): String =
      val initTime = if initEndTime > 0 then (initEndTime - initStartTime) / 1_000_000_000.0
                     else (currentTime - initStartTime) / 1_000_000_000.0

      val modLoadTime =
        if isModLoading && modLoadEndTime == 0 then (currentTime - modLoadStartTime) / 1_000_000_000.0
        else if modLoadEndTime > 0 then (modLoadEndTime - modLoadStartTime) / 1_000_000_000.0
        else 0.0

      val totalTime = (currentTime - initStartTime) / 1_000_000_000.0

      val lines = new StringBuilder()
      lines.append(f"Init: $initTime%.2fs\n")

      // Show individual component times if mod loading has started
      if modLoadStartTime > 0 then
        componentOrder.foreach { component =>
          componentTimes.get(component) match
            case Some(time) =>
              // Component completed - show final time
              lines.append(f"  $component: $time%.2fs\n")
            case None =>
              // Component not completed - show live timer if it's currently loading
              if component == currentComponent && currentComponentStartTime > 0 then
                val liveTime = (currentTime - currentComponentStartTime) / 1_000_000_000.0
                lines.append(f"  $component: $liveTime%.2fs\n")
        }
        lines.append(f"Mod Load: $modLoadTime%.2fs\n")

      lines.append(f"Total: $totalTime%.2fs")
      lines.toString

    def buildFinalTimerDisplay(
        initTime: Double,
        modLoadTime: Double,
        totalTime: Double,
        components: LinkedHashMap[String, Double],
        order: List[String]
    ): String =
      val lines = new StringBuilder()
      lines.append(f"Init: $initTime%.2fs\n")
      order.foreach { component =>
        components.get(component).foreach { time =>
          lines.append(f"  $component: $time%.2fs\n")
        }
      }
      lines.append(f"Mod Load: $modLoadTime%.2fs\n")
      lines.append(f"Total: $totalTime%.2fs")
      lines.toString

    // ===== Real-time Timer Updates =====
    val timerTimeline = new Timeline(
      new KeyFrame(Duration.millis(50), _ => {
        if timer != null && !isComplete then
          val display = buildTimerDisplay(System.nanoTime())
          Platform.runLater(() => timer.setText(display))
      })
    )
    timerTimeline.setCycleCount(Animation.INDEFINITE)
    timerTimeline.play()

    val task = new javafx.concurrent.Task[Unit]:
      override def call(): Unit =
        if isCancelled then return

        val pdxLoader = new PDXLoader()
        if isCancelled then return

        pdxLoader.clearPDX()
        pdxLoader.clearLB()
        pdxLoader.closeDB()
        if isCancelled then return

        initEndTime = System.nanoTime()
        modLoadStartTime = System.nanoTime()
        isModLoading = true

        def onComponentStart(componentName: String): Unit =
          currentComponent = componentName
          currentComponentStartTime = System.nanoTime()

        // Callback: Called when a component finishes loading
        def onComponentComplete(componentName: String, time: Double): Unit =
          componentTimes.synchronized {
            componentTimes(componentName) = time
          }
          currentComponent = ""
          currentComponentStartTime = 0
        
        pdxLoader.load(
          config.getProperties,
          loadingLabel,
          () => isCancelled,
          onComponentComplete,
          onComponentStart
        )

        modLoadEndTime = System.nanoTime()
        isModLoading = false
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
      isComplete = true
      timerTimeline.stop()
      
      val totalTime = (System.nanoTime() - initStartTime) / 1_000_000_000.0
      val initTime = (initEndTime - initStartTime) / 1_000_000_000.0
      val modLoadTime = if modLoadEndTime > 0 then (modLoadEndTime - modLoadStartTime) / 1_000_000_000.0 else 0.0
      
      val finalDisplay = buildFinalTimerDisplay(initTime, modLoadTime, totalTime, componentTimes, componentOrder)
      Platform.runLater(() => timer.setText(finalDisplay))
      
      val componentLog = componentTimes.map { case (name, time) => f"$name: $time%.2fs" }.mkString(", ")
      logger.info(f"Loading complete - Total: $totalTime%.2fs, Init: $initTime%.2fs, Mod load: $modLoadTime%.2fs, Components: [$componentLog]")

      contentGrid.setVisible(true)
      blockButtons(false)
      loadingLabel.setVisible(false)
      vFocusTree.requestFocus()

    task.setOnCancelled: _ =>
      isComplete = true
      timerTimeline.stop()
      val cancelTime = (System.nanoTime() - initStartTime) / 1_000_000_000.0
      Platform.runLater: () =>
        timer.setText(f"CANCELLED\nTotal: $cancelTime%.2fs")
        if loadingLabel != null then
          loadingLabel.setText("Operation cancelled")
          loadingLabel.setVisible(false)
        if contentGrid != null then
          contentGrid.setVisible(true)
        blockButtons(false)

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

  override def preSetup(): Unit = setupWindowControls(mVBox, contentGrid)

  /* fxml menu buttons */
  def openSettings(): Unit =
    cancelTask()
    closeWindow(vSettings) // closes the menu window
    new SettingsController().open()

  @FXML def handleFocusTreeViewerClick(event: MouseEvent): Unit = openWindow(event, classOf[FocusTree2Controller], focusTreeViewer)
  @FXML def handleFocusTreeLocalizationClick(event: MouseEvent): Unit = openWindow(event, classOf[FocusTreeLocalizationController], focusTreeLocalization)
  @FXML def handleIdeaLocalizationClick(event: MouseEvent): Unit = openWindow(event, classOf[IdeaLocalizationController], ideaLocalization)
  @FXML def handleManageFocusTreesClick(event: MouseEvent): Unit = openWindow(event, classOf[ManageFocusTreesController], manageFocusTrees)
  @FXML def handleCustomTooltipClick(event: MouseEvent): Unit = openWindow(event, classOf[CustomTooltipController], customTooltip)
  @FXML def handleBuildingsByCountryClick(event: MouseEvent): Unit = openWindow(event, classOf[BuildingsByCountryController2], buildingsByCountry)
  @FXML def handleInterfaceClick(event: MouseEvent): Unit = openWindow(event, classOf[InterfaceController], interface)

  @FXML def handleUnitComparisonClick(event: MouseEvent): Unit =
    if HOIIVFiles.isUnitsFolderValid then
      openWindow(event, classOf[CompareUnitsController], compareUnits)
    else handleInvalidUnitsFolder()

  @FXML def handleProvinceColorsClick(event: MouseEvent): Unit = openWindow(event, classOf[ProvinceColorsController], provinceColors)
  @FXML def handleMapGenerationClick(event: MouseEvent): Unit = openWindow(event, classOf[MapGenerationController], mapGeneration)
  @FXML def handleMapEditorClick(event: MouseEvent): Unit = openWindow(event, classOf[MapEditorController], mapEditor)
  @FXML def handleParserViewerClick(event: MouseEvent): Unit = openWindow(event, classOf[ParserViewerController], parserViewer)
  @FXML def handleErrorsClick(event: MouseEvent): Unit = openWindow(event, classOf[ErrorListController], errors)

  private def openWindow(event: MouseEvent, controller: Class[? <: HOIIVUtilsAbstractController2 | HOIIVUtilsAbstractController], fxml: String): Unit =
    if event.isControlDown then
      try controller.getDeclaredConstructor().newInstance().open()
      catch case e: Exception => logger.error("Failed to open window", e)
    else detailPanelManager.switchToView(fxml)


  private def cancelTask(): Unit =
    if currentTask != null && !currentTask.isDone then
      try currentTask.cancel(true)
      catch case ex: Exception => logger.debug("Failed to cancel task", ex)

  private def handleInvalidUnitsFolder(): Unit = {
    logger.warn("Unit comparison view cannot open: missing base or mod units folder.")
    JOptionPane.showMessageDialog(
      null,
      s"version: ${config.getProperties.getProperty("version")} Unit folders not found. Please check your HOI4 installation or the chosen mod directory.",
      "Error",
      JOptionPane.WARNING_MESSAGE
    )
  }

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