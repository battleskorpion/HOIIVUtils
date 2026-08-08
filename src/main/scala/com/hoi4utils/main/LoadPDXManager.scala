package com.hoi4utils.main

import com.hoi4utils.databases.modifier.ModifierDatabase
import com.hoi4utils.file.file_listener.{FileAdapter, FileEvent, FileWatcher}
import com.hoi4utils.hoi42.common.country_tags.{CountryTag, CountryTagService}
import com.hoi4utils.hoi42.common.national_focus.{FocusTree, FocusTreeService, SharedFocus}
import com.hoi4utils.hoi42.gfx.{Interface, InterfaceService}
import com.hoi4utils.hoi42.history.countries.CountryFile
import com.hoi4utils.hoi42.history.countries.service.CountryService
import com.hoi4utils.hoi4.localization.service.{LocalizationFileService, LocalizationService}
import com.hoi4utils.hoi42.map.resource.Resource
import com.hoi4utils.hoi42.map.state.State
import com.hoi4utils.hoi42.map.state.service.StateService
import com.hoi4utils.main.HOIIVFiles
import com.hoi4utils.main.HOIIVUtils.{AppPDXEnv, validateEnv}
import com.hoi4utils.script2.{PDXReadable, Registry}
import com.hoi4utils.ui.menus.MenuController
import com.typesafe.scalalogging.LazyLogging
import javafx.scene.control.Label
import zio.{RIO, Reloadable, Tag, Task, UIO, Unsafe, ZIO, ZLayer}

import java.awt.EventQueue
import java.beans.PropertyChangeListener
import java.io.File
import java.util.Properties
import scala.collection.mutable.ListBuffer
import scala.collection.parallel.CollectionConverters.*

/**
 * Loads in the mod and hoi4 together
 *
 * TODO: @Skorp Update the ChangeNotifier and FileWatcher or delete this todo if working as intended
 */
class LoadPDXManager extends LazyLogging:

//  /* LOAD ORDER IMPORTANT (depending on the class) */
//  val pdxList: List[List[PDXReadable]] = List(
//    List(Interface),
//    List(CountryTag, IdeasManager),             // , FocusTreeManager
//    List(ResourcesFile, State, CountryFile),
//  )

  /**
   * Generates the load order list by applying the given 'action' to every service.
   * @param action A function that takes a PDXReadable service and returns a Task (Boolean, Unit, etc.)
   */
  def applyToPDXServices[A](action: PDXReadable[?] => Task[A]): List[List[ZIO[AppPDXEnv, Throwable, A]]] = {
    // This is the trick: Create a local "partial" version of the helper
    // that already knows about 'action' and 'A'.
    def step[T <: PDXReadable[?] : Tag]: ZIO[T, Throwable, A] =
      callOnZIOService[T, A](action)

//    List(
//      List(step[InterfaceService]),
//      List(step[CountryTagService], step[IdeasManager], step[FocusTreeManager]),
//      List(step[ResourcesFileService], step[StateService], step[CountryService])
//    )
    List(
      List(step[InterfaceService]),
      List(step[CountryTagService], step[FocusTreeService]),  // todo ideasservice?
      List(step[StateService], step[CountryService])  // todo resources file service?
    )
  }

  // Helper: T is the service type, R is the return type of the method
  def callOnZIOService[T : zio.Tag, A](fn: T => RIO[Any, A]): ZIO[T, Throwable, A] =
    ZIO.serviceWithZIO[T](fn)

  /**
   * Loads all HOI4 and mod data with optional timing callbacks for performance monitoring.
   *
   * Components are loaded in this order:
   * 1. ModifierDatabase - Effect modifier definitions
   * 2. EffectDatabase - Effect system initialization
   * 3. Paths - HOI4 and mod directory validation
   * 4. Localization - Text/translation files
   * 5. Interface - UI definitions
   * 6. Resources - Resource definitions
   * 7. State - Map state files
   * 8. Country - Country history files
   * 9. CountryTag - Country tag definitions
   * 10. Ideas - National ideas/spirits
   * 11. FocusTrees - National focus trees
   * 8. FocusTrees - National focus trees
   * 9. Resources - Resource definitions
   * 10. State - Map state files
   * 11. Country - Country history files
   *
   * @param hProperties Configuration properties containing paths and settings
   * @param loadingLabel JavaFX label to update with loading status messages
   * @param isCancelled Callback to check if loading should be cancelled
   * @param onComponentComplete Callback invoked when a component finishes loading, receives (componentName, loadTimeSeconds)
   * @param onComponentStart Callback invoked when a component begins loading, receives componentName
   */
  def load(
//            hProperties: Properties,
            loadingLabel: Label,
            isCancelled: () => Boolean = () => false,
            onComponentComplete: (String, Long) => Unit = (_, _) => (),
            onComponentStart: String => Unit = _ => ()
          ): RIO[LocalizationService & Config & AppPDXEnv, Unit] = {
    for {
      localizationService <- ZIO.service[LocalizationService]
      config <- ZIO.service[Config]
      interfaceService <- ZIO.service[InterfaceService]
      countryTagService <- ZIO.service[CountryTagService]
      //            ideasManager <- ZIO.service[IdeasManager]
      focusTreeService <- ZIO.service[FocusTreeService]
      //            resourcesFileService <- ZIO.service[ResourcesFileService]
      stateService <- ZIO.service[StateService]
      countryService <- ZIO.service[CountryService]

      hProperties = config.getProperties
      sharedFocusRegistry = focusTreeService.sharedPseudoSharedFocusTree
      given Registry[SharedFocus] = sharedFocusRegistry

      // database
      _ <- ZIO.unless(isCancelled()) {
        ZIO.attempt {
          implicit val properties: Properties = hProperties
          implicit val label: Label = loadingLabel
          startDatabase(loadingLabel, onComponentStart, onComponentComplete)
        }
      }
      _ <- ZIO.unless(isCancelled()) {
        ZIO.attempt {
          implicit val properties: Properties = hProperties
          implicit val label: Label = loadingLabel
          startDatabase1(loadingLabel, onComponentStart, onComponentComplete)
        }
      }
      // paths
      _ <- ZIO.unless(isCancelled()) {
        ZIO.attempt {
          MenuController.updateLoadingStatus(loadingLabel, "Finding Paths...")
          val hoi4Path = hProperties.getProperty("hoi4.path")
          val modPath = hProperties.getProperty("mod.path")

          if validateDirectoryPath(hoi4Path, "hoi4.path") && validateDirectoryPath(modPath, "mod.path") then
            HOIIVFiles.setHoi4PathChildDirs(hoi4Path)
            HOIIVFiles.setModPathChildDirs(modPath)
            hProperties.setProperty("valid.HOIIVFilePaths", "true")
          else
            logger.error("Failed to create HOIIV file paths")
            hProperties.setProperty("valid.HOIIVFilePaths", "false")
        }
      }

      // Localization Reload
      _ <- ZIO.unless(isCancelled()) {
        for {
          _ <- ZIO.attempt {
            onComponentStart("Localization")
            MenuController.updateLoadingStatus(loadingLabel, "Loading Localization...")
          }
          timedResult <- localizationService.reload().timed
          (duration, _) = timedResult
          _ <- ZIO.attempt {
            onComponentComplete("Localization", duration.toNanos)
          }
        } yield ()
      }

      // Interface Reload
      _ <- ZIO.unless(isCancelled()) {
        reloadService(loadingLabel, interfaceService, isCancelled, onComponentStart, onComponentComplete)
      }

      // parallel 1
      _ <- ZIO.unless(isCancelled()) {
        reloadService(loadingLabel, countryTagService, isCancelled, onComponentStart, onComponentComplete) <&>
        // todo
//          reloadService(loadingLabel, ideasManager, isCancelled, onComponentStart, onComponentComplete) <&>
        reloadService(loadingLabel, focusTreeService, isCancelled, onComponentStart, onComponentComplete)
      }

      // parallel 2
      _ <- ZIO.unless(isCancelled()) {
        // todo
        //            reloadService(loadingLabel, resourcesFileService, isCancelled, onComponentStart, onComponentComplete) <&>
        reloadService(loadingLabel, stateService, isCancelled, onComponentStart, onComponentComplete) <&>
        reloadService(loadingLabel, countryService, isCancelled, onComponentStart, onComponentComplete)
      }
    } yield ()
  }

  private def startDatabase1(loadingLabel: Label, onComponentStart: String => Unit = _ => (),
                             onComponentComplete: (String, Long) => Unit = (_, _) => ()
                            ): Unit = {
    val startTime = System.nanoTime()
    onComponentStart("EffectDatabase")
    MenuController.updateLoadingStatus(loadingLabel, "Initializing EffectDatabase...")
    //          EffectDatabase.init() // todo
    onComponentComplete("EffectDatabase", System.nanoTime() - startTime)
  }

  private def startDatabase(loadingLabel: Label, onComponentStart: String => Unit = _ => (),
                            onComponentComplete: (String, Long) => Unit = (_, _) => ()
                           ): Unit = {
    val startTime = System.nanoTime()
    onComponentStart("ModifierDatabase")
    MenuController.updateLoadingStatus(loadingLabel, "Initializing ModifierDatabase...")
    ModifierDatabase.init()
    onComponentComplete("ModifierDatabase", System.nanoTime() - startTime)
  }

  private def reloadService[R](loadingLabel: Label,
                            service: PDXReadable[R],
                            isCancelled: () => Boolean = () => false,
                            onComponentStart: String => Unit = _ => (),
                            onComponentComplete: (String, Long) => Unit = (_, _) => ()) = {
    ZIO.unlessZIO(ZIO.succeed(isCancelled()))(
      ZIO.attempt {
        onComponentStart(service.display)
        MenuController.updateLoadingStatus(loadingLabel, s"Loading ${service.display}...")
      } *> {
        for
          timedResult <- service.read().timed
          (duration, _) = timedResult
          _ <- ZIO.attempt {
            onComponentComplete(service.display, duration.toNanos)
          }
        yield ()
      }
    )
  }

  def readPDX(pdx: PDXReadable[?], isCancelled: () => Boolean = () => false)(implicit properties: Properties, label: Label): Task[Unit] =
    val property = s"valid.${pdx.display}"

    MenuController.updateLoadingStatus(label, s"Loading ${pdx.display} files...")
    ZIO.succeed {
      if isCancelled() then ()
      else
        try {
          ZIO.ifZIO(pdx.read())(
            onTrue = ZIO.succeed(properties.setProperty(property, "true")),
            onFalse = ZIO.succeed {
              properties.setProperty(property, "false")
              logger.error(s"Exception while reading for ${pdx.display}")
            }
          )
        } catch
          case e: Exception =>
            properties.setProperty(property, "false")
            logger.error(s"Exception while reading for ${pdx.display}", e)
    }

  /** Validates whether the provided directory path is valid */
  private def validateDirectoryPath(path: String, keyName: String): Boolean =
    if path == null || path.isEmpty then
      logger.error("{} is null or empty!", keyName)
      return false
    val directory = new File(path)
    if !directory.exists || !directory.isDirectory then
      logger.error("{} does not point to a valid directory: {}", keyName, path)
      return false
    true

  /** Clears loaded PDX data. */
  def clearPDX(): Unit =
    applyToPDXServices(_.clear())
//    pdxList.foreach(_.foreach(_.clear()))

  def clearLB(): RIO[LocalizationFileService & AppPDXEnv, Unit] = {
    for {
      locFileService <- ZIO.service[LocalizationFileService]
      interfaceService <- ZIO.service[InterfaceService]
      focusTreeManager <- ZIO.service[FocusTreeService]
//      ideasManager <- ZIO.service[IdeasManager]
      stateService <- ZIO.service[StateService]
      countryService <- ZIO.service[CountryService]
//      errorsList = ListBuffer(
//        effectErrors,
//        //      localizationErrors,   // TODO
//        interfaceService.interfaceErrors,
//        countryService.countryErrors,
//        focusTreeManager.focusTreeErrors,
//        ideasManager.ideaFileErrors,
//        resourceErrors,
//        stateService.stateErrors
//      )
//      _ <- ZIO.succeed(errorsList.foreach(_.clear()))
    } yield ()
  }

  def closeDB(): Unit =
    ModifierDatabase.close()
//    EffectDatabase.close()  // todo
