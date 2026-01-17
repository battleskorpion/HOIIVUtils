package com.hoi4utils.ui.javafx.application

import javax.swing.JOptionPane

trait RootWindows extends HOIIVUtilsAbstractController2:
  override def setCloseButtonAction(): Unit =
    if closeButton != null then closeButton.setOnAction: _ =>
      try
        System.err.println("test1")
        Option(JOptionPane.getRootFrame).foreach(_.dispose())
        System.err.println("test2")
        javafx.application.Platform.exit()
        logger.debug("Application exited.")
      catch case e: Exception =>
        logger.error("Error during application shutdown", e)
        System.exit(1)
    else logger.warn("mClose button is null!")
