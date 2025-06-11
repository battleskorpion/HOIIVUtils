package com.hoi4utils.extensions

import javafx.collections.{FXCollections, ObservableList}
import scala.collection.mutable.ListBuffer

extension (buffer: ListBuffer[String])
  def toObservableList: ObservableList[String] =
    val observableList: ObservableList[String] = FXCollections.observableArrayList()
    buffer.foreach(observableList.add)
    observableList
