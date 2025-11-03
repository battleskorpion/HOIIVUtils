package com.hoi4utils.hoi4.common.idea

import com.hoi4utils.exceptions.UnexpectedIdentifierException
import com.hoi4utils.hoi4.localization.Localizable
import com.hoi4utils.main.HOIIVFiles
import com.hoi4utils.parser.{Node, ParserException}
import com.hoi4utils.script.*
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
  IdeasManager.add(this)

  file match
    case null => // create empty idea
    case _ =>
      require(file.exists && file.isFile, s"Idea file $file does not exist or is not a file.")
      loadPDX(file)
      setFile(file)
      _file.foreach(file => IdeasManager.ideaFileFileMap.put(file, this))

  override def handlePDXError(exception: Exception = null, node: Node = null, file: File = null): Unit =
    val pdxError = new PDXError(
      exception = exception,
      errorNode = node,
      file = if file != null then Some(file) else _file,
      pdxScript = this
    )
    IdeasManager.ideaFileErrors += pdxError

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