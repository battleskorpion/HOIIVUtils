package com.hoi4utils.hoi4mod.common.idea

import com.hoi4utils.main.HOIIVFiles
import com.hoi4utils.script.PDXReadable
import com.typesafe.scalalogging.LazyLogging
import javafx.collections.{FXCollections, ObservableList}

import java.io.File
import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.jdk.javaapi.CollectionConverters

object IdeasManager extends PDXReadable with LazyLogging:
  val ideaFileFileMap = new mutable.HashMap[File, IdeaFile]()
  val ideaFiles = new ListBuffer[IdeaFile]()
  var ideaFileErrors: ListBuffer[String] = ListBuffer.empty

  def get(idea_file: File): Option[IdeaFile] = {
    if (!ideaFileFileMap.contains(idea_file)) new IdeaFile(idea_file)
    ideaFileFileMap.get(idea_file)
  }

  def observeIdeaFileList: ObservableList[IdeaFile] = {
    FXCollections.observableArrayList(CollectionConverters.asJava(ideaFiles))
  }

  /**
   * Reads all focus trees from the focus trees folder, creating FocusTree instances for each.
   */
  def read(): Boolean = {
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
  override def clear(): Unit = {
    ideaFiles.clear()
    ideaFileFileMap.clear()
  }

  def add(ideaFile: IdeaFile): Iterable[IdeaFile] = {
    ideaFiles += ideaFile
    ideaFiles
  }

  def listIdeaFiles: Iterable[IdeaFile] = ideaFileFileMap.values

  def listIdeas(file: File): List[Idea] = {
    if (!file.exists || file.isDirectory) return List.empty
    // find all ideas defined in same file
    ideaFileFileMap.get(file) match {
      case Some(ideaFile) => ideaFile.listIdeas
      case None => List.empty
    }
  }

  def listIdeasFromAllIdeaFiles: List[Idea] = {
    ideaFileFileMap.values.flatMap(_.listIdeas).toList
  }
