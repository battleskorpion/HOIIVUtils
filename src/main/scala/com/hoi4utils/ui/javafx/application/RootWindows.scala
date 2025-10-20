package com.hoi4utils.ui.javafx.application

import javax.swing.JOptionPane

trait RootWindows extends HOIIVUtilsAbstractController2:
  override def setCloseButtonAction(): Unit =
    if closeButton != null then closeButton.setOnAction: _ =>
      try
        Option(JOptionPane.getRootFrame).foreach(_.dispose())
        System.exit(0)
      catch case e: Exception =>
        logger.error("Error during application shutdown", e)
        System.exit(1)
    else logger.warn("mClose button is null!")