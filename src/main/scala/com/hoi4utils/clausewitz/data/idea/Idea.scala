package com.hoi4utils.clausewitz.data.idea

import language.experimental.namedTuples
import com.hoi4utils.ExpectedRange
import com.hoi4utils.clausewitz.code.effect.EffectParameter
import com.hoi4utils.clausewitz.code.modifier.{Modifier, ModifierDatabase}
import com.hoi4utils.clausewitz.data.idea.Idea.idea_list
import com.hoi4utils.clausewitz.localization.Localizable
import com.hoi4utils.clausewitz.localization.Localization
import com.hoi4utils.clausewitz.localization.Property
import com.hoi4utils.clausewitz.script.{CollectionPDX, DoublePDX, PDXScript, StringPDX, StructuredPDX}
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
//  private val ideaFileMap = new mutable.HashMap[File, Idea]()
  private val idea_list = new ListBuffer[Idea]()

  def getDataFunctions: Iterable[Idea => ?] = {
    val dataFunctions = new ListBuffer[Idea => ?]() // for optimization, limited number of data functions.

    dataFunctions += (idea => idea.localizationText(Property.NAME))
    dataFunctions += (idea => idea.localizationText(Property.DESCRIPTION))
    dataFunctions
  }

  def getIdeas: List[Idea] = List.from(idea_list)

  def getIdeas(file: File): List[Idea] = {
    if (!file.exists || file.isDirectory) return List.empty
    // find all ideas defined in same file
    idea_list.filter(idea => idea._ideaFile == file).toList
  }

  def add(idea: Idea): Iterable[Idea] = {
    idea_list += idea
    idea_list
  }
}

class Idea extends StructuredPDX with Localizable with Comparable[Idea] {

  /* idea */
  final val id = new StringPDX("id")
  final val modifiers = new CollectionPDX[Modifier](ModifierDatabase(), "country") {
    override def loadPDX(expr: Node): Unit = {
      super.loadPDX(expr)
    }

    override def getPDXTypeName: String = "Modifiers"
  }
  final val removalCost = new DoublePDX("cost", ExpectedRange(-1.0, Double.PositiveInfinity))

  private var _ideaFile: Option[File] = None

  /* default */
  Idea.add(this)

  @throws[IllegalArgumentException]
  def this(file: File) = {
    this()
    if (!file.exists()) {
      LOGGER.fatal(s"Idea file does not exist: $file")
      throw new IllegalArgumentException(s"File does not exist: $file")
    }

    loadPDX(file)
    setFile(file)
//    ideaFileMap.put(this._ideaFile, this)
  }

//  protected def addModifier(modifier: Modifier): Unit = {
//    modifiers.add(modifier)
//  }
//
//  def getIdea(ideaID: String): Idea = {
//    import scala.collection.JavaConversions._
//    for (idea <- Idea.idea_list) {
//      if (idea.id == ideaID) return idea
//    }
//    null
//  }
//
//  def getRemovalCost: Int = removalCost
//
//  def setRemovalCost(removalCost: Int): Unit = {
//    if (removalCost < -1) this.removalCost = -1
//    else this.removalCost = removalCost
//  }
//
  override def toString: String = name

  private def name: String = null // todo

  def setID(id: String): Unit = {
    this.id.set(id)
  }

  def setFile(file: File): Unit = {
    this._ideaFile = Some(file)
  }

  override def compareTo(@NotNull o: Idea): Int = id.compareTo(o.id)

  @NotNull override def getLocalizableProperties: mutable.Map[Property, String] = {
    val id = this.id.getOrElse("")
    mutable.Map(Property.NAME -> id, Property.DESCRIPTION -> s"${id}_desc")
  }

  @NotNull override def getLocalizableGroup: Iterable[_ <: Localizable] = {
    if (idea_list == null) return Iterable(this)
    else idea_list
  }

  override protected def childScripts: mutable.Iterable[PDXScript[?]] = {
   ListBuffer(id, modifiers, removalCost) 
  }
}
