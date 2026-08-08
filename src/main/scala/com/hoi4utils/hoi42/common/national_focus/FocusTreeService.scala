package com.hoi4utils.hoi42.common.national_focus

import com.hoi4utils.hoi42.common.country_tags.{CountryTag, CountryTagService}
import com.hoi4utils.main.HOIIVFiles
import com.hoi4utils.parser.NodeExtensions.{contains, getTyped}
import com.hoi4utils.parser.{NodeSeq, SeqNode, ZIOParser}
import com.hoi4utils.script2.{Registry, *}
import javafx.collections.{FXCollections, ObservableList}
import zio.{Chunk, RIO, Task, UIO, URIO, URLayer, ZIO, ZLayer}

import java.io.File
import scala.annotation.targetName
import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.jdk.javaapi.CollectionConverters

type f = Registry[SharedFocus]

trait FocusTreeService extends FocusTreeRegistry with PDXReadable[f]  {

  override def read(): RIO[Registry[SharedFocus], Boolean]
  def clear123(): Task[Unit]
  override def add(focusTree: FocusTree): Iterable[FocusTree]
  override def addAll(focusTrees: Iterable[FocusTree]): Iterable[FocusTree]
  def add(sharedFocusFile: SharedFocusFile): Iterable[SharedFocusFile]
  @inline final def +=(sharedFocusFile: SharedFocusFile): Iterable[SharedFocusFile] = add(sharedFocusFile)

  def addToFileMap(file: File, focusTree: FocusTree): Task[Unit]
  def removeFromFileMap(file: File): Task[Unit]
  def focusTrees: Iterable[FocusTree]
  def sharedFocusFiles: Iterable[SharedFocusFile]
  def sharedFocusFilesAsPseudoTrees: URIO[FocusTreeService & CountryTagService, Set[PseudoSharedFocusTree]]
  def sharedFocuses: Set[SharedFocus]
  def observeFocusTrees: ObservableList[FocusTree]
  def hasFocusTreeHeader(file: File): Task[Boolean]
  def sharedFocusFileRegistry: Registry[SharedFocusFile]
  def sharedPseudoSharedFocusTree: PseudoSharedFocusTree

  override def clear(): Task[Unit] =
    super[FocusTreeRegistry].clear()
}

object FocusTreeService {
  val live: URLayer[CountryTagService, FocusTreeService] =
    ZLayer.fromFunction(FocusTreeServiceImpl.apply)

  val focusTreeIdentifier = "focus_tree"

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
  // pseudo shared focus tree which is a Registry[SharedFocus] for all sharedFocuses in a regular focus file and not in a
  // sharedFocusFile
  val sharedPseudoSharedFocusTree: PseudoSharedFocusTree = PseudoSharedFocusTree.named("__shared-global", pseudoSharedFocusTreeRegistry)

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
            pdx <- hasFocusTreeHeader(node).flatMap[Registry[SharedFocus], Throwable, FocusTree | SharedFocusFile] {
              case true =>
                ZIO.succeed {
                  val loader = new PDXLoader[FocusTree]()
                  val tree = new FocusTree(this, Some(file))(using sharedFocusRegistry)
                  // using 'node' is WRONG? here. must do `val pdxNode = node.getTyped[NodeSeq]("focus_tree")` and use pdxNode
                  val pdxNode = node.getTyped[NodeSeq](FocusTreeService.focusTreeIdentifier)
//                  val errors = loader.load(node, tree, tree)
                  val errors = loader.load(pdxNode, tree, tree)
                  if (errors.nonEmpty) {
                    // todos
                    Console.err.println(s"Parse errors in ${file.getName}:")
                    errors.map(err => s"\t$err").foreach(Console.err.println)
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
            case tree: FocusTree => ZIO.succeed(+=(tree))
            case sff: SharedFocusFile => ZIO.succeed(+=(sff))
          }
        } yield true
    }
  }

  override def focusTrees: Iterable[FocusTree] = referableEntities

  override def sharedFocusFiles: Iterable[SharedFocusFile] = sharedFocusFileRegistry.referableEntities

  /** Clears all focus trees and any other relevant values. */
  override def clear123(): Task[Unit] =
    ZIO.succeed(this.clear()) &> ZIO.succeed(focusTreeFileMap.clear())

  /**
   * Adds a focus tree to the list of focus trees.
   * @param focusTree the focus tree to add
   * @return the updated list of focus trees
   */
  override def add(focusTree: FocusTree): Iterable[FocusTree] =
    super.add(focusTree)
//      this register focusTree
      // TODO !!!!!
//      focusTree.file match
//        case Some(file) => focusTreeFileMap.put(file, focusTree)
//        case None =>
//      this.focusTrees

  override def addAll(focusTrees: Iterable[FocusTree]): Iterable[FocusTree] =
      super.addAll(focusTrees)
//      this register focusTrees
      // TODO !!!!!
      //      focusTree.file match
      //        case Some(file) => focusTreeFileMap.put(file, focusTree)
      //        case None =>
//      this.focusTrees

  /**
   * Adds a shared focus file to the list of shared focus files.
   *
   * @param sharedFocusFile the shared focus file to add
   * @return the updated list of focus trees
   */
  def add(sharedFocusFile: SharedFocusFile): Iterable[SharedFocusFile] =
    sharedFocusFileRegistry.add(sharedFocusFile)

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

  override def observeFocusTrees: ObservableList[FocusTree] = FXCollections.observableArrayList(CollectionConverters.asJava(focusTrees.toSeq))

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
    sharedFocusFiles.map(_.sharedFocuses).flatMap(_.list).toSet

  def hasFocusTreeHeader(file: File): Task[Boolean] =
    for {
      node <- new ZIOParser(file).parse
      result <- hasFocusTreeHeader(node)
    } yield result
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

  def hasFocusTreeHeader(rootNode: SeqNode): Task[Boolean] =
    ZIO.succeed(rootNode.contains(FocusTreeService.focusTreeIdentifier))

  def addNewFocus(f: Focus, tree: FocusTree): Unit =
    tree.focuses :+ f

  def width(tree: FocusTree): Int = tree.focuses.map(_.absoluteX).maxOption.getOrElse(0)
  def height(tree: FocusTree): Int = tree.focuses.map(_.absoluteY).maxOption.getOrElse(0)
