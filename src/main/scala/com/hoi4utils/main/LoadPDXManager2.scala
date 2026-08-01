//package com.hoi4utils.main
//
//import com.hoi4utils.databases.modifier.ModifierDatabase
//import com.hoi4utils.hoi4.localization.service.LocalizationService
//import com.hoi4utils.hoi42.common.country_tags.CountryTagService
//import com.hoi4utils.hoi42.common.national_focus.FocusTreeService
//import com.hoi4utils.hoi42.gfx.InterfaceService
//import com.hoi4utils.hoi42.history.countries.service.CountryService
//import com.hoi4utils.hoi42.map.state.service.StateService
//import com.hoi4utils.script2.PDXReadable
//import com.hoi4utils.ui.menus.MenuController
//import com.typesafe.scalalogging.LazyLogging
//import javafx.scene.control.Label
//import zio.{RIO, Tag, Task, ZIO}
//
//import java.io.File
//import java.util.Properties
//
///**
// * Loads in mod and base PDX files.
// */
//class LoadPDXManager2 extends LazyLogging:
//
//  type AppPDXEnv = InterfaceService & CountryTagService & FocusTreeService & StateService & CountryService // todo: & IdeasService? & ResourcesFileService?
//
//  /**
//   * Generates the load order list by applying the given 'action' to every service.
//   * @param action A function that takes a PDXReadable service and returns a Task (Boolean, Unit, etc.)
//   */
//  def applyToPDXServices[A](action: PDXReadable[?] => Task[A]): List[List[ZIO[AppPDXEnv, Throwable, A]]] = {
//    // This is the trick: Create a local "partial" version of the helper
//    // that already knows about 'action' and 'A'.
//    def step[T <: PDXReadable[?] : Tag]: ZIO[T, Throwable, A] =
//      callOnZIOService[T, A](action)
//
//    //    List(
//    //      List(step[InterfaceService]),
//    //      List(step[CountryTagService], step[IdeasManager], step[FocusTreeManager]),
//    //      List(step[ResourcesFileService], step[StateService], step[CountryService])
//    //    )
//    List(
//      List(step[InterfaceService]),
//      List(step[CountryTagService], step[FocusTreeService]),  // todo ideasservice?
//      List(step[StateService], step[CountryService])  // todo resources file service?
//    )
//  }
//
//  // Helper: T is the service type, R is the return type of the method
//  def callOnZIOService[T: zio.Tag, A](fn: T => RIO[Any, A]): ZIO[T, Throwable, A] =
//    ZIO.serviceWithZIO[T](fn)
//
//  /**
//   * Loads all HOI4 and mod data with optional timing callbacks for performance monitoring.
//   *
//   * Components are loaded in this order:
//   * 1. ModifierDatabase - Effect modifier definitions
//   * 2. EffectDatabase - Effect system initialization
//   * 3. Paths - HOI4 and mod directory validation
//   * 4. Localization - Text/translation files
//   * 5. Interface - UI definitions
//   * 6. Resources - Resource definitions
//   * 7. State - Map state files
//   * 8. Country - Country history files
//   * 9. CountryTag - Country tag definitions
//   * 10. Ideas - National ideas/spirits
//   * 11. FocusTrees - National focus trees
//   * 8. FocusTrees - National focus trees
//   * 9. Resources - Resource definitions
//   * 10. State - Map state files
//   * 11. Country - Country history files
//   *
//   * @param hProperties         Configuration properties containing paths and settings
//   * @param loadingLabel        JavaFX label to update with loading status messages
//   * @param isCancelled         Callback to check if loading should be cancelled
//   * @param onComponentComplete Callback invoked when a component finishes loading, receives (componentName, loadTimeSeconds)
//   * @param onComponentStart    Callback invoked when a component begins loading, receives componentName
//   */
//  def load(
//            //            hProperties: Properties,
//            loadingLabel: Label,
//            isCancelled: () => Boolean = () => false,
//            onComponentComplete: (String, Long) => Unit = (_, _) => (),
//            onComponentStart: String => Unit = _ => ()
//          ): RIO[LocalizationService & Config & AppPDXEnv, Unit] = {
//    for {
//      localizationService <- ZIO.service[LocalizationService]
//      config <- ZIO.service[Config]
//      interfaceService <- ZIO.service[InterfaceService]
//      countryTagService <- ZIO.service[CountryTagService]
//      //            ideasManager <- ZIO.service[IdeasManager]
//      focusTreeService <- ZIO.service[FocusTreeService]
//      //            resourcesFileService <- ZIO.service[ResourcesFileService]
//      stateService <- ZIO.service[StateService]
//      countryService <- ZIO.service[CountryService]
//      hProperties = config.getProperties
//      _ <- ZIO.attempt {
//        implicit val properties: Properties = hProperties
//        implicit val label: Label = loadingLabel
//
//        if !isCancelled() then
//          startDatabase(loadingLabel, onComponentStart, onComponentComplete)
//
//        if !isCancelled() then
//          startDatabase1(loadingLabel, onComponentStart, onComponentComplete)
//
//        if !isCancelled() then
//          MenuController.updateLoadingStatus(loadingLabel, "Finding Paths...")
//          val hoi4Path = hProperties.getProperty("hoi4.path")
//          val modPath = hProperties.getProperty("mod.path")
//          if !isCancelled() then
//            if validateDirectoryPath(hoi4Path, "hoi4.path") && validateDirectoryPath(modPath, "mod.path") then
//              HOIIVFiles.setHoi4PathChildDirs(hoi4Path)
//              HOIIVFiles.setModPathChildDirs(modPath)
//              hProperties.setProperty("valid.HOIIVFilePaths", "true")
//            else
//              logger.error("Failed to create HOIIV file paths")
//              hProperties.setProperty("valid.HOIIVFilePaths", "false")
//      } *> {
//        // Localization Reload
//        ZIO.ifZIO(ZIO.succeed(isCancelled()))(
//          ZIO.unit,
//          ZIO.attempt {
//            onComponentStart("Localization")
//            MenuController.updateLoadingStatus(loadingLabel, "Loading Localization...")
//          } *> {
//            for
//              timedResult <- localizationService.reload().timed
//              (duration, _) = timedResult
//              _ <- ZIO.attempt {
//                onComponentComplete("Localization", duration.toNanos)
//              }
//            yield ()
//          }
//        ) // todo adding interface reload or after breaks
//
//
//      }
//    } yield ()
//  }
//
//  private def startDatabase(loadingLabel: Label, onComponentStart: String => Unit = _ => (),
//                            onComponentComplete: (String, Long) => Unit = (_, _) => ()
//                           ): Unit = {
//    val startTime = System.nanoTime()
//    onComponentStart("ModifierDatabase")
//    MenuController.updateLoadingStatus(loadingLabel, "Initializing ModifierDatabase...")
//    ModifierDatabase.init()
//    onComponentComplete("ModifierDatabase", System.nanoTime() - startTime)
//  }
//
//  private def startDatabase1(loadingLabel: Label, onComponentStart: String => Unit = _ => (),
//                             onComponentComplete: (String, Long) => Unit = (_, _) => ()
//                            ): Unit = {
//    val startTime = System.nanoTime()
//    onComponentStart("EffectDatabase")
//    MenuController.updateLoadingStatus(loadingLabel, "Initializing EffectDatabase...")
//    //          EffectDatabase.init() // todo
//    onComponentComplete("EffectDatabase", System.nanoTime() - startTime)
//  }
//
//  private def reloadService(loadingLabel: Label,
//                            service: PDXReadable[?],
//                            isCancelled: () => Boolean = () => false,
//                            onComponentStart: String => Unit = _ => (),
//                            onComponentComplete: (String, Long) => Unit = (_, _) => ()) = {
//    ZIO.ifZIO(ZIO.succeed(isCancelled()))(
//      ZIO.unit,
//      ZIO.attempt {
//        onComponentStart(service.display)
//        MenuController.updateLoadingStatus(loadingLabel, s"Loading ${service.display}...")
//      } *> {
//        for
//          timedResult <- service.read().timed
//          (duration, _) = timedResult
//          _ <- ZIO.attempt {
//            onComponentComplete(service.display, duration.toNanos)
//          }
//        yield ()
//      }
//    )
//  }
//
//  def readPDX(pdx: PDXReadable[?], isCancelled: () => Boolean = () => false)(implicit properties: Properties, label: Label): Task[Unit] =
//    val property = s"valid.${pdx.display}"
//
//    MenuController.updateLoadingStatus(label, s"Loading ${pdx.display} files...")
//    ZIO.succeed {
//      if isCancelled() then ()
//      else
//        try {
//          ZIO.ifZIO(pdx.read())(
//            onTrue = ZIO.succeed(properties.setProperty(property, "true")),
//            onFalse = ZIO.succeed {
//              properties.setProperty(property, "false")
//              logger.error(s"Exception while reading for ${pdx.display}")
//            }
//          )
//        } catch
//          case e: Exception =>
//            properties.setProperty(property, "false")
//            logger.error(s"Exception while reading for ${pdx.display}", e)
//    }
//
//  /** Validates whether the provided directory path is valid */
//  private def validateDirectoryPath(path: String, keyName: String): Boolean =
//    if path == null || path.isEmpty then
//      logger.error("{} is null or empty!", keyName)
//      return false
//    val directory = new File(path)
//    if !directory.exists || !directory.isDirectory then
//      logger.error("{} does not point to a valid directory: {}", keyName, path)
//      return false
//    true
//
//  /** Clears loaded PDX data. */
//  def clearPDX(): Unit =
//    applyToPDXServices(_.clear())
////    pdxList.foreach(_.foreach(_.clear()))
//
