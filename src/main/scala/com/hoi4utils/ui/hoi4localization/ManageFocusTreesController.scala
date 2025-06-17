package com.hoi4utils.ui.hoi4localization

import com.hoi4utils.hoi4.focus.{Focus, FocusTree}
import com.hoi4utils.localization.{Localization, LocalizationManager, Property}
import com.hoi4utils.ui.HOIIVUtilsAbstractController
import com.hoi4utils.ui.javafx_ui.table.TableViewWindow
import javafx.collections.{FXCollections, ObservableList}
import javafx.fxml.FXML
import javafx.scene.control.cell.TextFieldTableCell
import javafx.scene.control.{TableColumn, TableRow, TableView}
import javafx.scene.paint.Color

import java.io.*
import scala.collection.mutable.ListBuffer
import scala.compiletime.uninitialized
import scala.jdk.CollectionConverters.*

class ManageFocusTreesController extends HOIIVUtilsAbstractController with TableViewWindow {

  private val NAME_PROPERTY = Property.valueOf("NAME")
  private val DESC_PROPERTY = Property.valueOf("DESCRIPTION")

  @FXML private var focusListTable: TableView[Focus] = uninitialized
  @FXML private var focusIDColumn: TableColumn[Focus, String] = uninitialized
  @FXML private var focusNameColumn: TableColumn[Focus, String] = uninitialized
  @FXML private var focusDescColumn: TableColumn[Focus, String] = uninitialized

  private val focusObservableList: ObservableList[Focus] = FXCollections.observableArrayList[Focus]()
  private val onSaveActions = ListBuffer[() => Unit]()

  // Constructor
  setFxmlResource("ManageFocusTrees.fxml")
  setTitle("HOIIVUtils Manage Focus Trees")

  /**
   * Initialize method
   */
  @FXML
  def initialize(): Unit = {
    // table
    loadTableView(this, focusListTable, focusObservableList, Focus.getDataFunctions)
    updateObservableFocusList()
  }

  override def setDataTableCellFactories(): Unit = {
    // column factory
    focusNameColumn.setCellFactory(TextFieldTableCell.forTableColumn[Focus]())
    focusDescColumn.setCellFactory(TextFieldTableCell.forTableColumn[Focus]())
    setColumnOnEditCommits()

    // row factory
    focusListTable.setRowFactory(_ => new TableRow[Focus]() {
      override protected def updateItem(focus: Focus, empty: Boolean): Unit = {
        super.updateItem(focus, empty)

        if (focus == null || empty) {
          setGraphic(null)
          setEditable(true)
        } else {
          val textStatus = focus.localizationStatus(NAME_PROPERTY)
          val descStatus = focus.localizationStatus(DESC_PROPERTY)

          val hasStatusUpdated = textStatus == Localization.Status.valueOf("UPDATED") ||
            descStatus == Localization.Status.valueOf("UPDATED")
          val hasStatusNew = descStatus == Localization.Status.valueOf("NEW") ||
            textStatus == Localization.Status.valueOf("NEW")
          val isVanilla = textStatus == Localization.Status.valueOf("VANILLA")

          if (hasStatusUpdated || hasStatusNew) {
            setTextFill(Color.BLACK) // Set text color to black
            setStyle("-fx-font-weight: bold; -fx-background-color: #328fa8;") // Apply bold text using CSS
            setEditable(true)
          } else if (isVanilla) {
            setTextFill(Color.BLACK) // Set text color to black
            setStyle("-fx-background-color: #5d5353;")
            // if vanilla disable editability of row
            setEditable(false)
          } else {
            setTextFill(Color.BLACK) // Set text color to black
            setStyle("-fx-background-color: transparent;") // Reset style
            setEditable(true)
          }
        }
      }
    })
  }

  private def updateObservableFocusList(): Unit = {
    focusObservableList.clear()
    val focuses = FocusTree.listFocusTrees.asJava
    for (focusTree <- focuses.asScala) {
      if (focusTree != null) {
        focusObservableList.addAll(focusTree.listFocuses.asJava)
      }
    }
  }

  @FXML private def handleExportFocusesMissingDescriptionsAction(): Unit = {
    // write to new file
    val file = File("focuses_missing_descriptions.csv")
    try {
      val writer = FileWriter(file, false)
      val bWriter = BufferedWriter(writer)
      val pWriter = PrintWriter(bWriter)

      pWriter.println("Focus ID; Focus Name; Focus Description; Notes")

      for (focus <- focusObservableList.asScala) {
        if (focus.localization(DESC_PROPERTY).isEmpty) {
          Focus.getDataFunctions.foreach { dataFunction =>
            pWriter.print(dataFunction.apply(focus))
            pWriter.print(";")
          }
          pWriter.print("Missing description (no localization key exists);")
          pWriter.println()
        } else if (focus.localization(DESC_PROPERTY).get.text.isEmpty) {
          Focus.getDataFunctions.foreach { dataFunction =>
            pWriter.print(dataFunction.apply(focus))
            pWriter.print(";")
          }
          pWriter.print("Empty description (localization key exists);")
          pWriter.println()
        }
      }

      pWriter.close()
      println(s"Exported focuses missing descriptions csv to ${file.getAbsolutePath}")
    } catch {
      case exc: IOException => exc.printStackTrace()
    }
  }

  private def setColumnOnEditCommits(): Unit = {
    focusNameColumn.setOnEditCommit { event =>
      val focus = event.getRowValue
      onSaveActions += (() => focus.replaceLocalization(NAME_PROPERTY, event.getNewValue))
    }

    focusDescColumn.setOnEditCommit { event =>
      val focus = event.getRowValue
      onSaveActions += (() => focus.replaceLocalization(DESC_PROPERTY, event.getNewValue))
    }
  }

  private def handleSaveButtonAction(): Unit = {
    if (onSaveActions.isEmpty) return
    onSaveActions.foreach(_.apply())
    LocalizationManager.get.saveLocalization()
    onSaveActions.clear()
  }
}