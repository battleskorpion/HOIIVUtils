package com.hoi4utils.hoi42.common.idea

import com.hoi4utils.databases.modifier.{Modifier, ModifierDatabase}
import com.hoi4utils.hoi42.localization.{Localizable, Property}
import com.hoi4utils.main.HOIIVUtils
import com.hoi4utils.parser.{Node, PDXValueNode, ParsingContext, SeqNode}
import com.hoi4utils.script.{DoublePDX, PDXFileError, PDXSupplier, StructuredPDX}
import com.hoi4utils.script2.*
import com.hoi4utils.script2.seq.CollectionPDX
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
        Some((expr: PDXValueNode[?]) => {
            Some(new Idea(expr)) // todo check if this is correct? or nah?
        })
      }

      override def blockPDXSupplier(): Option[SeqNode => Option[Idea]] = {
        Some((expr: SeqNode) => {
          Some(new Idea(expr))
        })
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
class Idea(pdxIdentifier: String) extends StructuredPDX(pdxIdentifier) with Localizable with Referable[String] {
  private var _ideaFile: Option[IdeaFile] = None

  /* idea */
  final val modifiers = new CollectionPDX[Modifier](ModifierDatabase(), "modifier") {
    override def getPDXTypeName: String = "Modifiers"
  }
  final val removalCost = new DoublePDX("cost", ExpectedRange(-1.0, Double.PositiveInfinity))

  def this(node: SeqNode) =
    this(node.name)

  def this(ideaFile: IdeaFile, identifier: String) =
    this(identifier)
    this._ideaFile = Some(ideaFile)

  def this(ideaFile: IdeaFile, node: SeqNode) =
    this(node)
    this._ideaFile = Some(ideaFile)

  /**
   * @inheritdoc
   */
  override protected def childScripts: mutable.Seq[PDXScript[?]] =
    ListBuffer(removalCost, modifiers)

  def id: Option[String] = this.pdxIdentifier match {
    case null => None
    case _ => Some(this.pdxIdentifier)
  }

  override def referableID: Option[String] = id
  
  def setIdeaFile(ideaFile: IdeaFile): Unit = {
    this._ideaFile = Some(ideaFile)
  }

  def clearFile(): Unit = _ideaFile = None

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
