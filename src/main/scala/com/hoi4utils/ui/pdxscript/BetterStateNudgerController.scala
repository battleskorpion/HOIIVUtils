package com.hoi4utils.ui.pdxscript

import com.hoi4utils.ui.HOIIVUtilsAbstractController
import com.typesafe.scalalogging.LazyLogging
import javafx.fxml.FXML

class BetterStateNudgerController extends HOIIVUtilsAbstractController with LazyLogging {
  setFxmlResource("BetterStateNudger.fxml")
  setTitle("HOIIVUtils Better Map Editor")

  def initialize(): Unit = {
    logger.info("BetterStateNudger initialized")
  }
}