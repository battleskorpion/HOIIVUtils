package com.hoi4utils.main

import com.hoi4utils.ui.menus.MenuController
import zio.{Unsafe, ZIO}

class App extends javafx.application.Application:
  override def start(primaryStage: javafx.stage.Stage): Unit = {
    // You can now run ZIO effects here if needed
    Unsafe.unsafe { implicit unsafe =>
      HOIIVUtils.runtime.unsafe.run(
        initAppTimer() *> launchJavaFXMenu(primaryStage)
      )
    }
  }

  def initAppTimer(): zio.Task[Unit] =
    ZIO.succeed(MenuController.setProgramStartTime(System.nanoTime()))

  def launchJavaFXMenu(primaryStage: javafx.stage.Stage): zio.Task[Unit] = ZIO.attempt {
    val controller = new MenuController
    controller.primaryStage = primaryStage
    controller.open()
  }
