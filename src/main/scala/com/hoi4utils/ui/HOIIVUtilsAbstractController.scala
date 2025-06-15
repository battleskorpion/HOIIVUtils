package com.hoi4utils.ui

import com.hoi4utils.HOIIVUtils.config
import com.hoi4utils.{HOIIVUtils, Version}
import com.typesafe.scalalogging.LazyLogging
import javafx.fxml.FXMLLoader
import javafx.scene.{Parent, Scene}
import javafx.stage.Stage

import java.lang.reflect.InvocationTargetException
import java.util.{Arrays, HashSet}
import javax.swing.JOptionPane
import scala.collection.mutable
import scala.compiletime.uninitialized
import scala.jdk.CollectionConverters.*
import scala.util.{Failure, Success, Try}

abstract class HOIIVUtilsAbstractController extends JavaFXUIManager with LazyLogging:

  private var fxmlResource: String = uninitialized
  private var title: String = uninitialized

  /**
   * Opens the stage with the specified FXML resource and title.
   */
  override def open(): Unit =
    try
      val fxml = FXMLLoader(getClass.getResource(fxmlResource))
      open(fxml)
      logger.debug(s"$title Started")
    catch
      case e: Exception =>
        handleOpenError(e)

  /**
   * Opens the stage with optional initialization arguments.
   * This method maintains Java compatibility by accepting Object varargs.
   *
   * @param initargs the initialization arguments for the controller
   */
  @annotation.varargs
  def open(initargs: AnyRef*): Unit =
    try
      val fxml = FXMLLoader(getClass.getResource(fxmlResource))
      fxml.setControllerFactory(_ => findMatchingConstructor(initargs*))
      open(fxml)
      logger.debug(s"$title Started with arguments ${Arrays.deepToString(initargs.toArray)}")
    catch
      case e: Exception =>
        handleOpenError(e)

  /**
   * Opens the stage with the specified FXMLLoader.
   * This method is used internally to set up the scene and stage.
   *
   * @param fxml the FXMLLoader instance to load the FXML resource
   */
  def open(fxml: FXMLLoader): Unit =
    val root = fxml.load[Parent]()
    val scene = Scene(root)
    get("theme") match
      case "dark" => scene.getStylesheets.add("com/hoi4utils/ui/javafx_dark.css")
      case _ => scene.getStylesheets.add("/com/hoi4utils/ui/highlight-background.css")
    val stage = Stage()
    stage.setScene(scene)
    stage.setTitle(title)
    decideScreen(stage)
    stage.show()

  private def findMatchingConstructor(initargs: AnyRef*): AnyRef =
    val argsClassHierarchies = mutable.ArrayBuffer.empty[List[Class[?]]]

    for arg <- initargs do
      val superclassesAndInterfaces = HashSet[Class[?]]()
      var currentClass: Class[?] = arg.getClass

      while currentClass != null do
        superclassesAndInterfaces.add(currentClass)
        superclassesAndInterfaces.addAll(Arrays.asList(currentClass.getInterfaces*))
        currentClass = currentClass.getSuperclass

      findSuperinterfacesRecursively(superclassesAndInterfaces)
      argsClassHierarchies += superclassesAndInterfaces.asScala.toList

    for combination <- generateCombinations(argsClassHierarchies.toList, 0) do
      Try {
        getClass.getConstructor(combination.toArray*).newInstance(initargs.toArray*)
      } match
        case Success(instance) => return instance.asInstanceOf[AnyRef]
        case Failure(_: NoSuchMethodException | _: InstantiationException |
                     _: IllegalAccessException | _: InvocationTargetException) =>
        // Continue trying other combinations
        case Failure(e) => throw e

    throw RuntimeException(s"No suitable constructor found for arguments: ${Arrays.toString(initargs.toArray)}")

  /**
   * Recursively find all superinterfaces of the given classes and add them to the set.
   *
   * @param hierarchyAndInterfaces the set to add the superinterfaces to
   */
  private def findSuperinterfacesRecursively(hierarchyAndInterfaces: HashSet[Class[?]]): Unit =
    var prevSize = 0

    while
      prevSize = hierarchyAndInterfaces.size()

      // Collect new interfaces from existing ones
      val newInterfaces = hierarchyAndInterfaces.asScala
        .filter(_.isInterface)
        .flatMap(interf => interf.getInterfaces.toList)
        .toSet

      // Add only if new interfaces exist
      hierarchyAndInterfaces.addAll(newInterfaces.asJava)

      hierarchyAndInterfaces.size() > prevSize // Continue while new interfaces are added
    do ()

  private def generateCombinations(classHierarchies: List[List[Class[?]]], index: Int): List[List[Class[?]]] =
    if index == classHierarchies.size then
      List(List.empty[Class[?]]) // Base case: return a list with an empty list
    else
      val nextCombinations = generateCombinations(classHierarchies, index + 1)

      classHierarchies(index).flatMap { clazz =>
        nextCombinations.map { combination =>
          clazz :: combination
        }
      }

  override def setFxmlResource(fxmlResource: String): Unit =
    this.fxmlResource = fxmlResource

  override def setTitle(title: String): Unit =
    this.title = title

  private def getClassHierarchy(clazz: Class[?]): List[Class[?]] =
    val hierarchy = mutable.ArrayBuffer.empty[Class[?]]
    var currentClass: Class[?] = clazz

    while currentClass != null do
      hierarchy += currentClass
      currentClass = currentClass.getSuperclass

    hierarchy.toList

  private def handleOpenError(e: Exception): Unit =
    val errorMessage = s"version: ${Version.getVersion(config.getProperties)} Failed to open window\nError loading FXML: $fxmlResource\n${e.getMessage}"
    logger.error(s"version: ${Version.getVersion(config.getProperties)} Error loading FXML: $fxmlResource")
    JOptionPane.showMessageDialog(null, errorMessage, "Error", JOptionPane.ERROR_MESSAGE)
    throw e

  def get(prop: String): String = HOIIVUtils.get(prop)
