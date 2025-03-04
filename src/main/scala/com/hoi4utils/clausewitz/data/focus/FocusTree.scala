package com.hoi4utils.clausewitz.data.focus

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
  private val focusTrees = new mutable.ListBuffer[FocusTree]()

  def get(focus_file: File): Option[FocusTree] = {
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
      LOGGER.fatal(s"In State.java - ${HOIIVFiles.Mod.focus_folder} is not a directory, or it does not exist.")
      false
    } else if (HOIIVFiles.Mod.focus_folder.listFiles == null || HOIIVFiles.Mod.focus_folder.listFiles.length == 0) {
      LOGGER.warn(s"No focuses found in ${HOIIVFiles.Mod.states_folder}")
      false
    } else {
      LOGGER.info("Reading focus trees from " + HOIIVFiles.Mod.focus_folder)

      // create focus trees from files
      for (f <- HOIIVFiles.Mod.focus_folder.listFiles) {
        if (f.getName.endsWith(".txt")) new FocusTree(f)
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

  def add(focusTree: FocusTree): Iterable[FocusTree] = {
    focusTrees += focusTree
    focusTrees
  }

  def listFocusTrees: Iterable[FocusTree] = focusTreeFileMap.values

  /**
   * Returns focus tree corresponding to the tag, if it exists
   *
   * @param tag
   * @return The focus tree, or null if could not be found/not yet created.
   */
  def get(tag: CountryTag): FocusTree = {
    //focusTrees.values.stream.filter((focusTree: FocusTree) => focusTree.country.nodeEquals(tag)).findFirst.orElse(null)
    for (tree <- listFocusTrees) {
      //if (tree.country.equals(tag)) return tree
      val countryTag = tree.country.get()
      countryTag match {
        case Some(t) => if (t.tag.equals(tag)) return tree
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
  extends StructuredPDX("focus_tree") with Localizable with Comparable[FocusTree] with Iterable[Focus] {
  /* pdxscript */
  final var country: ReferencePDX[CountryTag] = new ReferencePDX[CountryTag](() => CountryTag.toList, t => Some(t.get), "country")
  final var focuses: MultiPDX[Focus] = new MultiPDX[Focus](None, Some(() => new Focus(this)), "focus")
  final var id: StringPDX = new StringPDX("id")
  // private boolean defaultFocus; // ! todo Do This
  // private Point continuousFocusPosition; // ! todo DO THIS

  private var _focusFile: File = _

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
    focusTreeFileMap.put(this.focusFile, this)
  }

  // todo: add default, continuous focus position
  override protected def childScripts: mutable.Iterable[? <: PDXScript[?]] = ListBuffer(id, country, focuses)

  /**
   * List of all focus IDs in this focus tree.
   *
   * @return list containing each focus ID
   */
  def listFocusIDs: Seq[String] = Seq.from(focuses.flatMap(_.id.get()))

  def countryTag: CountryTag = country.get() match {
    case Some(t) => t
    case None => null
  }

  def focusFile: File = _focusFile

  override def toString: String = id.get() match {
    case Some(id) => id
    case None => country.get() match {
      case Some(tag) => tag.toString
      case None =>
        if (focusFile != null && focusFile.exists) s"[Unknown ID: ${focusFile.getName}]"
        else "[Unknown]"
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
    if (focuses.exists(_.id.get().contains(id))) return nextTempFocusID(lastIntID)
    id
  }

  def setID(s: String): Unit = {
    this.id.set(s)
  }

  def setCountryTag(tag: CountryTag): Unit = {
    this.country.set(tag)
  }

  def setFile(file: File): Unit = {
    this._focusFile = file
  }

  override def compareTo(o: FocusTree): Int = {
    (this.country.get(), this.id.get()) match {
      case (Some(countryTag), Some(id)) =>
        (o.country.get(), o.id.get()) match {
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
    id.get() match {
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
}