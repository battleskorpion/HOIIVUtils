package com.hoi4utils.hoi4mod.common.idea

import com.hoi4utils.exceptions.UnexpectedIdentifierException
import com.hoi4utils.hoi4mod.common.idea.IdeaFile.ideaFileFileMap
import com.hoi4utils.hoi4mod.localization.Localizable
import com.hoi4utils.main.HOIIVFiles
import com.hoi4utils.parser.{Node, ParserException}
import com.hoi4utils.script.{CollectionPDX, PDXFile, PDXReadable, PDXScript, StructuredPDX}
import com.typesafe.scalalogging.LazyLogging
import javafx.collections.{FXCollections, ObservableList}

import java.io.File
import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.jdk.javaapi.CollectionConverters

class IdeaFile(file: File = null) extends StructuredPDX("ideas") with Iterable[Idea] with PDXFile {
  /* pdxscript */
  final var countryIdeas = new CollectionPDX[Idea](Idea.pdxSupplier(), "country") {
    override def loadPDX(expr: Node): Unit = {
      super.loadPDX(expr)
      pdxList.foreach(idea => idea.setIdeaFile(IdeaFile.this))
    }

    override def getPDXTypeName: String = "Country Ideas"
  }

  private var _file: Option[File] = None

  /* default */
  IdeaFile.add(this)

  file match
    case null => // create empty idea
    case _ =>
      require(file.exists && file.isFile, s"Idea file $file does not exist or is not a file.")
      loadPDX(file)
      setFile(file)
      _file.foreach(file => ideaFileFileMap.put(file, this))

  override def handleUnexpectedIdentifier(node: Node, exception: Exception): Unit =
    val message = s"Unexpected identifier in idea file ${fileNameOrElse("[Unknown file]")}: ${node.identifier}"
    IdeaFile.ideaFileErrors += message
//    logger.error(message)


  override def handleNodeValueTypeError(node: Node, exception: Exception): Unit =
    val message = s"Node value type error in idea file ${fileNameOrElse("[Unknown file]")}: ${exception.getMessage}"
    IdeaFile.ideaFileErrors += message
//    logger.error(message)

  override protected def childScripts: mutable.Iterable[? <: PDXScript[?]] = {
    ListBuffer(countryIdeas)
  }

  def listIdeas: List[Idea] = {
    List.from(countryIdeas)
  }

  def setFile(file: File): Unit = {
    _file = Some(file)
  }
  
  override def getFile: Option[File] = _file

  override def iterator: Iterator[Idea] = listIdeas.iterator

  /**
   * Get the localizable group for ideas in this file, which is the list of all ideas in this file.
   *
   * @return
   */
  def getLocalizableGroup: Iterable[? <: Localizable] = listIdeas
}

object IdeaFile extends LazyLogging with PDXReadable {
  private val ideaFileFileMap = new mutable.HashMap[File, IdeaFile]()
  private val ideaFiles = new ListBuffer[IdeaFile]()
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