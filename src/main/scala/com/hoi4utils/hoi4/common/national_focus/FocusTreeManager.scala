package com.hoi4utils.hoi4.common.national_focus

import com.hoi4utils.hoi4.common.country_tags.{CountryTag, CountryTagService}
import com.hoi4utils.main.HOIIVFiles
import com.hoi4utils.parser.{Parser, ParserException}
import com.hoi4utils.script.{FocusTreeErrorGroup, PDXFileError, PDXReadable}
import com.typesafe.scalalogging.LazyLogging
import javafx.collections.{FXCollections, ObservableList}
import zio.{Chunk, Task, UIO, URIO, URLayer, ZIO, ZLayer}

import java.io.File
import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.concurrent.ExecutionContext
import scala.jdk.javaapi.CollectionConverters

trait FocusTreeManager extends PDXReadable with LazyLogging  {
  var focusTreeErrors: ListBuffer[FocusTreeErrorGroup]

  override def read(): Task[Boolean]
  override def clear(): Task[Unit]
  def add(focusTree: FocusTree): UIO[Set[FocusTree]]

  def addToFileMap(file: File, focusTree: FocusTree): Task[Unit]
  def removeFromFileMap(file: File): Task[Unit]
  def sharedFocusFiles: UIO[Set[SharedFocusFile]]
  def sharedFocusFilesAsPseudoTrees: URIO[FocusTreeManager & CountryTagService, Set[PseudoSharedFocusTree]]
  def sharedFocuses: Set[SharedFocus]
  def observeFocusTrees: ObservableList[FocusTree]
}

object FocusTreeManager {
  val live: URLayer[CountryTagService, FocusTreeManager] =
    ZLayer.fromFunction(FocusTreeManagerImpl.apply)
}

/**
 * ALL the FocusTree/FocusTrees
 * Localizable data: focus tree name. Each focus is its own localizable data.
 */
case class FocusTreeManagerImpl(countryTagService: CountryTagService) extends FocusTreeManager:
  override val cleanName: String = "FocusTrees"
  val focusTrees = new mutable.HashSet[FocusTree]()
  val focusTreeFileMap = new mutable.HashMap[File, FocusTree]()
  var focusTreeErrors: ListBuffer[FocusTreeErrorGroup] = ListBuffer.empty

  /* other */
  private val _sharedFocusFiles = new mutable.HashSet[SharedFocusFile]()

  /** Reads all focus trees from the focus trees folder, creating FocusTree instances for each. */
  override def read(): Task[Boolean] = {
    val modFocusFolder = HOIIVFiles.Mod.focus_folder

    if !modFocusFolder.exists || !modFocusFolder.isDirectory then
      logger.error(s"In ${this.getClass.getSimpleName} - ${modFocusFolder} is not a directory, or it does not exist.")
      ZIO.succeed(false)
    else if modFocusFolder.listFiles == null || modFocusFolder.listFiles.length == 0 then
      logger.warn(s"No focuses found in ${modFocusFolder}")
      ZIO.succeed(false)
    else
      val files = modFocusFolder.listFiles().filter(_.getName.endsWith(".txt")).toList

      for {
        _ <- ZIO.foreachParDiscard(files) { f =>
            ZIO.ifZIO(hasFocusTreeHeader(f))(
              onTrue = add(new FocusTree(f)(this, countryTagService)),
              onFalse = add(new SharedFocusFile(f)(this, countryTagService))
            )
        }
        _ <- ZIO.succeed {
          System.err.println(s"Shared focus files: ${_sharedFocusFiles.size}")
          System.err.println(s"Shared focuses: ${_sharedFocusFiles.map(_.sharedFocuses.size).sum}")
        }
      } yield true
    //    ZIO.succeed {
//      if !modFocusFolder.exists || !modFocusFolder.isDirectory then
//        logger.error(s"In ${this.getClass.getSimpleName} - ${modFocusFolder} is not a directory, or it does not exist.")
//        false
//      else if modFocusFolder.listFiles == null || modFocusFolder.listFiles.length == 0 then
//        logger.warn(s"No focuses found in ${modFocusFolder}")
//        false
//      else
//        // create focus trees from files
//        modFocusFolder.listFiles().filter(_.getName.endsWith(".txt")).par.foreach: f =>
//          ZIO.ifZIO(hasFocusTreeHeader(f))(
//            onTrue = ZIO.succeed(new FocusTree(f)(this)),
//            onFalse = add(new SharedFocusFile(f)(this))
//          )
//        System.err.println(s"Shared focus files: ${_sharedFocusFiles.size}")
//        System.err.println(s"Shared focuses: ${_sharedFocusFiles.map(_.sharedFocuses.size).sum}")
//        true
//    }
  }

  /** Clears all focus trees and any other relevant values. */
  override def clear(): Task[Unit] =
    ZIO.succeed(focusTrees.clear()) &> ZIO.succeed(focusTreeFileMap.clear())

  /**
   * Adds a focus tree to the list of focus trees.
   * @param focusTree the focus tree to add
   * @return the updated list of focus trees
   */
  override def add(focusTree: FocusTree): UIO[Set[FocusTree]] =
    ZIO.succeed {
      focusTrees += focusTree
      focusTree.focusFile match
        case Some(file) => focusTreeFileMap.put(file, focusTree)
        case None =>
      focusTrees.toSet
    }

  /**
   * Adds a shared focus file to the list of shared focus files.
   *
   * @param sharedFocusFile the shared focus file to add
   * @return the updated list of focus trees
   */
  def add(sharedFocusFile: SharedFocusFile): UIO[Set[SharedFocusFile]] =
    ZIO.succeed {
      _sharedFocusFiles += sharedFocusFile
      _sharedFocusFiles.toSet
    }

  /** Returns focus tree corresponding to the tag, if it exists*/
  def get(tag: CountryTag | File): UIO[Option[FocusTree]] =
    ZIO.succeed {
      tag match
        case t: CountryTag => focusTrees.find(_.countryTag == t)
        case f: File => focusTreeFileMap.get(f)
    }

  override def addToFileMap(file: File, focusTree: FocusTree): Task[Unit] =
    ZIO.succeed(focusTreeFileMap.put(file, focusTree))

  override def removeFromFileMap(file: File): Task[Unit] =
    ZIO.succeed(focusTreeFileMap.remove(file))

  override def observeFocusTrees: ObservableList[FocusTree] = FXCollections.observableArrayList(CollectionConverters.asJava(focusTrees))

  override def sharedFocusFiles: UIO[Set[SharedFocusFile]] =
    ZIO.succeed(_sharedFocusFiles.toSet)

  override def sharedFocusFilesAsPseudoTrees: URIO[FocusTreeManager & CountryTagService, Set[PseudoSharedFocusTree]] =
    for {
      files <- ZIO.succeed(Chunk.fromIterable(_sharedFocusFiles))
      pseudoTrees <- ZIO.foreach(files) { sff =>
        PseudoSharedFocusTree.forFocuses(sff.sharedFocuses.toList, sff.fileName)
      }
    } yield pseudoTrees.toSet
//    ZIO.succeed {
//      _sharedFocusFiles.map(sff => PseudoSharedFocusTree.forFocuses(sff.sharedFocuses.toList, s"${sff.fileName}")).toSet
//    }

  def sharedFocuses: Set[SharedFocus] =
    _sharedFocusFiles.flatMap(_.sharedFocuses).toSet

  def hasFocusTreeHeader(file: File): Task[Boolean] =
    ZIO.attemptBlocking {
      val parser = Parser(file)
      val rootNode = parser.parse
      rootNode.contains(focusTreeIdentifier)
    }.catchAll {
      case e: ParserException =>
        ZIO.logError(s"Error parsing file ${file.getName}: ${e.getMessage}").as(false)
      case e =>
        ZIO.fail(e) // Let critical errors (like disk failure) actually fail the Task
    }
