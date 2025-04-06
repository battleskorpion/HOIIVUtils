package com.hoi4utils.clausewitz.data.focus

import language.experimental.namedTuples

import com.hoi4utils.clausewitz.data.country.{CountryTag, CountryTagsManager}
import com.hoi4utils.clausewitz.data.focus.FocusTree.focusTreeFileMap
import com.hoi4utils.clausewitz.localization.*
import com.hoi4utils.clausewitz.script.*
import javafx.collections.{FXCollections, ObservableList}
import org.apache.logging.log4j.{LogManager, Logger}
import com.hoi4utils.clausewitz.{HOIIVUtils, HOIIVFiles}
import org.jetbrains.annotations.*

import java.io.File
import scala.collection.mutable
import scala.collection.mutable.{HashMap, ListBuffer}
import scala.jdk.javaapi.CollectionConverters

/**
 * ALL the FocusTree/FocusTrees
 * Localizable data: focus tree name. Each focus is its own localizable data.
 */
// todo extends file?
object FocusTree {
  val LOGGER: Logger = LogManager.getLogger(classOf[FocusTree])
  private val focusTreeFileMap = new mutable.HashMap[File, FocusTree]()
  private val focusTrees = new ListBuffer[FocusTree]()

  def get(focus_file: File): Option[FocusTree] = {
    if (focus_file == null) return None
    if (!focusTreeFileMap.contains(focus_file)) new FocusTree(focus_file)
    focusTreeFileMap.get(focus_file)
  }

  def observeFocusTrees: ObservableList[FocusTree] = {
    FXCollections.observableArrayList(CollectionConverters.asJava(focusTrees))
  }

  /**
   * Reads all focus trees from the focus trees folder, creating FocusTree instances for each.
   */
  def read(): Boolean = {
    if (!HOIIVFiles.Mod.focus_folder.exists || !HOIIVFiles.Mod.focus_folder.isDirectory) {
      LOGGER.fatal(s"In ${this.getClass.getSimpleName} - ${HOIIVFiles.Mod.focus_folder} is not a directory, or it does not exist.")
      false
    } else if (HOIIVFiles.Mod.focus_folder.listFiles == null || HOIIVFiles.Mod.focus_folder.listFiles.length == 0) {
      LOGGER.warn(s"No focuses found in ${HOIIVFiles.Mod.focus_folder}")
      false
    } else {
      LOGGER.info("Reading focus trees from " + HOIIVFiles.Mod.focus_folder)

      // create focus trees from files
      HOIIVFiles.Mod.focus_folder.listFiles().filter(_.getName.endsWith(".txt")).foreach { f =>
        new FocusTree(f)
      }
      true
    }
  }

  /**
   * Clears all focus trees and any other relevant values.
   */
  def clear(): Unit = {
    focusTrees.clear()
    focusTreeFileMap.clear()
  }

  /**
   * Adds a focus tree to the list of focus trees.
   * @param focusTree the focus tree to add
   * @return the updated list of focus trees
   */
  def add(focusTree: FocusTree): Iterable[FocusTree] = {
    focusTrees += focusTree
    focusTree.focusFile match {
      case Some(file) => focusTreeFileMap.put(file, focusTree)
      case None =>
    }
    focusTrees
  }

  def listFocusTrees: Iterable[FocusTree] = focusTrees

  /**
   * Returns focus tree corresponding to the tag, if it exists
   *
   * @param tag The country tag
   * @return The focus tree, or null if could not be found/not yet created.
   */
  def get(tag: CountryTag): FocusTree = {
    //focusTrees.values.stream.filter((focusTree: FocusTree) => focusTree.country.nodeEquals(tag)).findFirst.orElse(null)
    for (tree <- listFocusTrees) {
      //if (tree.country.equals(tag)) return tree
      val countryTag = tree.countryTag
      countryTag match {
        case Some(t) => if (tag.equals(t.tag)) return tree
        case None => 
      }
    }
    null
  }
}

/**
 * Represents a focus tree, which is a collection of focuses.
 *
 * @note Do not create instances of this class directly, unless a few focus tree is being created or loaded.
 *       Use FocusTree.get(File) instead.
 */
class FocusTree
  extends StructuredPDX("focus_tree") with Localizable with Comparable[FocusTree] with Iterable[Focus] with PDXFile {
  /* pdxscript */
  //final var country = new ReferencePDX[CountryTag](() => CountryTag.toList, tag => Some(tag.get), "country")
  final var country = new FocusTreeCountryPDX
  final var focuses = new MultiPDX[Focus](None, Some(() => new Focus(this)), "focus")
  final var id = new StringPDX("id")
  // private boolean defaultFocus; // ! todo Do This
  // private Point continuousFocusPosition; // ! todo DO THIS

  private var _focusFile: Option[File] = None

  /* default */
  FocusTree.add(this)

  @throws[IllegalArgumentException]
  def this(focus_file: File) = {
    this()
    if (!focus_file.exists) {
      LOGGER.fatal(s"Focus tree file does not exist: $focus_file")
      throw new IllegalArgumentException(s"File does not exist: $focus_file")
    }

    loadPDX(focus_file)
    setFile(focus_file)
  }

  // todo: add default, continuous focus position
  override protected def childScripts: mutable.Iterable[? <: PDXScript[?]] = {
    ListBuffer(id, country, focuses)
  }

  /**
   * List of all focus IDs in this focus tree.
   *
   * @return list containing each focus ID
   */
  def listFocusIDs: Seq[String] = Seq.from(focuses.flatMap(_.id.value))
  
  def focusFile: Option[File] = _focusFile

  override def toString: String = {
    id.value.orElse(country.value.map(_.toString)).getOrElse {
      _focusFile match {
        case Some(file) if file.exists() => s"[Unknown ID: ${file.getName}]"
        case _ => "[Unknown]"
      }
    }
  }

  /**
   * Get the minimum X coordinate of all focuses in this focus tree.
   * @return the minimum absolute X coordinate of all focuses
   */
  def minX: Int = focuses.map(_.absoluteX).minOption.getOrElse(0)

  def listFocuses: List[Focus] = focuses.toList

  def addNewFocus(f: Focus): Unit = {
    focuses += f
  }

  /**
   * Get the next temporary focus ID.
   * @return the next temporary focus ID
   *
   * @note for Java compatibility -_- (as opposed to being able to just use Scala's default parameter)
   */
  def nextTempFocusID(): String = {
    nextTempFocusID(focuses.size)
  }

  /**
   * Get the next temporary focus ID.
   * @param lastIntID the last integer ID (should default to size of the focus tree)
   * @return the next temporary focus ID
   */
  private def nextTempFocusID(lastIntID: Int): String = {
    val id = "focus_" + (lastIntID + 1)
    if (focuses.exists(_.id.value.contains(id))) return nextTempFocusID(lastIntID)
    id
  }

  def setID(s: String): Unit = {
    this.id.set(s)
  }

  def setCountryTag(tag: CountryTag): Unit = {
    country.modifier.length match {
      case 0 => //country.modifier.add(new country.TagModifierPDX)
      case _ => country.modifier.head.tag.set(tag)
    }
  }

  def setFile(file: File): Unit = {
    _focusFile = Some(file)
    _focusFile.foreach(file => focusTreeFileMap.put(file, this))
  }
  
  override def getFile: Option[File] = {
    _focusFile
  }

  def countryTag: Option[CountryTag] = {
    country.modifier.length match {
      case 0 => None
      case _ => country.modifier.head.tag.value
    }
  }

  override def compareTo(o: FocusTree): Int = {
    (this.countryTag, this.id.value) match {
      case (Some(countryTag), Some(id)) =>
        (o.countryTag, o.id.value) match {
          case (Some(otherCountryTag), Some(otherID)) =>
            val c = countryTag.compareTo(otherCountryTag)
            if (c == 0) id.compareTo(otherID) else c
          case _ => 0
        }
      case _ => 0
    }
  }

  override def iterator: Iterator[Focus] = focuses.iterator

  override def getLocalizableProperties: mutable.HashMap[Property, String] = {
    // lets us map null if we use hashmap instead of generic of() method
    val properties = new mutable.HashMap[Property, String]
    id.value match {
      case Some(id) => properties.put(Property.NAME, id)
      case None => //properties.put(Property.NAME, null)
    }
    properties
  }

  /**
   * Get the localizable group of this focus tree, which is the list of focuses.
   *
   * @return
   */
  override def getLocalizableGroup: Iterable[? <: Localizable] = focuses

  override def equals(other: PDXScript[?]): Boolean = {
    if (other.isInstanceOf[FocusTree]) return this == other
    false
  }
  
  class FocusTreeCountryPDX extends StructuredPDX("country") {
    final val base = new DoublePDX("base")
    final val factor = new DoublePDX("factor")
    final val add = new DoublePDX("add")
    final val modifier = new MultiPDX[TagModifierPDX](None, Some(() => new TagModifierPDX), "modifier")

    override protected def childScripts: mutable.Iterable[? <: PDXScript[?]] = ListBuffer(base, factor, add, modifier)

    override def getPDXTypeName: String = "AI Willingness"

    class TagModifierPDX extends StructuredPDX("modifier") {
      final val base = new DoublePDX("base")
      final val factor = new DoublePDX("factor")
      final val add = new DoublePDX("add")
      final val tag = new ReferencePDX[CountryTag](() => CountryTag.toList, tag => Some(tag.get), "country")

      override protected def childScripts: mutable.Iterable[? <: PDXScript[?]] = ListBuffer(base, factor, add, tag)

      override def getPDXTypeName: String = "Modifier"
    }
  }
}