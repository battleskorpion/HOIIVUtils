package com.hoi4utils.ui.menus

import com.hoi4utils.databases.effect.EffectDatabase.effectErrors
import com.hoi4utils.extensions.*
import com.hoi4utils.hoi4mod.common.idea.IdeaFile.ideaFileErrors
import com.hoi4utils.hoi4mod.common.national_focus.FocusTrees.focusTreeErrors
import com.hoi4utils.hoi4mod.gfx.Interface.interfaceErrors
import com.hoi4utils.hoi4mod.history.countries.CountryFile.countryErrors
import com.hoi4utils.hoi4mod.localization.LocalizationManager.localizationErrors
import com.hoi4utils.hoi4mod.map.resource.Resource.resourceErrors
import com.hoi4utils.hoi4mod.map.state.State.stateErrors
import com.hoi4utils.main.HOIIVUtils
import com.hoi4utils.ui.custom_javafx.controller.{HOIIVUtilsAbstractController, HOIIVUtilsAbstractController2}
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
  @FXML var mClose: Button = uninitialized
  @FXML var mSquare: Button = uninitialized
  @FXML var mMinimize: Button = uninitialized

  @FXML var effectsEL: ListView[String] = uninitialized
  @FXML var localizationEL: ListView[String] = uninitialized
  @FXML var interfaceEL: ListView[String] = uninitialized
  @FXML var countryEL: ListView[String] = uninitialized
  @FXML var focusTreeEL: ListView[String] = uninitialized
  @FXML var ideaEL: ListView[String] = uninitialized
  @FXML var resourceEL: ListView[String] = uninitialized
  @FXML var stateEL: ListView[String] = uninitialized
  @FXML var statesThatChangedList: ListView[String] = uninitialized

  val testList: ListBuffer[String] = ListBuffer.empty[String]

  def initialize(): Unit = update()

  override def preSetup(): Unit = setupWindowControls(contentContainer, mClose, mSquare, mMinimize, errorListTabPane)

  private def update(): Unit =
    val listViewsWithErrors = ListBuffer(
      (effectsEL, effectErrors),
      (localizationEL, localizationErrors),
      (interfaceEL, interfaceErrors),
      (countryEL, countryErrors),
      (focusTreeEL, focusTreeErrors),
      (ideaEL, ideaFileErrors),
      (resourceEL, resourceErrors),
      (stateEL, stateErrors)
    )

    listViewsWithErrors.foreach((listView, errors) => setListViewItems(listView, errors)) 
    statesThatChangedList.setItems(getListOrDefaultMessage(null, "No States Changed")) // null is statesThatChanged
    listViewsWithErrors.addOne(statesThatChangedList, null) // null is statesThatChanged
    listViewsWithErrors.map(_._1).foreach(setupAlternatingListView)

  private def setListViewItems(listView: ListView[String], errors: ListBuffer[String]): Unit =
    listView.setItems(getListOrDefaultMessage(errors))

  private def getListOrDefaultMessage(listBuffer: ListBuffer[String], message: String = "No Problems Found") =
    listBuffer match
      case null | Nil => ListBuffer(message).toObservableList
      case _ => listBuffer.toObservableList

  private def setupAlternatingListView(listView: ListView[String]): Unit =
    listView.setCellFactory: (_: ListView[String]) =>
      new ListCell[String]:
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