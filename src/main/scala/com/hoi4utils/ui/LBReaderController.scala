package com.hoi4utils.ui

import com.hoi4utils.HOIIVUtils
import com.hoi4utils.StateFilesWatcher.statesThatChanged
import com.hoi4utils.parser.Parser.parserFileErrors
import com.map.State.stateFileErrors
import com.map.ResourcesFile.resourcesFileErrors
import com.hoi4utils.extensions.*
import com.typesafe.scalalogging.LazyLogging
import javafx.fxml.FXML
import javafx.scene.control.{Label, ListCell, ListView}
import javafx.util.Callback

import scala.collection.mutable.ListBuffer
import scala.compiletime.uninitialized

class LBReaderController extends HOIIVUtilsAbstractController with LazyLogging {
  setTitle("LB Reader")
  setFxmlResource("LBReader.fxml")

  @FXML var parserFileErrorsList: ListView[String] = uninitialized
  @FXML var statesThatChangedList: ListView[String] = uninitialized
  @FXML var resouceFileErrorsList: ListView[String] = uninitialized
  @FXML var stateFileErrorsList: ListView[String] = uninitialized

  val testList: ListBuffer[String] = ListBuffer.empty[String]

  def initialize(): Unit = {
    update()
  }

  private def update(): Unit = {
    parserFileErrorsList.setItems(getListOrDefaultMessage(parserFileErrors))
    stateFileErrorsList.setItems(getListOrDefaultMessage(stateFileErrors))
    resouceFileErrorsList.setItems(getListOrDefaultMessage(resourcesFileErrors))
    statesThatChangedList.setItems(getListOrDefaultMessage(statesThatChanged, "No States Changed"))
    List(
      parserFileErrorsList,
      stateFileErrorsList,
      resouceFileErrorsList,
      statesThatChangedList
    ).foreach(setupAlternatingListView)
  }

  private def getListOrDefaultMessage(listBuffer: ListBuffer[String], message: String = "No Problems Found") = {
    listBuffer match {
      case null | Nil => ListBuffer(message).toObservableList
      case _ => listBuffer.toObservableList
    }
  }

  private def setupAlternatingListView(listView: ListView[String]): Unit =
    listView.setCellFactory((_: ListView[String]) => new ListCell[String] {
      private val label = new Label()
      setWrapText(true)
      label.setWrapText(true)
      setGraphic(label)

      override def updateItem(item: String, empty: Boolean): Unit =
        super.updateItem(item, empty)
        if empty || item == null then
          label.setText(null)
          setStyle("")
        else
          label.setText(item)
          val isEven = getIndex % 2 == 0
          if (HOIIVUtils.get("theme").equals("dark"))
            // Dark mode colors
            setWrapText(true)
            label.setWrapText(true)
            val bgColor = if isEven then "#2E2E2E" else "#3A3A3A" // dark gray
            setStyle(s"-fx-background-color: $bgColor; -fx-wrap-text: true;")
            label.setStyle(s"-fx-text-fill: lightgrey; -fx-wrap-text: true;") // text color based on background
          else
            // Light mode colors
            setWrapText(true)
            label.setWrapText(true)
            val bgColor = if isEven then "#FFFFFF" else "#D3D3D3" // white or light gray
            setStyle(s"-fx-background-color: $bgColor; -fx-wrap-text: true;")
            label.setStyle(s"-fx-text-fill: black; -fx-wrap-text: true;") // text color based on background
    })
}
