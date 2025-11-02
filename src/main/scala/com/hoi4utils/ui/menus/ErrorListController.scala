package com.hoi4utils.ui.menus

import com.hoi4utils.databases.effect.EffectDatabase.effectErrors
import com.hoi4utils.extensions.*
import com.hoi4utils.hoi4.common.idea.IdeasManager.ideaFileErrors
import com.hoi4utils.hoi4.common.national_focus.FocusTreeManager.focusTreeErrors
import com.hoi4utils.hoi4.gfx.Interface.interfaceErrors
import com.hoi4utils.hoi4.history.countries.CountryFile.countryErrors
import com.hoi4utils.hoi4.localization.LocalizationManager.localizationErrors
import com.hoi4utils.hoi4.map.resource.Resource.resourceErrors
import com.hoi4utils.hoi4.map.state.State.stateErrors
import com.hoi4utils.main.HOIIVUtils
import com.hoi4utils.script.{FocusTreeErrorGroup, PDXError}
import com.hoi4utils.ui.javafx.application.HOIIVUtilsAbstractController2
import javafx.application.Platform
import javafx.concurrent.Task
import javafx.scene.control.{Button, TabPane}
import javafx.scene.layout.BorderPane
//import com.hoi4utils.StateFilesWatcher.statesThatChanged

import com.typesafe.scalalogging.LazyLogging
import javafx.fxml.FXML
import javafx.scene.control.{Label, ListCell, ListView}
import javafx.util.Callback

import scala.collection.mutable.ListBuffer
import scala.compiletime.uninitialized

class ErrorListController extends HOIIVUtilsAbstractController2 with LazyLogging:
  setTitle("LB Reader")
  setFxmlFile("ErrorList.fxml")
  
  @FXML var contentContainer: BorderPane = uninitialized
  @FXML var errorListTabPane: TabPane = uninitialized

  @FXML var effectsEL: ListView[PDXError] = uninitialized
  @FXML var localizationEL: ListView[PDXError] = uninitialized
  @FXML var interfaceEL: ListView[PDXError] = uninitialized
  @FXML var countryEL: ListView[PDXError] = uninitialized
  @FXML var focusTreeEL: ListView[FocusTreeErrorGroup] = uninitialized
  @FXML var ideaEL: ListView[PDXError] = uninitialized
  @FXML var resourceEL: ListView[PDXError] = uninitialized
  @FXML var stateEL: ListView[PDXError] = uninitialized
  @FXML var statesThatChangedList: ListView[String] = uninitialized

  val testList: ListBuffer[PDXError] = ListBuffer.empty[PDXError]

  @FXML
  def initialize(): Unit =
    setWindowControlsVisibility()
    val loadErrorsTask = new Task[Unit]() {
      override def call(): Unit = update()
    }

    new Thread(loadErrorsTask).start()

  override def preSetup(): Unit = setupWindowControls(contentContainer, errorListTabPane)

  private def update(): Unit =
    val listViewsWithErrors = ListBuffer(
      (effectsEL, effectErrors),
      (localizationEL, localizationErrors),
      (interfaceEL, interfaceErrors),
      (countryEL, countryErrors),
      (ideaEL, ideaFileErrors),
      (resourceEL, resourceErrors),
      (stateEL, stateErrors)
    )

    listViewsWithErrors.foreach((listView, errors) => setListViewItems(listView, errors))
    listViewsWithErrors.map(_._1).foreach(setupAlternatingListView)

    // Handle focusTreeEL separately due to different type
    setFocusTreeListViewItems(focusTreeEL, focusTreeErrors)
    setupAlternatingFocusTreeListView(focusTreeEL)

  private def setListViewItems(listView: ListView[PDXError], errors: ListBuffer[PDXError]): Unit =
    listView.setItems(getListOrDefaultMessage(errors))

  private def getListOrDefaultMessage(listBuffer: ListBuffer[PDXError], message: String = "No Problems Found") =
    listBuffer match
      case null | Nil => ListBuffer(new PDXError(additionalInfo = Map("message" -> message))).toObservableList
      case _ => listBuffer.toObservableList

  private def setupAlternatingListView(listView: ListView[PDXError]): Unit =
    listView.setCellFactory: (_: ListView[PDXError]) =>
      new ListCell[PDXError]:
        private val label = new Label()
        setWrapText(true)
        label.setWrapText(true)
        setGraphic(label)

        override def updateItem(item: PDXError, empty: Boolean): Unit =
          super.updateItem(item, empty)
          if empty || item == null then
            label.setText(null)
            setStyle("")
          else
            label.setText(item.toString)
            val isEven = getIndex % 2 == 0
            if HOIIVUtils.get("theme").equals("dark") then
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

  private def setFocusTreeListViewItems(listView: ListView[FocusTreeErrorGroup], errors: ListBuffer[FocusTreeErrorGroup]): Unit =
    listView.setItems(getFocusTreeListOrDefaultMessage(errors))

  private def getFocusTreeListOrDefaultMessage(listBuffer: ListBuffer[FocusTreeErrorGroup], message: String = "No Problems Found") =
    listBuffer match
      case null | Nil => ListBuffer(new FocusTreeErrorGroup("No errors", ListBuffer.empty)).toObservableList
      case _ => listBuffer.toObservableList

  private def setupAlternatingFocusTreeListView(listView: ListView[FocusTreeErrorGroup]): Unit =
    listView.setCellFactory: (_: ListView[FocusTreeErrorGroup]) =>
      new ListCell[FocusTreeErrorGroup]:
        private val label = new Label()
        setWrapText(true)
        label.setWrapText(true)
        setGraphic(label)

        override def updateItem(item: FocusTreeErrorGroup, empty: Boolean): Unit =
          super.updateItem(item, empty)
          if empty || item == null then
            label.setText(null)
            setStyle("")
          else
            label.setText(item.toString)
            val isEven = getIndex % 2 == 0
            if HOIIVUtils.get("theme").equals("dark") then
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