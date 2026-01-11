package com.hoi4utils.main

import com.hoi4utils.databases.effect.EffectDatabase
import com.hoi4utils.databases.effect.EffectDatabase.effectErrors
import com.hoi4utils.databases.modifier.ModifierDatabase
import com.hoi4utils.file.file_listener.{FileAdapter, FileEvent, FileWatcher}
import com.hoi4utils.hoi4.common.country_tags.CountryTag
import com.hoi4utils.hoi4.common.idea.IdeasManager.ideaFileErrors
import com.hoi4utils.hoi4.common.idea.{IdeaFile, IdeasManager}
import com.hoi4utils.hoi4.common.national_focus.FocusTreeManager.focusTreeErrors
import com.hoi4utils.hoi4.common.national_focus.{FocusTree, FocusTreeManager}
import com.hoi4utils.hoi4.gfx.Interface
import com.hoi4utils.hoi4.gfx.Interface.interfaceErrors
import com.hoi4utils.hoi4.history.countries.CountryFile
import com.hoi4utils.hoi4.history.countries.CountryFile.countryErrors
import com.hoi4utils.hoi4.localization.LocalizationService
import com.hoi4utils.hoi4.map.resource.Resource.resourceErrors
import com.hoi4utils.hoi4.map.resource.ResourcesFile
import com.hoi4utils.hoi4.map.state.State
import com.hoi4utils.hoi4.map.state.State.stateErrors
import com.hoi4utils.main.HOIIVFiles
import com.hoi4utils.script.PDXReadable
import com.hoi4utils.ui.menus.MenuController
import com.typesafe.scalalogging.LazyLogging
import javafx.scene.control.Label
import zio.{RIO, UIO, Unsafe, ZIO}

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
class PDXLoader extends LazyLogging:

  /* LOAD ORDER IMPORTANT (depending on the class) */
  val pdxList: List[List[PDXReadable]] = List(
    List(Interface),
    List(CountryTag, IdeasManager, FocusTreeManager),
    List(ResourcesFile, State, CountryFile),
  )

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
            onComponentComplete: (String, Double) => Unit = (_, _) => (),
            onComponentStart: String => Unit = _ => ()
          ): RIO[LocalizationService & Config, Unit] = {
    for {
      service <- ZIO.service[LocalizationService]
      config <- ZIO.service[Config]
      hProperties = config.getProperties
      _ <- ZIO.attempt {
        implicit val properties: Properties = hProperties
        implicit val label: Label = loadingLabel
        //        if isCancelled() then return

        if !isCancelled() then
          val startTime = System.nanoTime()
          onComponentStart("ModifierDatabase")
          MenuController.updateLoadingStatus(loadingLabel, "Initializing ModifierDatabase...")
          ModifierDatabase.init()
          onComponentComplete("ModifierDatabase", (System.nanoTime() - startTime) / 1_000_000_000.0)

        if !isCancelled() then
          val startTime = System.nanoTime()
          onComponentStart("EffectDatabase")
          MenuController.updateLoadingStatus(loadingLabel, "Initializing EffectDatabase...")
          EffectDatabase.init()
          onComponentComplete("EffectDatabase", (System.nanoTime() - startTime) / 1_000_000_000.0)

        if !isCancelled() then
          MenuController.updateLoadingStatus(loadingLabel, "Finding Paths...")
          val hoi4Path = hProperties.getProperty("hoi4.path")
          val modPath = hProperties.getProperty("mod.path")
          if !isCancelled() then
            if validateDirectoryPath(hoi4Path, "hoi4.path") && validateDirectoryPath(modPath, "mod.path") then
              HOIIVFiles.setHoi4PathChildDirs(hoi4Path)
              HOIIVFiles.setModPathChildDirs(modPath)
              hProperties.setProperty("valid.HOIIVFilePaths", "true")
            else
              logger.error("Failed to create HOIIV file paths")
              hProperties.setProperty("valid.HOIIVFilePaths", "false")
      } *>
        // Localization Reload
        ZIO.ifZIO(ZIO.succeed(isCancelled()))(
          ZIO.unit,
          ZIO.attempt {
            onComponentStart("Localization")
            MenuController.updateLoadingStatus(loadingLabel, "Loading Localization...")
          } *> service.reload() *> ZIO.attempt {
            onComponentComplete("Localization", 0.0) // TODO TODO Timing could be improved here if needed
          }
        ) *>
          // Parallel PDX Loading
          ZIO.attempt {
            implicit val properties: Properties = hProperties
            implicit val label: Label = loadingLabel
            pdxList.par.foreach(l =>
              l.foreach(p =>
                if !isCancelled() then
                  val componentName = p.cleanName
                  onComponentStart(componentName)
                  val componentStart = System.nanoTime()
                  readPDX(p, isCancelled)
                  onComponentComplete(componentName, (System.nanoTime() - componentStart) / 1_000_000_000.0)
              )
            )
          }
    } yield () 
      
//        if !isCancelled() then
//          startTime = System.nanoTime()
//          onComponentStart("Localization")
//          MenuController.updateLoadingStatus(loadingLabel, "Loading Localization...")
//          //    Unsafe.unsafe { implicit unsafe =>
//          //      ZHOIIVUtils.getActiveRuntime.unsafe.run(
//          //        ZIO.serviceWithZIO[LocalizationService](_.reload())
//          //      ).getOrThrow()
//          //    }
//          ZIO.serviceWithZIO[LocalizationService](_.reload())
//          //    LocalizationService.reload()  // TODO TODO TODO !!!!!
//          onComponentComplete("Localization", (System.nanoTime() - startTime) / 1_000_000_000.0)
//
//        if isCancelled() then
//          pdxList.par.foreach(l =>
//            l.foreach(p =>
//              if !isCancelled() then
//                val componentName = p.cleanName
//                onComponentStart(componentName)
//                val componentStart = System.nanoTime()
//                readPDX(p, isCancelled)
//                onComponentComplete(componentName, (System.nanoTime() - componentStart) / 1_000_000_000.0)
//            )
//          )
//      }
  }

  def readPDX(pdx: PDXReadable, isCancelled: () => Boolean = () => false)(implicit properties: Properties, label: Label): Unit =
    val property = s"valid.${pdx.cleanName}"

    MenuController.updateLoadingStatus(label, s"Loading ${pdx.cleanName} files...")
    if isCancelled() then return
    try
      if pdx.read() then properties.setProperty(property, "true")
      else
        properties.setProperty(property, "false")
        logger.error(s"Exception while reading for ${pdx.cleanName}")
    catch
      case e: Exception =>
        properties.setProperty(property, "false")
        logger.error(s"Exception while reading for ${pdx.cleanName}", e)

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
  def clearPDX(): Unit = pdxList.foreach(_.foreach(_.clear()))

  def clearLB(): Unit =
    ListBuffer(
      effectErrors,
//      localizationErrors,   // TODO
      interfaceErrors,
      countryErrors,
      focusTreeErrors,
      ideaFileErrors,
      resourceErrors,
      stateErrors
    ).foreach(_.clear())

  def closeDB(): Unit =
    ModifierDatabase.close()
    EffectDatabase.close()
