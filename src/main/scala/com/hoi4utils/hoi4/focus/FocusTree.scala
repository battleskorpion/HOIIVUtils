package com.hoi4utils.hoi4.focus

import com.hoi4utils.exceptions.UnexpectedIdentifierException
import com.hoi4utils.hoi4.country.CountryTag
import com.hoi4utils.hoi4.focus.FocusTree.focusTreeFileMap
import com.hoi4utils.localization.{Localizable, Property}
import com.hoi4utils.parser.{Node, ParserException}
import com.hoi4utils.script.*
import com.hoi4utils.{HOIIVFiles, PDXReadable}
import com.typesafe.scalalogging.LazyLogging
import javafx.collections.{FXCollections, ObservableList}

import java.io.File
import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.jdk.javaapi.CollectionConverters
/**
 * Represents a focus tree, which is a collection of focuses.
 *
 * @note Do not create instances of this class directly, unless a few focus tree is being created or loaded.
 *       Use FocusTree.get(File) instead.
 */
class FocusTree(file: File = null) extends StructuredPDX("focus_tree") with Localizable with Comparable[FocusTree] with Iterable[Focus] with PDXFile {
  //final var country = new ReferencePDX[CountryTag](() => CountryTag.toList, tag => Some(tag.get), "country")
  final var country = new FocusTreeCountryPDX
  final var focuses = new MultiPDX[Focus](None, Some(() => new Focus(this)), "focus")
  final var id = new StringPDX("id")
  // private boolean defaultFocus; // ! todo Do This
  // private Point continuousFocusPosition; // ! todo DO THIS

  private var _focusFile: Option[File] = None

  /* default */
  FocusTree.add(this)

  file match
    case null => // create empty focus tree
    case _ =>
      require(file.exists && file.isFile, s"Focus tree file $file does not exist or is not a file.")
      loadPDX(file)
      setFile(file)


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
  
  def hasFocuses: Boolean = focuses.nonEmpty

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

  /* Error handling overrides to log detailed information about issues encountered during parsing. */

  override def handleUnexpectedIdentifier(node: Node, exception: Exception): Unit = {
    val errorDetails = ListBuffer[String]()
    errorDetails += s"Focus Tree File: ${_focusFile.map(_.getName).getOrElse("[Unknown file]")}"
    errorDetails += s"Focus Tree ID: ${id.value.getOrElse("undefined")}"
    errorDetails += s"Country Tag: ${countryTag.map(_.toString).getOrElse("undefined")}"
    errorDetails += s"Node Identifier: ${node.identifier.getOrElse("none")}"
    errorDetails += s"Expected Identifiers: ${pdxIdentifiers.mkString("[", ", ", "]")}"
    errorDetails += s"Node Value: ${Option(node.$).map(_.toString).getOrElse("null")}"
    errorDetails += s"Node Type: ${Option(node.$).map(_.getClass.getSimpleName).getOrElse("null")}"
    errorDetails += s"Focus Count: ${focuses.size}"
    errorDetails += s"File Path: ${_focusFile.map(_.getAbsolutePath).getOrElse("N/A")}"
    errorDetails += s"Exception Message: ${exception.getMessage}"
    if (exception.getCause != null) {
      errorDetails += s"Root Cause: ${exception.getCause.getMessage}"
    }

    val fullMessage = "Focus Tree - Unexpected Identifier Error:\n" +
      errorDetails.map(detail => s"\t$detail").mkString("\n")

    FocusTree.focusTreeFileErrors += fullMessage
//    logger.error("Focus Tree - Unexpected Identifier Error:")
//    errorDetails.foreach(detail => logger.error(s"\t$detail"))
  }

  override def handleNodeValueTypeError(node: Node, exception: Exception): Unit = {
    val errorDetails = ListBuffer[String]()
    errorDetails += s"Focus Tree File: ${_focusFile.map(_.getName).getOrElse("[Unknown file]")}"
    errorDetails += s"Focus Tree ID: ${id.value.getOrElse("undefined")}"
    errorDetails += s"Country Tag: ${countryTag.map(_.toString).getOrElse("undefined")}"
    errorDetails += s"Node Identifier: ${node.identifier.getOrElse("none")}"
    errorDetails += s"Expected Type: Variable (context-dependent)"
    errorDetails += s"Actual Value: ${Option(node.$).map(_.toString).getOrElse("null")}"
    errorDetails += s"Actual Type: ${Option(node.$).map(_.getClass.getSimpleName).getOrElse("null")}"
    errorDetails += s"Node Has Value: ${node.$ != null}"
    errorDetails += s"Node Is Empty: ${node.isEmpty}"
    errorDetails += s"Focus Count: ${focuses.size}"
    errorDetails += s"File Path: ${_focusFile.map(_.getAbsolutePath).getOrElse("N/A")}"
    errorDetails += s"Exception Type: ${exception.getClass.getSimpleName}"
    errorDetails += s"Exception Message: ${exception.getMessage}"
    if (exception.getCause != null) {
      errorDetails += s"Root Cause: ${exception.getCause.getMessage}"
    }

    val fullMessage = "Focus Tree - Node Value Type Error:\n" +
      errorDetails.map(detail => s"\t$detail").mkString("\n")

    FocusTree.focusTreeFileErrors += fullMessage
//    logger.error("Focus Tree - Node Value Type Error:")
//    errorDetails.foreach(detail => logger.error(s"\t$detail"))
  }

  override def handleParserException(node: Node, exception: Exception): Unit = {
    val errorDetails = ListBuffer[String]()
    errorDetails += s"Focus Tree File: ${_focusFile.map(_.getName).getOrElse("[Unknown file]")}"
    errorDetails += s"Focus Tree ID: ${id.value.getOrElse("undefined")}"
    errorDetails += s"Country Tag: ${countryTag.map(_.toString).getOrElse("undefined")}"
    errorDetails += s"Node Identifier: ${node.identifier.getOrElse("none")}"
    errorDetails += s"Node Content: ${if (node.isEmpty) "empty" else "has content"}"
    errorDetails += s"Node Value: ${Option(node.$).map(_.toString).getOrElse("null")}"
    errorDetails += s"Focus Count: ${focuses.size}"
    errorDetails += s"Current Focuses: ${if (focuses.nonEmpty) focuses.flatMap(_.id.value).take(5).mkString("[", ", ", if (focuses.size > 5) ", ...]" else "]") else "none"}"
    errorDetails += s"File Path: ${_focusFile.map(_.getAbsolutePath).getOrElse("N/A")}"
    errorDetails += s"Parser Exception Type: ${exception.getClass.getSimpleName}"
    errorDetails += s"Exception Message: ${exception.getMessage}"
    if (exception.getCause != null) {
      errorDetails += s"Root Cause: ${exception.getCause.getMessage}"
    }

    val fullMessage = "Focus Tree - Parser Exception (Node):\n" +
      errorDetails.map(detail => s"\t$detail").mkString("\n")

    FocusTree.focusTreeFileErrors += fullMessage
//    logger.error("Focus Tree - Parser Exception (Node):")
//    errorDetails.foreach(detail => logger.error(s"\t$detail"))
  }

  override def handleParserException(file: File, exception: Exception): Unit = {
    val errorDetails = ListBuffer[String]()
    errorDetails += s"Focus Tree ID: ${id.value.getOrElse("undefined")}"
    errorDetails += s"Country Tag: ${countryTag.map(_.toString).getOrElse("undefined")}"
    errorDetails += s"File Path: ${file.getAbsolutePath}"
    errorDetails += s"File Name: ${file.getName}"
    errorDetails += s"File Exists: ${file.exists()}"
    errorDetails += s"File Size: ${if (file.exists()) s"${file.length()} bytes" else "N/A"}"
    errorDetails += s"File Readable: ${file.canRead}"
    errorDetails += s"File Last Modified: ${if (file.exists()) new java.util.Date(file.lastModified()).toString else "N/A"}"
    errorDetails += s"Focus Count: ${focuses.size}"
    errorDetails += s"Current Focuses: ${if (focuses.nonEmpty) focuses.flatMap(_.id.value).take(5).mkString("[", ", ", if (focuses.size > 5) ", ...]" else "]") else "none"}"
    errorDetails += s"Total Focus Trees Loaded: ${FocusTree.listFocusTrees.size}"
    errorDetails += s"Parser Exception Type: ${exception.getClass.getSimpleName}"
    errorDetails += s"Exception Message: ${exception.getMessage}"
    if (exception.getCause != null) {
      errorDetails += s"Root Cause: ${exception.getCause.getMessage}"
    }

    val fullMessage = "Focus Tree - Parser Exception (File):\n" +
      errorDetails.map(detail => s"\t$detail").mkString("\n")

    FocusTree.focusTreeFileErrors += fullMessage
//    logger.error("Focus Tree - Parser Exception (File):")
//    errorDetails.foreach(detail => logger.error(s"\t$detail"))
  }
}

/**
 * ALL the FocusTree/FocusTrees
 * Localizable data: focus tree name. Each focus is its own localizable data.
 */
object FocusTree extends LazyLogging with PDXReadable {
  var focusTreeFileErrors: ListBuffer[String] = ListBuffer.empty
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
      logger.error(s"In ${this.getClass.getSimpleName} - ${HOIIVFiles.Mod.focus_folder} is not a directory, or it does not exist.")
      false
    } else if (HOIIVFiles.Mod.focus_folder.listFiles == null || HOIIVFiles.Mod.focus_folder.listFiles.length == 0) {
      logger.warn(s"No focuses found in ${HOIIVFiles.Mod.focus_folder}")
      false
    } else {

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
   */
  def get(tag: CountryTag): Option[FocusTree] = {
    listFocusTrees.foreach { tree =>
      tree.countryTag match {
        case Some(t) if tag.equals(t.tag) => return Some(tree)
        case _ => // Do nothing
      }
    }
    None
  }

  def getRandom: Option[FocusTree] = {
    if (focusTrees.isEmpty) return None
    Some(focusTrees(scala.util.Random.nextInt(focusTrees.size)))
  }
}