package com.hoi4utils.ui

import com.hoi4utils.StateFilesWatcher.statesThatChanged
import com.hoi4utils.extensions._
import com.typesafe.scalalogging.LazyLogging
import javafx.fxml.FXML
import javafx.scene.control.ListView

import scala.collection.mutable.ListBuffer
import scala.compiletime.uninitialized

class LBReaderController extends HOIIVUtilsAbstractController with LazyLogging {
  setTitle("LB Reader")
  setFxmlResource("LBReader.fxml")

  @FXML var statesThatChangedList: ListView[String] = uninitialized
  @FXML var testUI: ListView[String] = uninitialized

  val testList: ListBuffer[String] = ListBuffer.empty[String]

  def initialize(): Unit = {
    logger.info("LBReaderController initialized")
    statesThatChangedList.setItems(statesThatChanged.toObservableList)
    testList.addOne("Test item 1")
    testList.addOne("Test item 2")
    testList.addOne("Test item 3")
    testUI.setItems(testList.toObservableList)
  }
}
