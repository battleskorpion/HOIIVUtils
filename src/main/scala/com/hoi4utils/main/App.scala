package com.hoi4utils.main

import com.hoi4utils.ui.menus.MenuController

class App extends javafx.application.Application:
  override def start(primaryStage: javafx.stage.Stage): Unit =
    MenuController.setProgramStartTime(System.nanoTime())
    val controller = new MenuController
    controller.primaryStage = primaryStage
    controller.open()