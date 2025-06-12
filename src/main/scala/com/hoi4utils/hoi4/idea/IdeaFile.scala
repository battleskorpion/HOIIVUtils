package com.hoi4utils.hoi4.idea

import com.hoi4utils.hoi4.idea.IdeaFile.ideaFileFileMap
import com.hoi4utils.localization.Localizable
import com.hoi4utils.parser.Node
import com.hoi4utils.script.{CollectionPDX, PDXScript, StructuredPDX}
import com.hoi4utils.{HOIIVFiles, PDXReadable}
import com.typesafe.scalalogging.LazyLogging
import javafx.collections.{FXCollections, ObservableList}

import java.io.File
import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.jdk.javaapi.CollectionConverters

object IdeaFile extends LazyLogging with PDXReadable {
  private val ideaFileFileMap = new mutable.HashMap[File, IdeaFile]()
  private val ideaFiles = new ListBuffer[IdeaFile]()

  def get(idea_file: File): Option[IdeaFile] = {
    if (!ideaFileFileMap.contains(idea_file)) new IdeaFile(idea_file)
    ideaFileFileMap.get(idea_file)
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
  def clear(): Unit = {
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

}

class IdeaFile extends StructuredPDX("ideas") with Iterable[Idea] {
  /* pdxscript */
  final var countryIdeas = new CollectionPDX[Idea](Idea.pdxSupplier(), "country") {
    override def loadPDX(expr: Node): Unit = {
      super.loadPDX(expr)
      // todo try catch for unexpected identifiers and add to class specific listBuffer
      pdxList.foreach(idea => idea.setIdeaFile(IdeaFile.this))
    }

    override def getPDXTypeName: String = "Country Ideas"
  }

  private var _file: Option[File] = None

  /* default */
  IdeaFile.add(this)

  @throws[IllegalArgumentException]
  def this(file: File) = {
    this()
    if (!file.exists()) {
      logger.error(s"Idea file does not exist: $file")
      throw new IllegalArgumentException(s"File does not exist: $file")
    }

    loadPDX(file)
    setFile(file)
    _file.foreach(file => ideaFileFileMap.put(file, this))
  }

  override protected def childScripts: mutable.Iterable[? <: PDXScript[?]] = {
    ListBuffer(countryIdeas)
  }

  def file: Option[File] = _file

  def listIdeas: List[Idea] = {
    List.from(countryIdeas)
  }

  def setFile(file: File): Unit = {
    _file = Some(file)
  }

  override def iterator: Iterator[Idea] = listIdeas.iterator

  /**
   * Get the localizable group for ideas in this file, which is the list of all ideas in this file.
   *
   * @return
   */
  def getLocalizableGroup: Iterable[? <: Localizable] = listIdeas
}
