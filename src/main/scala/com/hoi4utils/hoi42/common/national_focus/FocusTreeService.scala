package com.hoi4utils.hoi42.common.national_focus

import com.hoi4utils.hoi42.common.country_tags.{CountryTag, CountryTagService}
import com.hoi4utils.main.HOIIVFiles
import com.hoi4utils.parser.ZIOParser
import com.hoi4utils.script2.*
import javafx.collections.{FXCollections, ObservableList}
import zio.{Chunk, RIO, Task, UIO, URIO, URLayer, ZIO, ZLayer}

import java.io.File
import scala.annotation.targetName
import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.jdk.javaapi.CollectionConverters


trait FocusTreeService extends FocusTreeRegistry with PDXReadable  {

  override def read(): Task[Boolean]
  override def clear123(): Task[Unit]
  @targetName("add")
  override def +=(focusTree: FocusTree): UIO[Set[FocusTree]]
  @targetName("addAll")
  override def ++=(focusTrees: Iterable[FocusTree]): UIO[Set[FocusTree]]
  @targetName("add")
  def +=(sharedFocusFile: SharedFocusFile): UIO[Set[SharedFocusFile]]

  def addToFileMap(file: File, focusTree: FocusTree): Task[Unit]
  def removeFromFileMap(file: File): Task[Unit]
  def sharedFocusFiles: UIO[Set[SharedFocusFile]]
  def sharedFocusFilesAsPseudoTrees: URIO[FocusTreeService & CountryTagService, Set[PseudoSharedFocusTree]]
  def sharedFocuses: Set[SharedFocus]
  def observeFocusTrees: ObservableList[FocusTree]
  def hasFocusTreeHeader(file: File): Task[Boolean]

  override def clear(): Task[Unit] =
    super[FocusTreeRegistry].clear()
}

object FocusTreeService {
  val live: URLayer[CountryTagService, FocusTreeService] =
    ZLayer.fromFunction(FocusTreeServiceImpl.apply)
}

/**
 * ALL the FocusTree/FocusTrees
 * Localizable data: focus tree name. Each focus is its own localizable data.
 */
case class FocusTreeServiceImpl(countryTagService: CountryTagService) extends FocusTreeService:
  override val display: String = "Focus Trees"

  val focusTreeFileMap = new mutable.HashMap[File, FocusTree]()
  val sharedFocusFileRegistry = new SharedFocusFileRegistry()
  val pseudoSharedFocusTreeRegistry = new PseudoSharedFocusTreeRegistry()

  /* other */
  // TODO bring back after getting things working :)
//  private val _sharedFocusFiles = new mutable.HashSet[SharedFocusFile]()

  /**
   * Reads all focus trees from the focus trees folder, creating a [[FocusTree]] for each.
   */
  override def read(): RIO[Registry[SharedFocus], Boolean] = {
    ZIO.serviceWithZIO[Registry[SharedFocus]] { sharedFocusRegistry =>
      def readFocusTrees(files: Seq[File]): RIO[Registry[SharedFocus], Seq[FocusTree | SharedFocusFile]] =
        ZIO.foreach(files) { file => // foreachParDiscard??
          for {
            node <- new ZIOParser(file).parse
            pdx <- hasFocusTreeHeader(file).flatMap[Registry[SharedFocus], Throwable, FocusTree | SharedFocusFile] {
              case true =>
                ZIO.attempt {
                  val loader = new PDXLoader[FocusTree]()
                  val tree = new FocusTree(this, Some(file))(using sharedFocusRegistry)
                  val errors = loader.load(node, tree, tree)
                  if (errors.nonEmpty) {
                    println(s"Parse errors in ${file.getName}: ${errors.mkString(", ")}")
                  }
                  tree
                }
              case false =>
                ZIO.attempt {
                  val loader = new PDXLoader[SharedFocusFile]()
                  val sharedFocusFile = new SharedFocusFile(sharedFocusFileRegistry, Some(file))
                  val errors = loader.load(node, sharedFocusFile, sharedFocusFile)
                  if (errors.nonEmpty) {
                    println(s"Parse errors in ${file.getName}: ${errors.mkString(", ")}")
                  }
                  sharedFocusFile
                }
            }
            _ <- ZIO.logDebug(s"Successfully processed: ${file.getName}")
          } yield pdx
          //        _ <- ZIO.log(s"Shared focus files: ${_sharedFocusFiles.size}")
          //        _ <- ZIO.log(s"Shared focuses: ${_sharedFocusFiles.map(_.sharedFocuses.size).sum}")
        }

      val modFocusFolder = HOIIVFiles.Mod.focus_folder

      if !modFocusFolder.exists || !modFocusFolder.isDirectory then
        ZIO.logError(s"In ${this.getClass.getSimpleName} - ${modFocusFolder} is not a directory, or it does not exist.")
          .as(false)
      else if modFocusFolder.listFiles == null || modFocusFolder.listFiles.length == 0 then
        ZIO.logWarning(s"No focuses found in ${modFocusFolder}")
          .as(false)
      else
        val files = modFocusFolder.listFiles().filter(_.getName.endsWith(".txt"))

        for {
          trees <- readFocusTrees(files)
          _ <- ZIO.foreachDiscard(trees) {
            case tree: FocusTree => +=(tree)
            case sff: SharedFocusFile => +=(sff)
          }
        } yield true
    }
  }

  override def focusTrees: Set[FocusTree] = referableEntities.toSet

  override def sharedFocusFiles: Set[SharedFocusFile] = sharedFocusFileRegistry.referableEntities.toSet

  /** Clears all focus trees and any other relevant values. */
  override def clear123(): Task[Unit] =
    ZIO.succeed(this.clear()) &> ZIO.succeed(focusTreeFileMap.clear())

  /**
   * Adds a focus tree to the list of focus trees.
   * @param focusTree the focus tree to add
   * @return the updated list of focus trees
   */
  @targetName("add")
  override def +=(focusTree: FocusTree): UIO[Set[FocusTree]] =
    ZIO.succeed {
      this register focusTree
      // TODO !!!!!
//      focusTree.file match
//        case Some(file) => focusTreeFileMap.put(file, focusTree)
//        case None =>
      this.focusTrees
    }

  @targetName("addAll")
  override def ++=(focusTrees: Iterable[FocusTree]): UIO[Set[FocusTree]] =
    ZIO.succeed {
      this register focusTrees
      // TODO !!!!!
      //      focusTree.file match
      //        case Some(file) => focusTreeFileMap.put(file, focusTree)
      //        case None =>
      this.focusTrees
    }

  /**
   * Adds a shared focus file to the list of shared focus files.
   *
   * @param sharedFocusFile the shared focus file to add
   * @return the updated list of focus trees
   */
  @targetName("add")
  def +=(sharedFocusFile: SharedFocusFile): UIO[Set[SharedFocusFile]] =
    ZIO.succeed {
      sharedFocusFileRegistry register sharedFocusFile
      sharedFocusFiles
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

  override def sharedFocusFilesAsPseudoTrees: URIO[FocusTreeService & CountryTagService, Set[PseudoSharedFocusTree]] =
    for {
      files <- ZIO.succeed(Chunk.fromIterable(sharedFocusFiles))
      pseudoTrees <- ZIO.foreach(files) { sff =>
        PseudoSharedFocusTree.forFocuses(sff.sharedFocuses.list, sff.fileName.getOrElse(""), pseudoSharedFocusTreeRegistry, sff.file)
      }
    } yield pseudoTrees.toSet
  //    ZIO.succeed {
  //      _sharedFocusFiles.map(sff => PseudoSharedFocusTree.forFocuses(sff.sharedFocuses.toList, s"${sff.fileName}")).toSet
  //    }

  def sharedFocuses: Set[SharedFocus] =
    sharedFocusFiles.map(_.sharedFocuses).flatMap(_.list)

  def hasFocusTreeHeader(file: File): Task[Boolean] =
    // TODO TODO
    ZIO.succeed(true)
//    ZIO.attemptBlocking {
//      val parser = Parser(file)
//      val rootNode = parser.parse
//      rootNode.contains(focusTreeIdentifier)
//    }.catchAll {
//      case e: ParserException =>
//        ZIO.logError(s"Error parsing file ${file.getName}: ${e.getMessage}").as(false)
//      case e =>
//        ZIO.fail(e) // Let critical errors (like disk failure) actually fail the Task
//    }

  def addNewFocus(f: Focus, tree: FocusTree): Unit =
    tree.focuses :+ f

  def width(tree: FocusTree): Int = tree.focuses.map(_.absoluteX).flatMap(_.maxOption).getOrElse(0)
  def height(tree: FocusTree): Int = tree.focuses.map(_.absoluteY).flatMap(_.maxOption).getOrElse(0)
