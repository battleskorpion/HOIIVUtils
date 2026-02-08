package com.hoi4utils.hoi4.common.idea

import com.hoi4utils.databases.modifier.{Modifier, ModifierDatabase}
import com.hoi4utils.hoi4.localization.{Localizable, Property}
import com.hoi4utils.main.HOIIVUtils
import com.hoi4utils.parser.{Node, PDXValueNode, ParsingContext, SeqNode}
import com.hoi4utils.script.*
import com.hoi4utils.script.datatype.DoublePDX
import com.hoi4utils.script.seq.CollectionPDX
import com.hoi4utils.shared.ExpectedRange
import com.typesafe.scalalogging.LazyLogging
import org.jetbrains.annotations.NotNull
import zio.ZIO

import java.io.File
import scala.collection.mutable
import scala.collection.mutable.ListBuffer


/**
 * This is the Idea file.
 */
object Idea extends LazyLogging {

  def getDataFunctions: Iterable[Idea => ?] = {
    val dataFunctions = new ListBuffer[Idea => ?]() // for optimization, limited number of data functions.

    dataFunctions += (idea => idea.localizationText(Property.NAME))
    dataFunctions += (idea => idea.localizationText(Property.DESCRIPTION))
    dataFunctions
  }

  def pdxSupplier(): PDXSupplier[Idea] = {
    new PDXSupplier[Idea] {
      override def simplePDXSupplier(): Option[PDXValueNode[?] => Option[Idea]] = {
//        Some((expr: PDXValueNode[?]) => {
//            Some(new Idea(expr)) // todo check if this is correct? or nah?
//        })
        // todo
        None
      }

      override def blockPDXSupplier(): Option[SeqNode => Option[Idea]] = {
//        Some((expr: SeqNode) => {
//          Some(new Idea(expr))
//        })
        // todo
        None
      }
    }
  }
}

/**
 *
 * @param name
 *
 * @note Since name is not declared val or var, it will not become a field of Idea.
 */
class Idea(pdxIdentifier: String) extends StructuredPDX(pdxIdentifier) with Localizable with Iterable[Modifier] with Referable[String] {
  private var _ideaFile: Option[IdeaFile] = None

  /* idea */
  final val modifiers = new CollectionPDX[Modifier](ModifierDatabase(), "modifier") {
    override def loadPDX(expr: NodeType, file: Option[File]): Unit = {
      super.loadPDX(expr, file)
    }

    override def handlePDXError(exception: Exception = null, node: Node[?] = null, file: File = null): Unit =
      val errFile = if file != null then file else _ideaFile.flatMap(_.getFile).orNull
      given ParsingContext(errFile, node)
      val pdxError = new PDXFileError(
        exception = exception,
        errorNode = node,
        pdxScript = this
      ).addInfo("idea", Idea.this.toString)
      val ideasManager: IdeasManager = zio.Unsafe.unsafe { implicit unsafe =>
        HOIIVUtils.getActiveRuntime.unsafe.run(ZIO.service[IdeasManager]).getOrThrowFiberFailure()
      }
      ideasManager.ideaFileErrors += pdxError

    override def getPDXTypeName: String = "Modifiers"
  }
  final val removalCost = new DoublePDX("cost", ExpectedRange(-1.0, Double.PositiveInfinity))

  def this(node: SeqNode) =
    this(node.name)
//    loadPDX(node, _ideaFile.flatMap(_.getFile)) TODO TODO

  def this(ideaFile: IdeaFile, identifier: String) =
    this(identifier)
    this._ideaFile = Some(ideaFile)

  def this(ideaFile: IdeaFile, node: SeqNode) =
    this(node)
    this._ideaFile = Some(ideaFile)

  /**
   * @inheritdoc
   */
  override protected def childScripts: mutable.Seq[PDXScript[?, ?]] =
    ListBuffer(removalCost, modifiers)

//  protected def addModifier(modifier: Modifier): Unit = {
//    modifiers.add(modifier)
//  }
//
//  def getRemovalCost: Int = removalCost
//
//  def setRemovalCost(removalCost: Int): Unit = {
//    if (removalCost < -1) this.removalCost = -1
//    else this.removalCost = removalCost
//  }
//

  def id: Option[String] = this.pdxIdentifier match {
    case null => None
    case _ => Some(this.pdxIdentifier)
  }

  override def referableID: Option[String] = id

//  def setID(id: String): Unit = {
//    this.id.set(id)
//  }

  def setIdeaFile(ideaFile: IdeaFile): Unit = {
    this._ideaFile = Some(ideaFile)
  }

  def clearFile(): Unit = _ideaFile = None

//  override def compareTo(@NotNull o: Idea): Int = id.compareTo(o.id)

  override def iterator: Iterator[Modifier] = modifiers.iterator

  override def toString: String = {
    id match {
      case Some(id) => id
      case None => "[Unknown]"
    }
  }

  @NotNull override def localizableProperties: Map[Property, String] = {
    val id = this.id.getOrElse("")
    Map(Property.NAME -> id, Property.DESCRIPTION -> s"${id}_desc")
  }

  @NotNull override def getLocalizableGroup: Iterable[? <: Localizable] = {
    _ideaFile match {
      case Some(ideaFile) => ideaFile.getLocalizableGroup
      case None => Iterable(this)
    }
  }

}
