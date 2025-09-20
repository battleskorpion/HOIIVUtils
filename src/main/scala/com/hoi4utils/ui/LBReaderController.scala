package com.hoi4utils.ui

import com.hoi4utils.HOIIVUtils
//import com.hoi4utils.StateFilesWatcher.statesThatChanged
import com.hoi4utils.extensions.*
import com.hoi4utils.hoi4.effect.EffectDatabase.unrecognizedEffects
import com.hoi4utils.gfx.Interface.interfaceErrors 
//import com.hoi4utils.hoi4.country.Country.countryErrors // todo re add errors or change to different lists
import com.hoi4utils.hoi4.focus.FocusTree.focusTreeFileErrors
//import com.hoi4utils.hoi4.idea.Idea.ideaErrors
//import com.hoi4utils.hoi4.idea.IdeaFile.ideaFileErrors
import com.hoi4utils.localization.LocalizationManager.localizationErrors
//import com.map.Province.provinceErrors
//import com.map.Resource.resourceErrors
//import com.map.ResourcesFile.resourcesFileErrors
//import com.map.State.stateErrors
//import com.map.StateCategories.stateCategoriesErrors
//import com.map.StrategicRegion.strategicRegionErrors
//import com.map.VictoryPoint.victoryPointErrors
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
  @FXML var localizationErrorsList: ListView[String] = uninitialized
  @FXML var interfaceErrorsList: ListView[String] = uninitialized
  @FXML var countryErrorsList: ListView[String] = uninitialized
  @FXML var focusErrorsList: ListView[String] = uninitialized
  @FXML var focusTreeErrorsList: ListView[String] = uninitialized
  @FXML var ideaErrorsList: ListView[String] = uninitialized
  @FXML var ideaFileErrorsList: ListView[String] = uninitialized
  @FXML var provinceErrorsList: ListView[String] = uninitialized
  @FXML var resourceErrorsList: ListView[String] = uninitialized
  @FXML var resourcesFileErrorsList: ListView[String] = uninitialized
  @FXML var resourcesErrorsList: ListView[String] = uninitialized
  @FXML var stateErrorsList: ListView[String] = uninitialized
  @FXML var stateCategoriesErrorsList: ListView[String] = uninitialized
  @FXML var strategicRegionErrorsList: ListView[String] = uninitialized
  @FXML var victoryPointErrorsList: ListView[String] = uninitialized
  @FXML var statesThatChangedList: ListView[String] = uninitialized

  val testList: ListBuffer[String] = ListBuffer.empty[String]

  def initialize(): Unit = {
    update()
  }

  private def update(): Unit = {
    val listViewsWithErrors = ListBuffer(
      (parserFileErrorsList, unrecognizedEffects),
      (localizationErrorsList, localizationErrors),
      (interfaceErrorsList, interfaceErrors),
//      (countryErrorsList, countryErrors),  // todo: re add errors or change to different lists
//      (focusErrorsList, focusErrors),
//      (focusTreeErrorsList, focusTreeFileErrors),
//      (ideaErrorsList, ideaErrors),
//      (ideaFileErrorsList, ideaFileErrors),
//      (provinceErrorsList, provinceErrors),
//      (resourceErrorsList, resourceErrors),
//      (resourcesFileErrorsList, resourcesFileErrors),
//      (stateErrorsList, stateErrors),
//      (stateCategoriesErrorsList, stateCategoriesErrors),
//      (strategicRegionErrorsList, strategicRegionErrors),
//      (victoryPointErrorsList, victoryPointErrors)
    )

    // todo: re add errors or change to different lists
    listViewsWithErrors.foreach((listView, errors) => setListViewItems(listView, errors)) 
    statesThatChangedList.setItems(getListOrDefaultMessage(null, "No States Changed")) // null is statesThatChanged
    listViewsWithErrors.addOne(statesThatChangedList, null) // null is statesThatChanged
    listViewsWithErrors.map(_._1).foreach(setupAlternatingListView)
  }

  private def setListViewItems(listView: ListView[String], errors: ListBuffer[String]): Unit =
    listView.setItems(getListOrDefaultMessage(errors))

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
