package com.hoi4utils.ui.menus

import com.hoi4utils.ui.custom_javafx.controller.HOIIVUtilsAbstractController2
import com.hoi4utils.ui.focus.FocusTree2Controller
import com.hoi4utils.ui.map.ProvinceColorsController
import com.hoi4utils.ui.tooltip.CustomTooltipController
import com.hoi4utils.ui.units.CompareUnitsController
import com.typesafe.scalalogging.LazyLogging
import javafx.fxml.FXMLLoader
import javafx.scene.layout.{Pane, StackPane}

import scala.collection.mutable

/**
 * Manages loading and switching between different detail views in the menu
 */
class DetailPanelManager(val contentPane: StackPane) extends LazyLogging:

  private val viewCache = mutable.Map[String, (Pane, Any)]()
  private var currentView: Option[String] = None

  /**
   * Clears the detail panel (shows nothing)
   */
  def clear(): Unit =
    contentPane.getChildren.clear()
    currentView = None
    logger.debug("Detail panel cleared")

  /**
   * Load and switch to a view by FXML path
   * @param fxmlPath Path to FXML file (e.g., "/com/hoi4utils/ui/menus/ErrorList.fxml")
   * @param forceReload If true, ignores cache and reloads the view
   */
  def switchToView(fxmlPath: String, forceReload: Boolean = false): Unit =
    try
      val (pane, controller) = if forceReload then
        viewCache.remove(fxmlPath)
        loadView(fxmlPath)
      else
        viewCache.getOrElseUpdate(fxmlPath, loadView(fxmlPath))

      contentPane.getChildren.clear()
      contentPane.getChildren.add(pane)
      currentView = Some(fxmlPath)

    catch
      case e: Exception =>
        logger.error(s"Failed to load view: $fxmlPath", e)
        showError(s"Failed to load view: ${e.getMessage}")

  /**
   * Load and switch to a view, with access to its controller
   * @param fxmlPath Path to FXML file
   * @param onLoad Callback with the controller
   */
  def switchToViewWithCallback[T](fxmlPath: String, onLoad: T => Unit): Unit =
    switchToViewWithCallback(fxmlPath, onLoad, forceReload = false)

  /**
   * Load and switch to a view, with access to its controller
   * @param fxmlPath Path to FXML file
   * @param onLoad Callback with the controller
   * @param forceReload If true, ignores cache and reloads the view
   */
  def switchToViewWithCallback[T](fxmlPath: String, onLoad: T => Unit, forceReload: Boolean): Unit =
    try
      val (pane, controller) = if forceReload then
        viewCache.remove(fxmlPath)
        loadView(fxmlPath)
      else
        viewCache.getOrElseUpdate(fxmlPath, loadView(fxmlPath))

      contentPane.getChildren.clear()
      contentPane.getChildren.add(pane)
      currentView = Some(fxmlPath)

      // Call the callback with the controller
      onLoad(controller.asInstanceOf[T])

      logger.info(s"Switched to view: $fxmlPath with callback")
    catch
      case e: Exception =>
        logger.error(s"Failed to load view: $fxmlPath", e)
        showError(s"Failed to load view: ${e.getMessage}")

  /**
   * Get the current view's path
   */
  def getCurrentView: Option[String] = currentView

  /**
   * Get a cached controller if available
   */
  def getController[T](fxmlPath: String): Option[T] =
    viewCache.get(fxmlPath).map(_._2.asInstanceOf[T])

  /**
   * Clear the cache for a specific view (forces reload next time)
   */
  def clearViewCache(fxmlPath: String): Unit =
    viewCache.remove(fxmlPath)
    logger.debug(s"Cleared cache for view: $fxmlPath")

  /**
   * Clear all cached views
   */
  def clearAllCaches(): Unit =
    viewCache.clear()
    logger.debug("Cleared all view caches")

  /**
   * Reload the current view (useful for refreshing data)
   */
  def reloadCurrentView(): Unit =
    currentView.foreach(path => switchToView(path, forceReload = true))

  /**
   * Internal method to load a view from FXML
   */
  private def loadView(fxmlPath: String): (Pane, Any) =
    val loader = new FXMLLoader(getClass.getResource(fxmlPath))

    // Create controller instance based on the FXML path
    val controller = fxmlPath match
      case "/com/hoi4utils/ui/focus/FocusTree2.fxml" => new FocusTree2Controller()
      case "/com/hoi4utils/ui/tooltip/CustomTooltip.fxml" => new CustomTooltipController()
      case "/com/hoi4utils/ui/units/CompareUnits.fxml" => new CompareUnitsController()
      case "/com/hoi4utils/ui/map/ProvinceColors.fxml" => new ProvinceColorsController()
      case "/com/hoi4utils/ui/menus/ErrorList.fxml" => new ErrorListController()
      case _ =>
        logger.warn(s"No specific controller mapping for: $fxmlPath")
        null

    // Set the controller if we created one
    if controller != null then
      loader.setController(controller)

    val pane = loader.load[Pane]()

    // Get controller (either the one we set, or one from fx:controller)
    val actualController = Option(loader.getController[Any]()).getOrElse(controller)

    // Manually call initialize() if it exists
//    if actualController != null then
//      try {
//        val initMethod = actualController.getClass.getMethod("initialize")
//        initMethod.invoke(actualController)
//      } catch {
//        case _: NoSuchMethodException =>
//          logger.debug(s"No initialize() method found for: $fxmlPath")
//        case e: Exception =>
//          logger.error(s"Error calling initialize() for: $fxmlPath", e)
//      }

    (pane, actualController)

  /**
   * Show an error message in the detail panel
   */
  private def showError(message: String): Unit =
    import javafx.scene.control.Label
    contentPane.getChildren.clear()
    val errorLabel = new Label(message)
    errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 14px;")
    contentPane.getChildren.add(errorLabel)