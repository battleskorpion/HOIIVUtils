package com.hoi4utils.hoi4.common.idea

import com.hoi4utils.hoi4.gfx.InterfaceService
import com.hoi4utils.main.HOIIVFiles
import com.hoi4utils.script.{PDXFileError, PDXReadable}
import com.typesafe.scalalogging.LazyLogging
import javafx.collections.{FXCollections, ObservableList}
import zio.{Task, UIO, URIO, URLayer, ZIO, ZLayer}

import java.io.File
import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.jdk.javaapi.CollectionConverters

trait IdeasManager extends PDXReadable with LazyLogging {
  var ideaFileErrors: ListBuffer[PDXFileError]

  def get(idea_file: File): Option[IdeaFile]
  def addToFileMap(file: File, ideaFile: IdeaFile): Unit
  def observeIdeaFileList: ObservableList[IdeaFile]
  def add(ideaFile: IdeaFile): Iterable[IdeaFile]
  def listIdeaFiles: Iterable[IdeaFile]
  def listIdeas(file: File): List[Idea]
  def listIdeasFromAllIdeaFiles: List[Idea]
}

object IdeasManager {
  val live: URLayer[Any, IdeasManager] =
    ZLayer.derive[IdeasManagerImpl]
}

case class IdeasManagerImpl() extends IdeasManager:
  override val cleanName: String = "Ideas"
  val ideaFileFileMap = new mutable.HashMap[File, IdeaFile]()
  val ideaFiles = new ListBuffer[IdeaFile]()
  var ideaFileErrors: ListBuffer[PDXFileError] = ListBuffer.empty

  override def get(idea_file: File): Option[IdeaFile] = {
    if (!ideaFileFileMap.contains(idea_file)) new IdeaFile(idea_file)
    ideaFileFileMap.get(idea_file)
  }

  override def addToFileMap(file: File, ideaFile: IdeaFile): Unit =
    ideaFileFileMap.put(file, ideaFile)

  override def observeIdeaFileList: ObservableList[IdeaFile] = {
    FXCollections.observableArrayList(CollectionConverters.asJava(ideaFiles))
  }

  /**
   * Reads all focus trees from the focus trees folder, creating FocusTree instances for each.
   */
  override def read(): Task[Boolean] =
    ZIO.succeed {
      if (!HOIIVFiles.Mod.ideas_folder.exists || !HOIIVFiles.Mod.ideas_folder.isDirectory) {
        logger.error(s"In ${this.getClass.getSimpleName} - ${HOIIVFiles.Mod.focus_folder} is not a directory, or it does not exist.")
        false
      } else if (HOIIVFiles.Mod.ideas_folder.listFiles == null || HOIIVFiles.Mod.ideas_folder.listFiles.length == 0) {
        logger.warn(s"No ideas found in ${HOIIVFiles.Mod.ideas_folder}")
        false
      } else {

        // create focus trees from files
        HOIIVFiles.Mod.ideas_folder.listFiles().filter(_.getName.endsWith(".txt")).foreach { f =>
          new IdeaFile(f)
        }
        true
      }
    }

  /**
   * Clears all idea files and any other relevant values.
   */
  override def clear(): Task[Unit] =
    ZIO.succeed(ideaFiles.clear()) &> ZIO.succeed(ideaFileFileMap.clear())

  override def add(ideaFile: IdeaFile): Iterable[IdeaFile] = {
    ideaFiles += ideaFile
    ideaFiles
  }

  override def listIdeaFiles: Iterable[IdeaFile] = ideaFileFileMap.values

  override def listIdeas(file: File): List[Idea] = {
    if (!file.exists || file.isDirectory) return List.empty
    // find all ideas defined in same file
    ideaFileFileMap.get(file) match {
      case Some(ideaFile) => ideaFile.listIdeas
      case None => List.empty
    }
  }

  override def listIdeasFromAllIdeaFiles: List[Idea] = {
    ideaFileFileMap.values.flatMap(_.listIdeas).toList
  }
