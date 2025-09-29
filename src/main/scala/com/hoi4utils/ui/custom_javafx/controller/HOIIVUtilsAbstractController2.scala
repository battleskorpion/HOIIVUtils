package com.hoi4utils.ui.custom_javafx.controller

import com.hoi4utils.main.HOIIVUtils.config
import com.hoi4utils.main.{HOIIVUtils, Version}
import com.hoi4utils.ui.custom_javafx.controller.HOIIVUtilsAbstractController
import com.typesafe.scalalogging.LazyLogging
import javafx.fxml.FXMLLoader
import javafx.scene.control.Button
import javafx.scene.image.Image
import javafx.scene.layout.GridPane
import javafx.scene.{Parent, Scene}
import javafx.stage.Stage

import java.io.IOException
import javax.swing.JOptionPane
import scala.compiletime.uninitialized

abstract class HOIIVUtilsAbstractController2 extends HOIIVUtilsAbstractController with LazyLogging:
  var primaryStage: Stage = uninitialized
  var fxml: FXMLLoader = uninitialized
  var xOffset: Double = 0
  var yOffset: Double = 0

  override def open(): Unit =
    val stage = new Stage
    primaryStage = stage
    primaryStage.getIcons.addAll(
      new Image(getClass.getResourceAsStream("/icons/settings-icon-gray-gear16.png")),
      new Image(getClass.getResourceAsStream("/icons/settings-icon-gray-gear32.png")),
      new Image(getClass.getResourceAsStream("/icons/settings-icon-gray-gear48.png")),
      new Image(getClass.getResourceAsStream("/icons/settings-icon-gray-gear64.png")),
      new Image(getClass.getResourceAsStream("/icons/settings-icon-gray-gear128.png"))
    )

    try
      /* fxml */
      if getClass.getResource(fxmlResource) == null then throw new IllegalArgumentException(s"FXML resource not found: $fxmlResource")
      fxml = new FXMLLoader(getClass.getResource(fxmlResource))
      fxmlSetController()
      val root = fxml.load[Parent]()
      if root == null then throw new IllegalStateException(s"Failed to load FXML root from: $fxmlResource")
      val scene = new Scene(root)

      /* theme */
      val cssPath = Option(HOIIVUtils.get("theme")).getOrElse("light") match
        case "dark" => "/com/hoi4utils/ui/css/javafx_dark.css"
        case _ => "/com/hoi4utils/ui/css/highlight-background.css"

      Option(getClass.getResource(cssPath)) match
        case Some(_) =>
          scene.getStylesheets.add(cssPath)
        case None =>
          logger.warn(s"CSS file not found: $cssPath, continuing without theme")

      primaryStage.setScene(scene)
      primaryStage.setTitle(title)
      decideScreen(stage)
      preSetup()
      primaryStage.show()
    catch
      case e: IOException =>
        val errorMsg = s"IO Error loading FXML resource: $fxmlResource"
        logger.error(s"version: ${Version.getVersion(config.getProperties)} $errorMsg", e)
        JOptionPane.showMessageDialog(
          null,
          s"version: ${Version.getVersion(config.getProperties)} $errorMsg\nCheck if the file exists and is accessible.\nPlease go to our Discord for help.",
          "File Loading Error",
          JOptionPane.ERROR_MESSAGE
        )
        System.exit(1)

      case e: IllegalArgumentException =>
        val errorMsg = s"Invalid FXML resource: $fxmlResource"
        logger.error(s"version: ${Version.getVersion(config.getProperties)} $errorMsg", e)
        JOptionPane.showMessageDialog(
          null,
          s"version: ${Version.getVersion(config.getProperties)} $errorMsg\nThe specified resource path is invalid.\nPlease go to our Discord for help.",
          "Configuration Error",
          JOptionPane.ERROR_MESSAGE
        )
        System.exit(1)

      case e: IllegalStateException =>
        val errorMsg = s"Failed to initialize UI components from: $fxmlResource"
        logger.error(s"version: ${Version.getVersion(config.getProperties)} $errorMsg", e)
        JOptionPane.showMessageDialog(
          null,
          s"version: ${Version.getVersion(config.getProperties)} $errorMsg\nThe FXML file may be corrupted or invalid.\nPlease go to our Discord for help.",
          "UI Initialization Error",
          JOptionPane.ERROR_MESSAGE
        )
        System.exit(1)

      case e: Exception =>
        val errorMsg = s"Unexpected error during application startup"
        logger.error(s"version: ${Version.getVersion(config.getProperties)} $errorMsg with FXML: $fxmlResource", e)
        JOptionPane.showMessageDialog(
          null,
          s"version: ${Version.getVersion(config.getProperties)} $errorMsg\nError: ${e.getClass.getSimpleName}: ${Option(e.getMessage).getOrElse("Unknown error")}\nPlease go to our Discord for help.",
          "Startup Error",
          JOptionPane.ERROR_MESSAGE
        )
        System.exit(1)

  def fxmlSetController(): Unit = throw new RuntimeException("fxmlSetController() not implemented in subclass of HOIIVUtilsAbstractController2")

  def preSetup(): Unit = throw new RuntimeException("preSetup() not implemented in subclass of HOIIVUtilsAbstractController2")

  // Setup window controls AFTER primaryStage is available
  def setupWindowControls(container: GridPane, close: Button, square: Button, minimize: Button): Unit =
    // Verify all components are available
    if container == null then
      logger.error("aContentContainer is null - FXML injection failed!")
    else if primaryStage == null then
      logger.error("primaryStage is null - called too early!")
    else
      // Setup window dragging
      container.setOnMousePressed: event =>
        if event != null then
          xOffset = event.getSceneX
          yOffset = event.getSceneY

      container.setOnMouseDragged: event =>
        if event != null && primaryStage != null then
          val newX = event.getScreenX - xOffset
          val newY = event.getScreenY - yOffset
          primaryStage.setX(newX)
          primaryStage.setY(newY)

      // Setup window control buttons
      if close != null then close.setOnAction: _ =>
          try
            Option(JOptionPane.getRootFrame).foreach(_.dispose())
            System.exit(0)
          catch case e: Exception => 
              logger.error("Error during application shutdown", e)
              System.exit(1)
      else logger.warn("mClose button is null!")

      if square != null then square.setOnAction: _ =>
          try if primaryStage != null then primaryStage.setMaximized(!primaryStage.isMaximized)
          catch case e: Exception => logger.error("Error toggling window maximized state", e)
      else logger.warn("mSquare button is null!")

      if minimize != null then minimize.setOnAction: _ =>
          try if primaryStage != null then primaryStage.setIconified(true)
          catch case e: Exception => logger.error("Error minimizing window", e)
      else logger.warn("mMinimize button is null!")