package com.hoi4utils.ui.focus_view

import com.hoi4utils.{HOIIVFiles, HOIIVUtils}
import com.hoi4utils.ddsreader.DDSReader
import com.hoi4utils.hoi4.country.CountryTag
import com.hoi4utils.hoi4.focus.{FixFocus, Focus, FocusTree}
import com.hoi4utils.script.PDXScript
import com.hoi4utils.ui.HOIIVUtilsAbstractController
import com.hoi4utils.ui.focus_view.FocusTreeController.updateLoadingStatus
import com.hoi4utils.ui.javafx_ui.image.ScalaFXImageUtils
import com.hoi4utils.ui.pdxscript.{NewFocusTreeController, PDXEditorPane}
import com.typesafe.scalalogging.LazyLogging
import javafx.application.Platform
import javafx.beans.property.SimpleBooleanProperty
import javafx.collections.ObservableList
import javafx.fxml.FXML
import javafx.geometry.{Point2D, Rectangle2D}
import javafx.scene.canvas.{Canvas, GraphicsContext}
import javafx.scene.control.*
import javafx.scene.image.Image
import javafx.scene.input.{MouseButton, MouseEvent}
import javafx.scene.layout.AnchorPane
import javafx.scene.paint.Color

import javax.swing.JOptionPane
import java.io.*
import java.time.LocalDateTime
import java.util.{Comparator, function}
import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.jdk.CollectionConverters.*
import scala.util.{Failure, Success, Try}
import scala.compiletime.uninitialized
import java.util.function.Consumer
import scala.annotation.unused

class FocusTreeController extends HOIIVUtilsAbstractController with LazyLogging {
  setFxmlResource("FocusTree.fxml")
  setTitle("Focus Tree View")

  // Constants
  private val VISIBLE_DROPDOWN_ROW_COUNT: Int = 20

  // FXML components
  @FXML private var focusTreeDropdown: ComboBox[FocusTree] = uninitialized
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
  def initialize(): Unit = {
    // Ensure FXML components are loaded first
    if (contentContainer == null || loadingLabel == null) {
      logger.error("FXML components not properly initialized")
      return
    }

    // Set initial UI state on FX thread
    contentContainer.setVisible(false)
    loadingLabel.setVisible(true)
    loadingLabel.setText("Beginning focus tree view initialization...")

    // Validate other required FXML components
    if (focusTreeDropdown == null || exportFocusTreeButton == null) {
      logger.error("Required FXML components are null")
      Platform.runLater(() => {
        loadingLabel.setText("Error: Required UI components not loaded")
        initFailed.set(true)
      })
      return
    }

    // Setup UI components that don't require background loading
    setupUIComponents()

    // Start background initialization
    val task = new javafx.concurrent.Task[InitializationResult] {
      override def call(): InitializationResult = {
        try {
          performBackgroundInitialization()
        } catch {
          case e: Exception =>
            logger.error("Background initialization failed", e)
            InitializationResult(success = false, error = Some(e.getMessage), focusTree = None, focusTrees = None)
        }
      }
    }

    task.setOnSucceeded(_ => {
      val result = task.getValue
      handleInitializationResult(result)
    })

    task.setOnFailed(_ => {
      logger.error("Initialization task failed", task.getException)
      Platform.runLater(() => {
        loadingLabel.setText(s"Initialization failed: ${task.getException.getMessage}")
        initFailed.set(true)
        showErrorDialog("Initialization Error", s"Failed to initialize: ${task.getException.getMessage}")
      })
    })

    task.setOnCancelled(_ => {
      logger.warn("Initialization was cancelled")
      Platform.runLater(() => {
        loadingLabel.setText("Initialization was cancelled")
        initFailed.set(true)
        showWarningDialog("Cancelled", "Focus Tree initialization was cancelled.")
      })
    })

    // Start the task in a new thread
    val thread = new Thread(task)
    thread.setDaemon(true)
    thread.start()
  }

  private def setupUIComponents(): Unit = {
    // Setup components that can be configured immediately
    focusTreeDropdown.setTooltip(new Tooltip("Select a focus tree to view"))
    focusTreeDropdown.setVisibleRowCount(VISIBLE_DROPDOWN_ROW_COUNT)

    exportFocusTreeButton.setOnAction(_ => handleExportFocusTreeButtonClick())

    focusTreeViewSplitPane.getItems.add(focusTreePane)
  }

  private def performBackgroundInitialization(): InitializationResult = {

    def updateStatus(status: String): Unit = {
      Platform.runLater(() => updateLoadingStatus(loadingLabel, status))
    }

    // Load available focus trees
    updateStatus("Loading focus trees...")
    val trees: ObservableList[FocusTree] = FocusTree.observeFocusTrees

    if (trees == null) {
      updateStatus("Focus trees list is null. Ensure mod files are loaded correctly.")
      logger.error("Focus trees list is null. Ensure mod files are loaded correctly.")
      return InitializationResult(success = false, error = Some("Focus trees list is null"), focusTree = None, focusTrees = None)
    }

    if (trees.isEmpty) {
      updateStatus("Focus trees list is empty. Ensure mod files are loaded correctly.")
      logger.warn("Focus trees list is empty. Ensure mod files are loaded correctly.")
      return InitializationResult(success = false, error = Some("No focus trees found"), focusTree = None, focusTrees = Some(trees))
    }

    updateStatus(s"Focus trees loaded successfully: ${trees.size()} items")
    logger.info(s"Focus trees loaded successfully: ${trees.size()} items")

    // Try loading a specific focus tree
    updateStatus("Loading focus tree...")
    val loadedFocusTree = getFocusTree("SMA", "massachusetts.txt")

    loadedFocusTree match {
      case None =>
        updateStatus("No valid Focus Tree found. Ensure mod files are loaded correctly.")
        logger.error("Failed to load a valid focus tree. This may indicate an issue with mod loading.")
        return InitializationResult(success = false, error = Some("No valid Focus Tree found"), focusTree = None, focusTrees = Some(trees))

      case Some(tree) =>
        updateStatus("Focus tree loaded successfully.")
        logger.info(s"Loaded focus tree: $tree")

        // Fix localization
        updateStatus(s"Fixing localization for focus tree: $tree")
        Try(FixFocus.fixLocalization(tree)) match {
          case Success(_) =>
            updateStatus("Localization fix completed.")
            logger.debug("Localization fix completed.")
          case Failure(e) =>
            updateStatus(s"Failed to fix localization for focus tree: $tree")
            logger.error(s"Failed to fix localization for focus tree: $tree", e)
            return InitializationResult(success = false, error = Some(s"Failed to fix localization: ${e.getMessage}"), focusTree = Some(tree), focusTrees = Some(trees))
        }

        updateStatus("Initialization completed successfully. Drawing focus tree to canvas and displaying...")
        return InitializationResult(success = true, error = None, focusTree = Some(tree), focusTrees = Some(trees))
    }
  }

  private def handleInitializationResult(result: InitializationResult): Unit = {
    Platform.runLater(() => {
      if (!result.success) {
        initFailed.set(true)
        result.error.foreach { error =>
          logger.error("Initialization failed", error)
          showErrorDialog("Initialization Error", error)
        }
        return
      }

      logger.debug("Adding focus trees to dropdown...")
      // Setup dropdown with loaded trees
      result.focusTrees.foreach { trees =>
        focusTreeDropdown.setItems(trees)
        focusTreeDropdown.getItems.sort(Comparator.comparing[FocusTree, String](_.toString))

        if (!trees.isEmpty) {
          focusTreeDropdown.getSelectionModel.select(0)
        }

        // Setup selection listener
        focusTreeDropdown.getSelectionModel.selectedItemProperty().addListener { (_, _, newValue) =>
          Option(newValue).foreach { tree =>
            logger.debug(s"Focus tree selected: $tree")
            setFocusTree(tree)
          }
        }
      }

      // Set the loaded focus tree
      logger.debug("Setting focus tree...")
      result.focusTree.foreach { tree =>
        setFocusTree(tree)
      }

      // Show completion message
      result.focusTree.foreach { tree =>
        logger.info(s"Loaded focuses: ${tree.focuses.size}")
        logger.info(s"Country: ${tree.country.value}")
        logger.info(s"Focus tree: $tree")

        // Optional: Show completion dialog
        showInfoDialog("Initialization Complete",
          s"Loaded focuses: ${tree.focuses.size}\n" +
            s"Loaded tree of country: ${tree.country.value}\n" +
            s"Focus tree: $tree")
      }

      // Switch to main content
      loadingLabel.setVisible(false)
      logger.debug("Switching to main content view...")
      contentContainer.setVisible(true)
      logger.info("FocusTreeController initialized successfully.")
    })
  }

  private def setFocusTree(tree: FocusTree): Unit = {
    focusTreePane.focusTree = tree
  }

  // Helper case class for initialization results
  private case class InitializationResult(
                                           success: Boolean,
                                           error: Option[String],
                                           focusTree: Option[FocusTree],
                                           focusTrees: Option[ObservableList[FocusTree]]
                                         )

  // Helper methods for dialogs
  private def showErrorDialog(title: String, message: String): Unit = {
    JOptionPane.showMessageDialog(null, message, title, JOptionPane.ERROR_MESSAGE)
  }

  private def showWarningDialog(title: String, message: String): Unit = {
    JOptionPane.showMessageDialog(null, message, title, JOptionPane.WARNING_MESSAGE)
  }

  private def showInfoDialog(title: String, message: String): Unit = {
    JOptionPane.showMessageDialog(null, message, title, JOptionPane.INFORMATION_MESSAGE)
  }

  /**
   * Attempts to load a valid FocusTree from different sources.
   */
  private def getFocusTree(tag: String, filePath: String): Option[FocusTree] = {
    // Attempt to get the FocusTree by country tag
    val focusTreeOpt = FocusTree.get(CountryTag(tag))
      .orElse {
        // Attempt to load the FocusTree from a file
        val focusFile = new File(HOIIVFiles.Mod.focus_folder, filePath)
        if (focusFile.exists()) FocusTree.get(focusFile) else None
      }
      .orElse {
        // Attempt to find a valid FocusTree from the list of focus trees
        val focusTrees = FocusTree.listFocusTrees
        focusTrees.find(_.nonEmpty)
      }

    // Handle the result
    focusTreeOpt match {
      case Some(tree) =>
        logger.info(s"Found valid FocusTree: $tree")
        Some(tree)
      case None =>
        logger.error(s"No valid FocusTree found for tag: $tag or file: $filePath")
        None
    }
  }

  private def exportFocusTree(focusTree: FocusTree, path: String): Unit = {
    Try {
      val writer = new PrintWriter(path)
      try {
        writer.println(s"### Generated by HOI4Utils, time: ${LocalDateTime.now()} ###")
        writer.println(focusTree.toScript)
        logger.info(s"Exported focus tree $focusTree to $path")
      } finally {
        writer.close()
      }
    }.recover {
      case e: FileNotFoundException =>
        logger.error(s"Error exporting focus tree: ${e.getMessage}")
        showErrorDialog("Export Error", s"Error exporting focus tree: ${e.getMessage}")
    }
  }

  @FXML
  def handleExportFocusTreeButtonClick(): Unit = {
    val path = "focusOutput.txt"
    logger.info(s"Exporting focus tree to $path")
    focusTreePane.focusTree match {
      case Some(tree) =>
        exportFocusTree(tree, path)
      case None =>
        logger.error("No focus tree available to export.")
        JOptionPane.showMessageDialog(null, "No focus tree available to export.", "Error", JOptionPane.ERROR_MESSAGE)
        return
    }
    JOptionPane.showMessageDialog(null, s"Focus tree exported to $path", "Export Successful",
      JOptionPane.INFORMATION_MESSAGE)
  }

  //  @FXML
  //  def openEditorWindow(focus: Focus): Unit = {
  //    require(focus != null, "Focus cannot be null")
  //    val pdxEditor = new PDXEditorPane(focus.asInstanceOf[PDXScript[?]]) // this is not necessarily redundant. DO NOT CHANGE
  //    focusTreeViewSplitPane.getItems.removeIf(_.isInstanceOf[PDXEditorPane])
  //    focusTreeViewSplitPane.getItems.add(pdxEditor)
  //  }


  def openEditorWindow(focus: Focus, onUpdate: Runnable): Unit = {
    require(focus != null, "Focus cannot be null")
    val pdxEditor = new PDXEditorPane(focus.asInstanceOf[PDXScript[?]], onUpdate) // DO NOT CHANGE
    focusTreeViewSplitPane.getItems.removeIf(_.isInstanceOf[PDXEditorPane])
    focusTreeViewSplitPane.getItems.add(pdxEditor)
  }

  def openNewFocusTreeWindow(): Unit = {
    val newFocusTreeController = new NewFocusTreeController()
    newFocusTreeController.open(new Consumer[FocusTree] {
      override def accept(f: FocusTree): Unit = {
        addFocusTree(f)
        viewFocusTree(f)
      }
    })
  }

  def addFocusTree(focusTree: FocusTree): Unit = {
    focusTreeDropdown.getItems.add(focusTree)
    focusTreeDropdown.getItems.sort(Comparator.comparing[FocusTree, String](_.toString))
  }

  def viewFocusTree(focusTree: FocusTree): Unit = {
    logger.info(s"Viewing focus tree: $focusTree")
    focusTreeDropdown.getSelectionModel.select(focusTree)
  }

  @FXML
  private def toggleGridLines(): Unit = focusTreePane.toggleGridLines()

}

object FocusTreeController {
  def updateLoadingStatus(loadingLabel: Label, status: String): Unit = {
    Platform.runLater(() => {
      if (loadingLabel != null) {
        val currentText = loadingLabel.getText
        loadingLabel.setText(if (currentText.isEmpty) status else s"$currentText\n$status")
      }
    })
  }
}
