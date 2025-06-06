package com.hoi4utils.ui.hoi4localization

import com.hoi4utils.HOIIVFiles
import com.hoi4utils.hoi4.focus.{FixFocus, Focus, FocusTree}
import com.hoi4utils.localization.{Localization, LocalizationManager, Property}
import com.hoi4utils.ui.{HOIIVUtilsAbstractController, JavaFXUIManager}
import com.hoi4utils.ui.javafx_ui.table.TableViewWindow
import javafx.collections.{FXCollections, ObservableList}
import javafx.fxml.FXML
import javafx.scene.control.*
import javafx.scene.control.cell.TextFieldTableCell
import javafx.scene.paint.Color

import javax.swing.JOptionPane
import java.io.{File, IOException}
import java.util.Iterator
import java.util.function.Function
import scala.compiletime.uninitialized
import scala.jdk.CollectionConverters.*

/**
 * TODO: have to redo some methods/design to fit with the new localization system
 */
class FocusLocalizationController extends HOIIVUtilsAbstractController with TableViewWindow:

  @FXML private var numLocAddedLabel: Label = uninitialized
  @FXML private var focusTreeFileTextField: TextField = uninitialized
  @FXML private var focusTreeFileBrowseButton: Button = uninitialized
  @FXML private var focusTreeNameLabel: Label = uninitialized
  @FXML private var focusLocFileTextField: TextField = uninitialized
  @FXML private var focusLocFileBrowseButton: Button = uninitialized
  @FXML private var loadButton: Button = uninitialized
  @FXML private var saveButton: Button = uninitialized
  @FXML private var focusListTable: TableView[Focus] = uninitialized
  @FXML private var focusIDColumn: TableColumn[Focus, String] = uninitialized
  @FXML private var focusNameColumn: TableColumn[Focus, String] = uninitialized
  @FXML private var focusDescColumn: TableColumn[Focus, String] = uninitialized
  @FXML private var focusLocStatusColumn: TableColumn[Focus, String] = uninitialized
  @FXML private var localizationStatusRadioItem: RadioMenuItem = uninitialized

  private var focusTree: Option[FocusTree] = None
  private val focusObservableList: ObservableList[Focus] = FXCollections.observableArrayList()

  // Constructor equivalent
  setFxmlResource("FocusLocalization.fxml")
  setTitle("HOIIVUtils Focus Localization")

  /**
   * JavaFX initialize method
   */
  @FXML
  def initialize(): Unit =
    // table setup
    loadTableView(this, focusListTable, focusObservableList, Focus.getDataFunctions)

    val dataFunction: Function[Focus, String] = (focus: Focus) =>
      s"Name: ${focus.localizationStatus(Property.valueOf("NAME"))}, " +
        s"Desc: ${focus.localizationStatus(Property.valueOf("DESCRIPTION"))}"

    focusLocStatusColumn.setCellValueFactory(JavaFXUIManager.tableCellDataCallback(dataFunction))

    // buttons
    saveButton.setDisable(true)

    // effects
    focusLocStatusColumn.setVisible(localizationStatusRadioItem.isSelected)

  @FXML
  private def handleFocusTreeFileBrowseButtonAction(): Unit =
    val initialFocusDirectory = HOIIVFiles.Mod.focus_folder
    val selectedFile = JavaFXUIManager.openChooser(focusTreeFileBrowseButton, initialFocusDirectory, false)

    println(selectedFile)

    Option(selectedFile) match
      case Some(file) =>
        focusTreeFileTextField.setText(file.getAbsolutePath)
        focusTree = FocusTree.get(file)
        focusTree match
          case Some(tree) =>
            focusTreeNameLabel.setText(tree.toString)
          case None =>
            JOptionPane.showMessageDialog(null, "Error: Selected focus tree not found in loaded focus trees.")
            return
      case None =>
        focusTreeNameLabel.setText("[not found]")

  @FXML
  private def handleFocusLocFileBrowseButtonAction(): Unit = ???
  // Commented out in original - keeping as comment
//  val initialFocusLocDirectory = HOIIVUtilsFiles.mod_localization_folder
//  val selectedFile = openChooser(focusLocFileBrowseButton, initialFocusLocDirectory, false)
//
//  println(selectedFile)
//
//  Option(selectedFile) match
//    case Some(file) =>
//      focusLocFileTextField.setText(file.getAbsolutePath)
//      try
//        focusLocFile = FocusLocalizationFile(file)
//      catch
//        case e: IllegalLocalizationFileTypeException =>
//          openError(e)
//          return
//      focusTree.foreach(_.setLocalization(focusLocFile))
//      println(s"Set localization file of $focusTree to $focusLocFile")
//    case None => // do nothing


  @FXML
  private def handleLoadButtonAction(): Unit =
    focusTree match
      case None =>
        JOptionPane.showMessageDialog(null, "Error: Focus tree not properly initialized.")
        return
      case Some(tree) =>
        // load focus loc
        try
          FixFocus.fixLocalization(tree)
          updateNumLocalizedFocuses(tree.listFocuses.count(_.isLocalized))
        catch
          case e: IOException =>
            openError(e)
            return

        updateObservableFocusList()
        // enable saving of localization
        saveButton.setDisable(false)

  private def updateObservableFocusList(): Unit =
    focusTree.foreach { tree =>
      focusObservableList.clear()
      focusObservableList.addAll(tree.listFocuses.asJava)
    }

  @SuppressWarnings(Array("unused"))
  private def updateObservableFocusList(focus: Focus): Unit =
    if focus == null then
      println("Focus was null and therefore did not update focus list.")
      return

    var i = 0
    val iterator = focusObservableList.iterator()
    while iterator.hasNext do
      val f = iterator.next()
      if f.id.value == focus.id.value then
        iterator.remove() // Remove the current element using the iterator
        focusObservableList.add(i, focus)
        return // update is done
      i += 1

    // never updated list, means focus not found in list
    System.err.println(
      s"Attempted to update focus $focus in focus observable list, but it was not found in the list.")

  private def updateNumLocalizedFocuses(numLocalizedFocuses: Int): Unit =
    numLocAddedLabel.setText(
      numLocAddedLabel.getText.replace("x", numLocalizedFocuses.toString)
    )

  @FXML
  private def handleSaveButtonAction(): Unit =
    // Commented out in original - keeping as comment
    /*
    focusLocFile match
      case None =>
        val window = MessageController()
        window.open("Error: Focus localization file not properly initialized.")
        return
      case Some(file) =>
        file.writeLocalization()
    */
    LocalizationManager.get.saveLocalization()

  @FXML
  private def handleShowFocusLocalizationSettingAction(): Unit =
    focusLocStatusColumn.setVisible(localizationStatusRadioItem.isSelected)

  override def setDataTableCellFactories(): Unit =
    // column factory
    focusNameColumn.setCellFactory(TextFieldTableCell.forTableColumn())
    focusDescColumn.setCellFactory(TextFieldTableCell.forTableColumn()) // This makes the column editable
    focusLocStatusColumn.setCellFactory(TextFieldTableCell.forTableColumn())
    setColumnOnEditCommits()

    // row factory
    focusListTable.setRowFactory(_ => new TableRow[Focus]():
      override protected def updateItem(focus: Focus, empty: Boolean): Unit =
        super.updateItem(focus, empty)

        if focus == null || empty then
          setGraphic(null) // Clear any previous content
        else
          val textStatus = focus.localizationStatus(Property.valueOf("NAME"))
          val descStatus = focus.localizationStatus(Property.valueOf("DESCRIPTION"))

          val hasStatusUpdated = textStatus == Localization.Status.valueOf("UPDATED") ||
            descStatus == Localization.Status.valueOf("UPDATED")
          val hasStatusNew = descStatus == Localization.Status.valueOf("NEW") ||
            textStatus == Localization.Status.valueOf("NEW")

          if hasStatusUpdated || hasStatusNew then
            setTextFill(Color.BLACK) // Set text color to black
            setStyle("-fx-font-weight: bold; -fx-background-color: #328fa8;") // Apply bold text using CSS
          else
            setTextFill(Color.BLACK) // Set text color to black
            setStyle("-fx-background-color: transparent;") // Reset style
    )

  private def setColumnOnEditCommits(): Unit =
    focusNameColumn.setOnEditCommit(event =>
      val focus = event.getRowValue
      focus.replaceLocalization(Property.valueOf("NAME"), event.getNewValue)
      focusListTable.refresh() // Force update of the table view
    )

    focusDescColumn.setOnEditCommit(event =>
      val focus = event.getRowValue
      focus.replaceLocalization(Property.valueOf("DESCRIPTION"), event.getNewValue)
      focusListTable.refresh() // Force update of the table view
    )