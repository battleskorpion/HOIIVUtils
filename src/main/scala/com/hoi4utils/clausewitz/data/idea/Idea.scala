package com.hoi4utils.clausewitz.data.idea

import language.experimental.namedTuples
import com.hoi4utils.ExpectedRange
import com.hoi4utils.clausewitz.code.effect.EffectParameter
import com.hoi4utils.clausewitz.code.modifier.{Modifier, ModifierDatabase}
import com.hoi4utils.clausewitz.data.idea.Idea.listAllIdeas
import com.hoi4utils.clausewitz.localization.Localizable
import com.hoi4utils.clausewitz.localization.Localization
import com.hoi4utils.clausewitz.localization.Property
import com.hoi4utils.clausewitz.script.{CollectionPDX, DoublePDX, PDXScript, PDXSupplier, StringPDX, StructuredPDX}
import com.hoi4utils.clausewitz_parser.Node
import javafx.beans.property.SimpleStringProperty
import org.apache.logging.log4j.{LogManager, Logger}
import org.jetbrains.annotations.NotNull

import scala.jdk.javaapi.CollectionConverters
import java.io.File
import scala.collection.mutable
import scala.collection.mutable.{ListBuffer, Map}


/**
 * This is the Idea file.
 */
object Idea {
  val LOGGER: Logger = LogManager.getLogger(classOf[Idea])

  def getDataFunctions: Iterable[Idea => ?] = {
    val dataFunctions = new ListBuffer[Idea => ?]() // for optimization, limited number of data functions.

    dataFunctions += (idea => idea.localizationText(Property.NAME))
    dataFunctions += (idea => idea.localizationText(Property.DESCRIPTION))
    dataFunctions
  }

  def listAllIdeas: List[Idea] = {
    IdeaFile.listIdeasFromAllIdeaFiles
  }

  def pdxSupplier(): PDXSupplier[Idea] = {
    new PDXSupplier[Idea] {
      override def simplePDXSupplier(): Option[Node => Option[Idea]] = {
        Some((expr: Node) => {
            Some(new Idea(expr)) // todo check if this is correct? or nah?
        })
      }

      override def blockPDXSupplier(): Option[Node => Option[Idea]] = {
        Some((expr: Node) => {
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
class Idea(pdxIdentifier: String) extends StructuredPDX(pdxIdentifier) with Localizable with Iterable[Modifier] {
  private var _ideaFile: Option[IdeaFile] = None

  /* idea */
  final val modifiers = new CollectionPDX[Modifier](ModifierDatabase(), "modifier") {
    override def loadPDX(expr: Node): Unit = {
      super.loadPDX(expr)
    }

    override def getPDXTypeName: String = "Modifiers"
  }
  final val removalCost = new DoublePDX("cost", ExpectedRange(-1.0, Double.PositiveInfinity))

  def this(node: Node) = {
    this(node.identifier)
    loadPDX(node)
  }

  def this(ideaFile: IdeaFile, identifier: String) = {
    this(identifier)
    this._ideaFile = Some(ideaFile)
  }

  def this(ideaFile: IdeaFile, node: Node) = {
    this(node)
    this._ideaFile = Some(ideaFile)
  }

  /**
   * @inheritdoc
   */
  override protected def childScripts: mutable.Iterable[PDXScript[?]] = {
    ListBuffer(removalCost, modifiers)
  }

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

  @NotNull override def getLocalizableProperties: mutable.Map[Property, String] = {
    val id = this.id.getOrElse("")
    mutable.Map(Property.NAME -> id, Property.DESCRIPTION -> s"${id}_desc")
  }

  @NotNull override def getLocalizableGroup: Iterable[_ <: Localizable] = {
    _ideaFile match {
      case Some(ideaFile) => ideaFile.getLocalizableGroup
      case None => Iterable(this)
    }
  }

}
