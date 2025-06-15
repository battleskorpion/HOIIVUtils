package com.hoi4utils.extensions

import javafx.collections.{FXCollections, ObservableList}

import scala.collection.mutable.ListBuffer

extension [T](buffer: ListBuffer[T])
  def toObservableList: ObservableList[T] =
    val observableList: ObservableList[T] = FXCollections.observableArrayList[T]()
    buffer.foreach(observableList.add)
    observableList
