package com.hoi4utils.ui.menus

import com.hoi4utils.databases.effect.EffectDatabase.effectErrors
import com.hoi4utils.extensions.*
import com.hoi4utils.hoi4.common.idea.IdeasManager.ideaFileErrors
import com.hoi4utils.hoi4.common.national_focus.FocusTreeManager.focusTreeErrors
import com.hoi4utils.hoi4.gfx.Interface.interfaceErrors
import com.hoi4utils.hoi4.history.countries.CountryFile.countryErrors
import com.hoi4utils.hoi4.localization.BaseLocalizationService.localizationErrors
import com.hoi4utils.hoi4.map.resource.Resource.resourceErrors
import com.hoi4utils.hoi4.map.state.State.stateErrors
import com.hoi4utils.main.HOIIVUtils
import com.hoi4utils.script.{FocusTreeErrorGroup, PDXFileError}
import com.hoi4utils.ui.javafx.application.HOIIVUtilsAbstractController2
import javafx.application.Platform
import javafx.concurrent.Task
import javafx.scene.control.{Button, TabPane}
import javafx.scene.layout.BorderPane
//import com.hoi4utils.StateFilesWatcher.statesThatChanged

import com.typesafe.scalalogging.LazyLogging
import javafx.fxml.FXML
import javafx.scene.control.{Button, Label, TreeCell, TreeItem, TreeView}
import javafx.scene.layout.HBox
import javafx.util.Callback
import javafx.geometry.Insets

import scala.collection.mutable.ListBuffer
import scala.compiletime.uninitialized
import java.io.File
import java.awt.Desktop

class ErrorListController extends HOIIVUtilsAbstractController2 with LazyLogging:
  setTitle("LB Reader")
  setFxmlFile("ErrorList.fxml")
  
  @FXML var contentContainer: BorderPane = uninitialized
  @FXML var errorListTabPane: TabPane = uninitialized

  @FXML var effectsEL: TreeView[ErrorTreeItem] = uninitialized
  @FXML var localizationEL: TreeView[ErrorTreeItem] = uninitialized
  @FXML var interfaceEL: TreeView[ErrorTreeItem] = uninitialized
  @FXML var countryEL: TreeView[ErrorTreeItem] = uninitialized
  @FXML var focusTreeEL: TreeView[ErrorTreeItem] = uninitialized
  @FXML var ideaEL: TreeView[ErrorTreeItem] = uninitialized
  @FXML var resourceEL: TreeView[ErrorTreeItem] = uninitialized
  @FXML var stateEL: TreeView[ErrorTreeItem] = uninitialized

  @FXML
  def initialize(): Unit =
    setWindowControlsVisibility()
    val loadErrorsTask = new Task[Unit]() {
      override def call(): Unit = update()
    }

    new Thread(loadErrorsTask).start()

  override def preSetup(): Unit = setupWindowControls(contentContainer, errorListTabPane)

  private def update(): Unit =
    val treeViewsWithErrors = ListBuffer(
      (effectsEL, effectErrors),
//      (localizationEL, localizationErrors),
      (interfaceEL, interfaceErrors),
      (countryEL, countryErrors),
      (ideaEL, ideaFileErrors),
      (resourceEL, resourceErrors),
      (stateEL, stateErrors)
    )

    // Set up each TreeView with grouped errors
    treeViewsWithErrors.foreach((treeView, errors) => setTreeViewItems(treeView, errors))
    treeViewsWithErrors.map(_._1).foreach(setupTreeViewCellFactory)

    // Handle focusTreeEL separately due to hierarchical structure
    setFocusTreeViewItems(focusTreeEL, focusTreeErrors)
    setupTreeViewCellFactory(focusTreeEL)

  /**
   * Groups errors by file and creates a tree hierarchy
   */
  private def setTreeViewItems(treeView: TreeView[ErrorTreeItem], errors: ListBuffer[PDXFileError]): Unit =
    val root = createRootNode(errors)
    Platform.runLater(() => {
      treeView.setRoot(root)
      treeView.setShowRoot(false) // Hide the root node
    })

  /**
   * Creates a root node with errors grouped by file
   */
  private def createRootNode(errors: ListBuffer[PDXFileError]): TreeItem[ErrorTreeItem] =
    val rootItem = new TreeItem(new ErrorTreeItem("Root", ErrorTreeItemType.FileGroup))

    if errors.isEmpty || errors == null then
      val noErrorsItem = new TreeItem(new ErrorTreeItem("No Problems Found", ErrorTreeItemType.ErrorDetail))
      rootItem.getChildren.add(noErrorsItem)
    else
      // Group errors by file
      val errorsByFile = errors.groupBy(_.file)

      errorsByFile.foreach { (file, fileErrors) => 
        // cant really map with Nones 
//        case (Some(file), fileErrors) =>
//          val fileGroupItem = ErrorTreeItem.fromFileGroup(file, fileErrors.toList)
//          val fileTreeItem = buildTreeItem(fileGroupItem)
//          rootItem.getChildren.add(fileTreeItem)
//        case (None, fileErrors) =>
//          // Errors without a file - create a generic group
//          val noFileGroup = new ErrorTreeItem(
//            s"Unknown File (${fileErrors.size} ${if fileErrors.size == 1 then "error" else "errors"})",
//            ErrorTreeItemType.FileGroup,
//            None,
//            fileErrors.map(ErrorTreeItem.fromPDXError).toList
//          )
//          val noFileTreeItem = buildTreeItem(noFileGroup)
//          rootItem.getChildren.add(noFileTreeItem)
        val fileGroupItem = ErrorTreeItem.fromFileGroup(file, fileErrors.toList)
        val fileTreeItem = buildTreeItem(fileGroupItem)
        rootItem.getChildren.add(fileTreeItem)
      }

    rootItem

  /**
   * Creates tree items for focus tree errors with their hierarchical structure
   */
  private def setFocusTreeViewItems(treeView: TreeView[ErrorTreeItem], errors: ListBuffer[FocusTreeErrorGroup]): Unit =
    val root = createFocusTreeRootNode(errors)
    Platform.runLater(() => {
      treeView.setRoot(root)
      treeView.setShowRoot(false)
    })

  /**
   * Creates a root node for focus tree errors
   */
  private def createFocusTreeRootNode(errors: ListBuffer[FocusTreeErrorGroup]): TreeItem[ErrorTreeItem] =
    val rootItem = new TreeItem(new ErrorTreeItem("Root", ErrorTreeItemType.FocusTreeGroup))

    if errors.isEmpty || errors == null then
      val noErrorsItem = new TreeItem(new ErrorTreeItem("No Problems Found", ErrorTreeItemType.ErrorDetail))
      rootItem.getChildren.add(noErrorsItem)
    else
      errors.foreach { focusTreeGroup =>
        val focusTreeItem = ErrorTreeItem.fromFocusTreeErrorGroup(focusTreeGroup)
        val treeItem = buildTreeItem(focusTreeItem)
        rootItem.getChildren.add(treeItem)
      }

    rootItem

  /**
   * Recursively builds TreeItem hierarchy from ErrorTreeItem
   */
  private def buildTreeItem(errorTreeItem: ErrorTreeItem): TreeItem[ErrorTreeItem] =
    val treeItem = new TreeItem(errorTreeItem)

    // Add children recursively
    errorTreeItem.children.foreach { child =>
      treeItem.getChildren.add(buildTreeItem(child))
    }

    treeItem

  /**
   * Finds the root item index for a given TreeItem
   */
  private def getRootItemIndex(treeView: TreeView[ErrorTreeItem], item: TreeItem[ErrorTreeItem]): Int =
    if item == null || treeView.getRoot == null then return 0

    var current = item
    while current.getParent != null && current.getParent != treeView.getRoot do
      current = current.getParent

    if current.getParent == treeView.getRoot then
      treeView.getRoot.getChildren.indexOf(current)
    else
      0

  /**
   * Opens a file in the system default editor
   */
  private def openFile(file: File): Unit =
    try
      if Desktop.isDesktopSupported then
        Desktop.getDesktop.open(file)
    catch
      case e: Exception => logger.error(s"Failed to open file: ${file.getAbsolutePath}", e)

  /**
   * Opens the folder containing the file
   */
  private def openFolder(file: File): Unit =
    try
      if Desktop.isDesktopSupported then
        val folder = if file.isDirectory then file else file.getParentFile
        Desktop.getDesktop.open(folder)
    catch
      case e: Exception => logger.error(s"Failed to open folder: ${file.getAbsolutePath}", e)

  /**
   * Parses and handles clicks on stack trace lines
   */
  private def handleStackTraceClick(stackTraceLine: String): Unit =
    // Parse stack trace line format: "at package.Class.method(FileName.scala:123)"
    val pattern = """at\s+[\w\.$]+\(([\w\.]+):(\d+)\)""".r
    pattern.findFirstMatchIn(stackTraceLine) match
      case Some(m) =>
        val fileName = m.group(1)
        val lineNumber = m.group(2).toInt
        logger.info(s"Stack trace click: $fileName:$lineNumber")
        // TODO: Open file in editor at line number (requires editor integration)
      case None =>
        logger.warn(s"Could not parse stack trace line: $stackTraceLine")

  /**
   * Sets up custom cell factory for TreeView with styling, buttons, and clickable stack traces
   */
  private def setupTreeViewCellFactory(treeView: TreeView[ErrorTreeItem]): Unit =
    // Load CSS for arrow styling
    val cssUrl = getClass.getResource("/com/hoi4utils/ui/css/error-list.css")
    if cssUrl != null then
      treeView.getStylesheets.add(cssUrl.toExternalForm)

    // Add theme-specific style class
    val isDarkMode = HOIIVUtils.get("theme").equals("dark")
    val themeClass = if isDarkMode then "dark-mode" else "light-mode"
    treeView.getStyleClass.add(themeClass)

    treeView.setCellFactory: (_: TreeView[ErrorTreeItem]) =>
      new TreeCell[ErrorTreeItem]:
        override def updateItem(item: ErrorTreeItem, empty: Boolean): Unit =
          super.updateItem(item, empty)

          if empty || item == null then
            setText(null)
            setGraphic(null)
            setStyle("")
          else
            // Apply theme-based styling
            val isDarkMode = HOIIVUtils.get("theme").equals("dark")
            val textColor = if isDarkMode then "lightgrey" else "black"

            // Alternating background colors based on root item index
            val rootIndex = getRootItemIndex(treeView, getTreeItem)
            val isEven = rootIndex % 2 == 0
            val bgColor = if isDarkMode then
              if isEven then "#3A3A3A" else "#424242" // Lighter dark mode colors
            else
              if isEven then "#F5F5F5" else "#E8E8E8" // Lighter light mode colors

            // Different styling based on item type
            val baseStyle = s"-fx-background-color: $bgColor; -fx-padding: 4 4 4 4;"

            // Check if this is a file/focus tree group that needs buttons
            if (item.itemType == ErrorTreeItemType.FileGroup || item.itemType == ErrorTreeItemType.FocusTreeGroup) && item.filePath.isDefined then
              // Create HBox with text and buttons
              val hbox = new HBox(10)
              hbox.setPadding(new Insets(2, 5, 2, 5))

              val label = new Label(item.displayText)
              label.setStyle(s"-fx-text-fill: $textColor; -fx-font-size: 14px; -fx-font-weight: bold;")

              val openFolderBtn = new Button("Open Folder")
              openFolderBtn.setStyle(s"-fx-font-size: 11px; -fx-padding: 2 8 2 8;")
              openFolderBtn.setOnAction(_ => openFolder(item.filePath.get))

              val openFileBtn = new Button("Open File")
              openFileBtn.setStyle(s"-fx-font-size: 11px; -fx-padding: 2 8 2 8;")
              openFileBtn.setOnAction(_ => openFile(item.filePath.get))

              hbox.getChildren.addAll(label, openFolderBtn, openFileBtn)
              setGraphic(hbox)
              setText(null)
              setStyle(baseStyle)

            // Check if this is a clickable stack trace line
            else if item.isStackTraceLine then
              setText(item.displayText)
              setGraphic(null)
              setStyle(s"$baseStyle -fx-text-fill: #4A9EFF; -fx-underline: true; -fx-cursor: hand; -fx-font-size: 12px;")
              setOnMouseClicked(_ => handleStackTraceClick(item.displayText))

            // Regular item
            else
              setText(item.displayText)
              setGraphic(null)
              setOnMouseClicked(null)

              item.itemType match
                case ErrorTreeItemType.FileGroup | ErrorTreeItemType.FocusTreeGroup =>
                  setStyle(s"$baseStyle -fx-font-weight: bold; -fx-text-fill: $textColor; -fx-font-size: 14px;")
                case ErrorTreeItemType.FocusGroup =>
                  setStyle(s"$baseStyle -fx-font-style: italic; -fx-text-fill: $textColor; -fx-font-size: 13px;")
                case ErrorTreeItemType.ErrorDetail =>
                  val errorColor = if isDarkMode then "#FF8A8A" else "#E53935"
                  setStyle(s"$baseStyle -fx-text-fill: $errorColor; -fx-font-size: 13px;")
                case ErrorTreeItemType.ErrorSection =>
                  setStyle(s"$baseStyle -fx-text-fill: $textColor; -fx-font-size: 12px;")