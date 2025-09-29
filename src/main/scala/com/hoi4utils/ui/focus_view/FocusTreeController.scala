package com.hoi4utils.ui.focus_view

import com.hoi4utils.hoi4mod.common.country_tags.CountryTag
import com.hoi4utils.hoi4mod.common.national_focus.{FixFocus, Focus, FocusTreeFile}
import com.hoi4utils.main.{HOIIVFiles, HOIIVUtils}
import com.hoi4utils.script.PDXScript
import com.hoi4utils.ui.custom_javafx.controller.HOIIVUtilsAbstractController
import com.hoi4utils.ui.focus_view.FocusTreeController.updateLoadingStatus
import com.hoi4utils.ui.pdxscript.{NewFocusTreeController, PDXEditorPane}
import com.typesafe.scalalogging.LazyLogging
import javafx.application.Platform
import javafx.beans.property.SimpleBooleanProperty
import javafx.collections.ObservableList
import javafx.fxml.FXML
import javafx.scene.control.*
import javafx.scene.layout.AnchorPane

import java.io.*
import java.time.LocalDateTime
import java.util.Comparator
import java.util.function.Consumer
import javax.swing.JOptionPane
import scala.compiletime.uninitialized
import scala.util.Try

class FocusTreeController extends HOIIVUtilsAbstractController with LazyLogging:
  setFxmlResource("FocusTree.fxml")
  setTitle("Focus Tree View")

  // Constants
  private val VISIBLE_DROPDOWN_ROW_COUNT: Int = 20

  // FXML components
  @FXML private var focusTreeDropdown: ComboBox[FocusTreeFile] = uninitialized
  @FXML private var exportFocusTreeButton: Button = uninitialized
  @FXML private var focusTreeViewSplitPane: SplitPane = uninitialized
  @FXML var loadingLabel: Label = uninitialized
  @FXML var contentContainer: AnchorPane = uninitialized
  @FXML private val focusTreePane = FocusTreeScrollPane(None)

  // State variables
  private val initFailed = new SimpleBooleanProperty(false)

  /**
   * Called when the application is initialized.
   */
  @FXML
  def initialize(): Unit =
    // Start background initialization
    val task = new javafx.concurrent.Task[InitializationResult]:
      override def call(): InitializationResult =
        try
          performBackgroundInitialization()
        catch
          case e: Exception =>
            logger.error("Background initialization failed", e)
            InitializationResult(success = false, error = Some(e.getMessage), focusTree = None, focusTrees = None)

    task.setOnSucceeded(_ =>
      val result = task.getValue
      handleInitializationResult(result)
    )

    task.setOnFailed(_ =>
      logger.error("Initialization task failed", task.getException)
      Platform.runLater(() =>
        loadingLabel.setText(s"Initialization failed: ${task.getException.getMessage}")
        initFailed.set(true)
        showErrorDialog("Initialization Error", s"Failed to initialize: ${task.getException.getMessage}")
      )
    )

    task.setOnCancelled(_ =>
      logger.warn("Initialization was cancelled")
      Platform.runLater(() =>
        loadingLabel.setText("Initialization was cancelled")
        initFailed.set(true)
        showWarningDialog("Cancelled", "Focus Tree initialization was cancelled.")
      )
    )


    // Set initial UI state on FX thread
    contentContainer.setVisible(false)
    loadingLabel.setVisible(true)
    loadingLabel.setText("Beginning focus tree view initialization...")

    // Validate other required FXML components
    if focusTreeDropdown == null || exportFocusTreeButton == null then
      logger.error("Required FXML components are null")
      Platform.runLater(() =>
        loadingLabel.setText("Error: Required UI components not loaded")
        initFailed.set(true)
      )
    else
      // Setup UI components that don't require background loading
      setupUIComponents()
  
      // Start the task in a new thread
      val thread = new Thread(task)
      thread.setDaemon(true)
      thread.start()

  private def setupUIComponents(): Unit =
    // Setup components that can be configured immediately
    focusTreeDropdown.setTooltip(new Tooltip("Select a focus tree to view"))
    focusTreeDropdown.setVisibleRowCount(VISIBLE_DROPDOWN_ROW_COUNT)

    exportFocusTreeButton.setOnAction(_ => handleExportFocusTreeButtonClick())

    focusTreeViewSplitPane.getItems.add(focusTreePane)

  private def performBackgroundInitialization(): InitializationResult =

    def updateStatus(status: String): Unit =
      Platform.runLater(() => updateLoadingStatus(loadingLabel, status))

    // Load available focus trees
    updateStatus("Loading focus trees...")
    val trees: ObservableList[FocusTreeFile] = FocusTreeFile.observeFocusTrees

    if trees == null then
      updateStatus("Focus trees list is null. Ensure mod files are loaded correctly.")
      logger.error("Focus trees list is null. Ensure mod files are loaded correctly.")
      return InitializationResult(success = false, error = Some("Focus trees list is null"), focusTree = None, focusTrees = None)

    if trees.isEmpty then
      updateStatus("Focus trees list is empty. Ensure mod files are loaded correctly.")
      logger.warn("Focus trees list is empty. Ensure mod files are loaded correctly.")
      return InitializationResult(success = false, error = Some("No focus trees found"), focusTree = None, focusTrees = Some(trees))

    updateStatus(s"${trees.size()} Focus trees loaded successfully.")
    logger.info(s"${trees.size()} Focus trees loaded successfully.")

    // Try loading a specific focus tree
    updateStatus("Loading focus tree...")
    val loadedFocusTree = getFocusTree("SMA", "massachusetts.txt")

    loadedFocusTree match
      case None =>
        updateStatus("No valid Focus Tree found. Ensure mod files are loaded correctly.")
        logger.error("Failed to load a valid focus tree. This may indicate an issue with mod loading.")
        InitializationResult(success = false, error = Some("No valid Focus Tree found"), focusTree = None, focusTrees = Some(trees))
      case Some(tree) =>
        updateStatus("Focus tree loaded successfully.")
        logger.info(s"Loaded focus tree: $tree")
        updateStatus("Initialization completed successfully. Drawing focus tree to canvas and displaying...")
        InitializationResult(success = true, error = None, focusTree = Some(tree), focusTrees = Some(trees))

  private def handleInitializationResult(result: InitializationResult): Unit =
    Platform.runLater(() =>
      if !result.success then
        initFailed.set(true)
        result.error.foreach: error =>
          logger.error("Initialization failed", error)
          showErrorDialog("Initialization Error", error)
      else
        logger.debug("Adding focus trees to dropdown...")
        // Setup dropdown with loaded trees
        result.focusTrees.foreach: trees =>
          focusTreeDropdown.setItems(trees)
          focusTreeDropdown.getItems.sort(Comparator.comparing[FocusTreeFile, String](_.toString))

          if !trees.isEmpty then
            focusTreeDropdown.getSelectionModel.select(0)

          // Setup selection listener
          focusTreeDropdown.getSelectionModel.selectedItemProperty().addListener: (_, _, newValue) =>
            Option(newValue).foreach: tree =>
              logger.debug(s"Focus tree selected: $tree")
              setFocusTree(tree)

        // Set the loaded focus tree
        logger.debug("Setting focus tree...")
        result.focusTree.foreach: tree =>
          setFocusTree(tree)

        // Show completion message
        result.focusTree.foreach: tree =>
          logger.info(s"Loaded focuses: ${tree.focuses.size}")
          logger.info(s"Country: ${tree.country.value}")
          logger.info(s"Focus tree: $tree")

          // Optional: Show completion dialog
          showInfoDialog("Initialization Complete",
            s"Loaded focuses: ${tree.focuses.size}\n" +
              s"Loaded tree of country: ${tree.country.value}\n" +
              s"Focus tree: $tree")

        // Switch to main content
        loadingLabel.setVisible(false)
        logger.debug("Switching to main content view...")
        contentContainer.setVisible(true)
        logger.info("FocusTreeController initialized successfully.")
    )

  private def setFocusTree(tree: FocusTreeFile): Unit =
    focusTreePane.focusTree = tree

  // Helper case class for initialization results
  private case class InitializationResult(
                                           success: Boolean,
                                           error: Option[String],
                                           focusTree: Option[FocusTreeFile],
                                           focusTrees: Option[ObservableList[FocusTreeFile]]
                                         )

  // Helper methods for dialogs
  private def showErrorDialog(title: String, message: String): Unit =
    JOptionPane.showMessageDialog(null, message, title, JOptionPane.ERROR_MESSAGE)

  private def showWarningDialog(title: String, message: String): Unit =
    JOptionPane.showMessageDialog(null, message, title, JOptionPane.WARNING_MESSAGE)

  private def showInfoDialog(title: String, message: String): Unit =
    JOptionPane.showMessageDialog(null, message, title, JOptionPane.INFORMATION_MESSAGE)

  /**
   * Attempts to load a valid FocusTree from different sources.
   */
  private def getFocusTree(tag: String, filePath: String): Option[FocusTreeFile] =
    FocusTreeFile.getRandom

  private def exportFocusTree(focusTree: FocusTreeFile, path: String): Unit =
    Try:
      val writer = new PrintWriter(path)
      try
        writer.println(s"### Generated by HOI4Utils, time: ${LocalDateTime.now()} ###")
        writer.println(focusTree.toScript)
        logger.info(s"Exported focus tree $focusTree to $path")
      finally
        writer.close()
    .recover:
      case e: FileNotFoundException =>
        logger.error(s"Error exporting focus tree: ${e.getMessage}")
        showErrorDialog("Export Error", s"Error exporting focus tree: ${e.getMessage}")

  @FXML
  def handleExportFocusTreeButtonClick(): Unit =
    val path = "focusOutput.txt"
    logger.info(s"Exporting focus tree to $path")
    focusTreePane.focusTree match
      case Some(tree) =>
        exportFocusTree(tree, path)
      case None =>
        logger.error("No focus tree available to export.")
        JOptionPane.showMessageDialog(null, "No focus tree available to export.", "Error", JOptionPane.ERROR_MESSAGE)
        return ()

    JOptionPane.showMessageDialog(null, s"Focus tree exported to $path", "Export Successful",
      JOptionPane.INFORMATION_MESSAGE)

  //  @FXML
  //  def openEditorWindow(focus: Focus): Unit = {
  //    require(focus != null, "Focus cannot be null")
  //    val pdxEditor = new PDXEditorPane(focus.asInstanceOf[PDXScript[?]]) // this is not necessarily redundant. DO NOT CHANGE
  //    focusTreeViewSplitPane.getItems.removeIf(_.isInstanceOf[PDXEditorPane])
  //    focusTreeViewSplitPane.getItems.add(pdxEditor)
  //  }

  def openEditorWindow(focus: Focus, onUpdate: Runnable): Unit =
    require(focus != null, "Focus cannot be null")
    val pdxEditor = new PDXEditorPane(focus.asInstanceOf[PDXScript[?]], onUpdate) // DO NOT CHANGE
    focusTreeViewSplitPane.getItems.removeIf(_.isInstanceOf[PDXEditorPane])
    focusTreeViewSplitPane.getItems.add(pdxEditor)

  def openNewFocusTreeWindow(): Unit =
    val newFocusTreeController = new NewFocusTreeController()
    newFocusTreeController.open(new Consumer[FocusTreeFile]:
      override def accept(f: FocusTreeFile): Unit =
        addFocusTree(f)
        viewFocusTree(f)
    )

  def addFocusTree(focusTree: FocusTreeFile): Unit =
    focusTreeDropdown.getItems.add(focusTree)
    focusTreeDropdown.getItems.sort(Comparator.comparing[FocusTreeFile, String](_.toString))

  def viewFocusTree(focusTree: FocusTreeFile): Unit =
    logger.info(s"Viewing focus tree: $focusTree")
    focusTreeDropdown.getSelectionModel.select(focusTree)

  @FXML
  private def toggleGridLines(): Unit = focusTreePane.toggleGridLines()

object FocusTreeController:
  def updateLoadingStatus(loadingLabel: Label, status: String): Unit =
    Platform.runLater(() =>
      if loadingLabel != null then
        val currentText = loadingLabel.getText
        loadingLabel.setText(if currentText.isEmpty then status else s"$currentText\n$status")
    )