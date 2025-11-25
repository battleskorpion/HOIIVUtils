package com.hoi4utils.ui.javafx.application

import com.hoi4utils.internal.UTF8ResourceBundleControl
import com.hoi4utils.main.HOIIVUtils.*
import com.hoi4utils.main.{HOIIVUtils, Initializer, Version}
import com.hoi4utils.ui.javafx.application.HOIIVUtilsAbstractController
import com.typesafe.scalalogging.LazyLogging
import javafx.application.Platform
import javafx.fxml.{FXML, FXMLLoader}
import javafx.scene.control.Button
import javafx.scene.image.Image
import javafx.scene.layout.{GridPane, Pane}
import javafx.scene.{Parent, Scene}
import javafx.stage.{Stage, StageStyle}

import java.io.IOException
import java.util.{Locale, MissingResourceException, ResourceBundle}
import javax.swing.JOptionPane
import scala.compiletime.uninitialized

abstract class HOIIVUtilsAbstractController2 extends HOIIVUtilsAbstractController with JavaFXUIManager2 with LazyLogging:
  var primaryStage: Stage = uninitialized
  var primaryScene: Scene = uninitialized
  protected var fxmlLoader: FXMLLoader = uninitialized
  protected var xOffset: Double = 0
  protected var yOffset: Double = 0
  protected var isEmbedded: Boolean = false

  // Generic window control buttons - use these fx:ids in all FXMLs
  @FXML protected var closeButton: Button = uninitialized
  @FXML protected var minimizeButton: Button = uninitialized
  @FXML protected var maximizeButton: Button = uninitialized

  override def open(): Unit =
    if primaryStage == null then
      primaryStage = new Stage
    primaryStage.getIcons.addAll(icons)
    primaryStage.initStyle(StageStyle.UNDECORATED)
    try
      /* fxml */
      val fxmlResource = getClass.getResource(fxmlFile)
      if fxmlResource == null then throw new IllegalArgumentException(s"FXML file not found: $fxmlFile")
      fxmlLoader = new FXMLLoader(fxmlResource)
      fxmlLoader.setController(this)
      fxmlSetResource()
      val root = fxmlLoader.load[Parent]()
      if root == null then throw new IllegalStateException(s"Failed to load FXML root from: $fxmlFile")
      val scene = new Scene(root)
      primaryScene = scene

      /* theme */
      val cssPath = Option(HOIIVUtils.get("theme")).getOrElse("light") match
        case "dark" => "/com/hoi4utils/ui/css/javafx_dark.css"
        case _ => "/com/hoi4utils/ui/css/highlight-background.css"

      Option(getClass.getResource(cssPath)) match
        case Some(_) => scene.getStylesheets.add(cssPath)
        case None => logger.warn(s"CSS file not found: $cssPath, continuing without theme")

      primaryStage.setScene(scene)
      primaryStage.setTitle(title)
      decideScreen(primaryStage)
      preSetup()
      ResizeHelper.addResizeListener(primaryStage)
      primaryStage.show()
    catch
      case e: IOException => handleJavaFXControllerIOError(e)
      case e: IllegalArgumentException => handleJavaFXControllerIllegalArgumentError(e)
      case e: IllegalStateException => handleJavaFXControllerIllegalStateError(e)
      case e: Exception => handleJavaFXControllerError(e)

  private def version =
    new Initializer().initialize(getConfig)
    try Version.getVersion(getConfig.getProperties)
    catch case e: Exception =>
      logger.error("Failed to get application version", e)
      Version.DEFAULT

  protected def fxmlSetResource(): Unit = ()

  protected def setWindowControlsVisibility(): Unit =
    Platform.runLater(() =>
      isEmbedded = primaryScene == null
      Seq(closeButton, minimizeButton, maximizeButton).foreach(b => if b != null then b.setVisible(!isEmbedded))
    )

  protected def preSetup(): Unit = ()

  protected def setupWindowControls(container: Pane, additionalDraggableNodes: javafx.scene.Node*): Unit =
    setupWindowDrag(container, additionalDraggableNodes*)

    // Made a method to be overrided by RootWindows
    setCloseButtonAction()

    if maximizeButton != null then
      maximizeButton.setOnAction: _ =>
        try if primaryStage != null then primaryStage.setMaximized(!primaryStage.isMaximized)
        catch case e: Exception => logger.error("Error toggling window maximized state", e)
    else
      logger.warn("maximizeButton is null - fx:id='maximizeButton' not found in FXML")

    // Minimize button
    if minimizeButton != null then
      minimizeButton.setOnAction: _ =>
        try if primaryStage != null then primaryStage.setIconified(true)
        catch case e: Exception => logger.error("Error minimizing window", e)
    else
      logger.warn("minimizeButton is null - fx:id='minimizeButton' not found in FXML")

  private def setupWindowDrag(container: Pane, additionalDraggableNodes: javafx.scene.Node*): Unit =
    // Verify all components are available
    if container == null then
      logger.error("Container is null - FXML injection failed!")
    else if primaryStage == null then
      logger.error("primaryStage is null - called too early!")
    else
      // Setup window dragging for container and additional nodes
      val allDraggableNodes = container +: additionalDraggableNodes

      allDraggableNodes.foreach: node =>
        if node != null then
          node.setOnMousePressed: event =>
            if event != null then
              xOffset = event.getSceneX
              yOffset = event.getSceneY

          node.setOnMouseDragged: event =>
            if event != null && primaryStage != null then
              val newX = event.getScreenX - xOffset
              val newY = event.getScreenY - yOffset
              primaryStage.setX(newX)
              primaryStage.setY(newY)

  protected def setCloseButtonAction(): Unit = {
    if closeButton != null then
      closeButton.setOnAction(_ => primaryStage.close())
    else
      logger.warn("closeButton is null - fx:id='closeButton' not found in FXML")
  }

  protected def getResourceBundle(resourceFileName: String): ResourceBundle =
    val utf8Control = UTF8ResourceBundleControl()
    val resourceBundle: ResourceBundle =
//      val currentLocale = new Locale("ru") // Russian locale
      val currentLocale = Locale.getDefault
      try
        val bundle = ResourceBundle.getBundle(resourceFileName, currentLocale, utf8Control)
        bundle
      catch
        case _: MissingResourceException =>
          logger.error(s"Could not find ResourceBundle for locale $currentLocale. Falling back to English.")
          val fallbackBundle = ResourceBundle.getBundle("i18n.menu", Locale.US, utf8Control)
          logger.error(s"Fallback ResourceBundle loaded: ${fallbackBundle.getLocale}")
          fallbackBundle
    if resourceBundle == null then
      logger.error("ResourceBundle is null, cannot load FXML.")
      throw new RuntimeException("ResourceBundle is null, cannot load FXML.")
    resourceBundle

  private def handleJavaFXControllerError(e: Exception): Unit =
    val errorMsg = s"Unexpected error during application startup"
    logger.error(s"version: $version $errorMsg with FXML: $fxmlFile", e)
    JOptionPane.showMessageDialog(
      null,
      s"version: $version $errorMsg\nError: ${e.getClass.getSimpleName}: ${Option(e.getMessage).getOrElse("Unknown error")}\nPlease go to our Discord for help.",
      "Startup Error",
      JOptionPane.ERROR_MESSAGE
    )
    System.exit(1)

  private def handleJavaFXControllerIllegalStateError(e: IllegalStateException): Unit =
    val errorMsg = s"Failed to initialize UI components from: $fxmlFile"
    logger.error(s"version: $version $errorMsg", e)
    JOptionPane.showMessageDialog(
      null,
      s"version: $version $errorMsg\nThe FXML file may be corrupted or invalid.\nPlease go to our Discord for help.",
      "UI Initialization Error",
      JOptionPane.ERROR_MESSAGE
    )
    System.exit(1)

  private def handleJavaFXControllerIllegalArgumentError(e: IllegalArgumentException): Unit =
    val errorMsg = s"Invalid FXML resource: $fxmlFile"
    logger.error(s"version: $version $errorMsg", e)
    JOptionPane.showMessageDialog(
      null,
      s"version: $version $errorMsg\nThe specified resource path is invalid.\nPlease go to our Discord for help.",
      "Configuration Error",
      JOptionPane.ERROR_MESSAGE
    )
    System.exit(1)

  private def handleJavaFXControllerIOError(e: IOException): Unit =
    val errorMsg = s"IO Error loading FXML resource: $fxmlFile"
    logger.error(s"version: $version $errorMsg", e)
    JOptionPane.showMessageDialog(
      null,
      s"version: $version $errorMsg\nCheck if the file exists and is accessible.\nPlease go to our Discord for help.",
      "File Loading Error",
      JOptionPane.ERROR_MESSAGE
    )
    System.exit(1)
