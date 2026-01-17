package com.hoi4utils.main

import com.hoi4utils.ui.menus.MenuController
import zio.{Unsafe, ZIO}

class App extends javafx.application.Application:
  override def start(primaryStage: javafx.stage.Stage): Unit =
//    MenuController.setProgramStartTime(System.nanoTime())
//    val controller = new MenuController
//    controller.primaryStage = primaryStage
//    controller.open()

    // You can now run ZIO effects here if needed
    Unsafe.unsafe { implicit unsafe =>
      HOIIVUtils.runtime.unsafe.run(
        ZIO.succeed(MenuController.setProgramStartTime(System.nanoTime())) *>
          ZIO.attempt {
            val controller = new MenuController
            controller.primaryStage = primaryStage
            controller.open()
          }
      ).getOrThrow()
    }
