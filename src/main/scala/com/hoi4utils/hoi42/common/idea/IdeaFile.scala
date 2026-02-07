//package com.hoi4utils.hoi42.common.idea
//
//import com.hoi4utils.exceptions.UnexpectedIdentifierException
//import com.hoi4utils.hoi42.localization.Localizable
//import com.hoi4utils.main.{HOIIVFiles, HOIIVUtils}
//import com.hoi4utils.parser.{Node, ParserException, ParsingContext, SeqNode}
//import com.hoi4utils.script.{PDXFileError, StructuredPDX}
//import com.hoi4utils.script2.*
//import com.hoi4utils.script2.seq.CollectionPDX
//import com.typesafe.scalalogging.LazyLogging
//import javafx.collections.{FXCollections, ObservableList}
//import zio.ZIO
//
//import java.io.File
//import scala.collection.mutable
//import scala.collection.mutable.ListBuffer
//import scala.jdk.javaapi.CollectionConverters
//
//class IdeaFile(file: File = null) extends StructuredPDX("ideas") with Iterable[Idea] with PDXFile {
//  /* pdxscript */
//  final var countryIdeas = new CollectionPDX[Idea](Idea.pdxSupplier(), "country") {
//    override def loadPDX(expr: NodeType, file: Option[File]): Unit = {
//      super.loadPDX(expr, file)
//      pdxList.foreach(idea => idea.setIdeaFile(IdeaFile.this))
//    }
//
//    override def getPDXTypeName: String = "Country Ideas"
//  }
//
//  private var _file: Option[File] = None
//
//  file match
//    case null => // create empty idea
//    case _ =>
//      require(file.exists && file.isFile, s"Idea file $file does not exist or is not a file.")
//      loadPDX(file)
//      setFile(file)
////      _file.foreach(file => ideasManager.addToFileMap(file, this))  // done in Ideas mgr now
//
//  override def handlePDXError(exception: Exception = null, node: Node[?] = null, file: File = null): Unit =
//    given ParsingContext(file, node)
//    val ideasManager: IdeasManager = zio.Unsafe.unsafe { implicit unsafe =>
//      HOIIVUtils.getActiveRuntime.unsafe.run(ZIO.service[IdeasManager]).getOrThrowFiberFailure()
//    }
//    val pdxError = new PDXFileError(
//      exception = exception,
//      errorNode = node,
//      pdxScript = this
//    )
//    ideasManager.ideaFileErrors += pdxError
//
//  override protected def childScripts: mutable.Seq[? <: PDXScript[?, ?]] = {
//    ListBuffer(countryIdeas)
//  }
//
//  def listIdeas: List[Idea] = {
//    List.from(countryIdeas)
//  }
//
//  def setFile(file: File): Unit = {
//    _file = Some(file)
//  }
//
//  override def getFile: Option[File] = _file
//
//  override def iterator: Iterator[Idea] = listIdeas.iterator
//
//  /**
//   * Get the localizable group for ideas in this file, which is the list of all ideas in this file.
//   *
//   * @return
//   */
//  def getLocalizableGroup: Iterable[? <: Localizable] = listIdeas
//}
