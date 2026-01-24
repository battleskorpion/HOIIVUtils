package com.hoi4utils.ui.parser

import com.hoi4utils.extensions.validateFolder
import com.hoi4utils.main.HOIIVFiles
import com.hoi4utils.ui.javafx.application.HOIIVUtilsAbstractController2
import com.typesafe.scalalogging.LazyLogging
import javafx.application.Platform
import javafx.fxml.FXML
import javafx.scene.control.{Button, Label, TreeCell, TreeItem, TreeView}
import javafx.scene.image.ImageView
import javafx.scene.layout.VBox

import java.io.File
import scala.compiletime.uninitialized
import scala.jdk.CollectionConverters.*
import scala.util.{Failure, Success, Try}

class ParserViewer2Controller extends HOIIVUtilsAbstractController2 with LazyLogging:
  setFxmlFile("/com/hoi4utils/ui/parser/ParserViewer2.fxml")
  setTitle("Parser Viewer 2")

  @FXML var root: VBox = uninitialized
  @FXML var fileTreeView: TreeView[File] = uninitialized
  @FXML var refreshTreeButton: Button = uninitialized
  @FXML var statusLabel: Label = uninitialized
  @FXML var fileCountLabel: Label = uninitialized

  var modFolder: File = HOIIVFiles.Mod.folder

  @FXML def initialize(): Unit =
    setWindowControlsVisibility()
    setupFileTree()
    setupButtons()
    loadModFolder()

  override def preSetup(): Unit =
    setupWindowControls(root)

  private def setupFileTree(): Unit =
    // Setup custom cell factory to display file names with icons
    fileTreeView.setCellFactory(_ => new TreeCell[File]:
      override def updateItem(item: File, empty: Boolean): Unit =
        super.updateItem(item, empty)
        if empty || item == null then
          setText(null)
          setGraphic(null)
        else
          setText(item.getName)
          val icon = if item.isDirectory then createFolderIcon() else createFileIcon()
          setGraphic(icon)
    )

    // Setup selection listener - this is where file click handling happens
    fileTreeView.getSelectionModel.selectedItemProperty.addListener((_, _, newValue) =>
      if newValue != null then
        val selectedFile = newValue.getValue
        handleFileClick(selectedFile)
    )

  private def setupButtons(): Unit =
    refreshTreeButton.setOnAction(_ => handleRefresh())

  private def handleRefresh(): Unit =
    loadModFolder()

  private def loadModFolder(): Unit =
    Try {
      val rootItem = new TreeItem[File](modFolder)
      rootItem.setExpanded(true)

      buildTreeStructure(rootItem, modFolder)

      Platform.runLater(() => {
        fileTreeView.setRoot(rootItem)
        updateFileCount(rootItem)
        statusLabel.setText(s"Loaded: ${modFolder.getName}")
      })
    } match
      case Success(_) =>
        logger.info(s"Successfully loaded mod folder tree: ${modFolder.getAbsolutePath}")
      case Failure(ex) =>
        logger.error(s"Failed to load mod folder tree: ${ex.getMessage}", ex)
        statusLabel.setText(s"Error loading folder: ${ex.getMessage}")

  private def buildTreeStructure(parentItem: TreeItem[File], directory: File): Unit =
    directory.validateFolder("directory") match
      case Right(validDir) =>
        val children = Option(validDir.listFiles()).getOrElse(Array.empty[File])

        // Sort: directories first, then files, both alphabetically
        val sorted = children.sortBy(f => (!f.isDirectory, f.getName.toLowerCase))

        sorted.foreach { file =>
          val childItem = new TreeItem[File](file)
          parentItem.getChildren.add(childItem)

          // Recursively add subdirectories with lazy loading
          if file.isDirectory then
            childItem.setExpanded(false)

            // Lazy load children when expanded
            childItem.expandedProperty.addListener((_, _, expanded) =>
              if expanded && childItem.getChildren.isEmpty then
                buildTreeStructure(childItem, file)
            )
        }
      case Left(error) =>
        logger.warn(s"Cannot access directory ${directory.getAbsolutePath}: $error")

  /**
   * Handle file click events. Override this method to customize behavior.
   * By default, it logs the file and updates the status label.
   *
   * @param file The file that was clicked
   */
  protected def handleFileClick(file: File): Unit =
    if file.isFile then
      logger.info(s"File clicked: ${file.getAbsolutePath}")
      statusLabel.setText(s"Selected: ${file.getName}")
      onFileSelected(file)
    else
      logger.info(s"Directory clicked: ${file.getAbsolutePath}")
      statusLabel.setText(s"Directory: ${file.getName}")
      onDirectorySelected(file)

  /**
   * Called when a file (not directory) is selected.
   * Override this method to implement custom file handling logic.
   *
   * @param file The selected file
   */
  protected def onFileSelected(file: File): Unit =
    // Default implementation - override in subclass or add logic here
    logger.debug(s"File selected: ${file.getName}")
  // Example: Parse the file, display contents, etc.

  /**
   * Called when a directory is selected.
   * Override this method to implement custom directory handling logic.
   *
   * @param directory The selected directory
   */
  protected def onDirectorySelected(directory: File): Unit =
    // Default implementation - override in subclass or add logic here
    logger.debug(s"Directory selected: ${directory.getName}")

  private def updateFileCount(rootItem: TreeItem[File]): Unit =
    val count = countFiles(rootItem)
    fileCountLabel.setText(s"$count file(s)")

  private def countFiles(item: TreeItem[File]): Int =
    val file = item.getValue
    if file.isFile then 1
    else
      item.getChildren.asScala.map(countFiles).sum

  private def createFolderIcon(): ImageView =
    val icon = new ImageView()
    icon.setFitWidth(16)
    icon.setFitHeight(16)
    icon.setPreserveRatio(true)
    // Load actual folder icon if available
    // Try(new Image(getClass.getResourceAsStream("/icons/folder.png"))).foreach(icon.setImage)
    icon

  private def createFileIcon(): ImageView =
    val icon = new ImageView()
    icon.setFitWidth(16)
    icon.setFitHeight(16)
    icon.setPreserveRatio(true)
    // Load actual file icon if available
    // Try(new Image(getClass.getResourceAsStream("/icons/file.png"))).foreach(icon.setImage)
    icon