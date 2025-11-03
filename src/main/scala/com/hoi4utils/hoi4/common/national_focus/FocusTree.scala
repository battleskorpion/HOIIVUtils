package com.hoi4utils.hoi4.common.national_focus

import com.hoi4utils.hoi4.common.country_tags.CountryTag
import com.hoi4utils.hoi4.common.national_focus.FocusTreeManager.*
import com.hoi4utils.hoi4.localization.{Localizable, Property}
import com.hoi4utils.main.HOIIVFiles
import com.hoi4utils.parser.Node
import com.hoi4utils.script.*
import com.hoi4utils.script.datatype.*
import com.hoi4utils.shared.{BoolType, ExpectedRange}
import com.typesafe.scalalogging.LazyLogging
import javafx.collections.{FXCollections, ObservableList}

import java.io.File
import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.jdk.javaapi.CollectionConverters
import scala.util.boundary

val focusTreeIdentifier = "focus_tree"

/**
 * Represents a focus tree, which is a collection of focuses.
 *
 * @note Do not create instances of this class directly, unless a few focus tree is being created or loaded.
 *       Use FocusTree.get(File) instead.
 */
class FocusTree(file: File = null) extends StructuredPDX(focusTreeIdentifier) with Localizable with Comparable[FocusTree] with Iterable[Focus] with PDXFile:
  //final var country = new ReferencePDX[CountryTag](() => CountryTag.toList, tag => Some(tag.get), "country")

  /* PDX attributes */
  val id = 
    StringPDX("id")
  val country = 
    FocusTreeCountry()
  val focuses: MultiPDX[Focus] = 
    MultiPDX[Focus](None, Some(() => new Focus(this)), "focus")
  /* less used */
  val default = 
    BooleanPDX("default")
  val reset_on_civilwar =
    BooleanPDX("reset_on_civilwar")
  val continuousFocusPosition = 
    PointPDX("continuous_focus_position")
  val initialShowPosition =
    InitialShowPosition("initial_show_position")
  val shortcut =
    Shortcut()
  /* special cases - requires special handling :) */
  // todo handle specially
  val sharedFocuses: ReferencePDX[SharedFocus] =
    ReferencePDX[SharedFocus](() => FocusTreeManager.sharedFocuses, "shared_focus")

  // File-level errors (parse errors, etc.)
  var treeErrors: ListBuffer[PDXError] = ListBuffer.empty[PDXError]

  var name: String = ""
  var columns: Int = 1
  var rows: Int = 1
  // private boolean defaultFocus; // ! todo Do This
  // private Point continuousFocusPosition; // ! todo DO THIS

  private var _focusFile: Option[File] = None
  private var _commentedFocuses: ListBuffer[String] = ListBuffer.empty

  /* default */
  FocusTreeManager.add(this)

  file match
    case null => // create empty focus tree
    case _ =>
      require(file.exists && file.isFile, s"Focus tree file $file does not exist or is not a file.")
      loadPDX(file)
      setFile(file)
      collectAndRegisterErrors()

  
  def width: Int = focuses.map(_.absoluteX).maxOption.getOrElse(0)
  def height: Int = focuses.map(_.absoluteY).maxOption.getOrElse(0)
  columns = width + 1
  rows = height + 1

  // todo: add default, continuous focus position
  override protected def childScripts: mutable.Iterable[? <: PDXScript[?]] =
    ListBuffer(id, country, focuses)

  override def handlePDXError(exception: Exception = null, node: Node = null, file: File = null): Unit =
    val pdxError = new PDXError(
      exception = exception,
      errorNode = node,
      file = if file != null then Some(file) else _focusFile,
      pdxScript = this
    ).addInfo("focusTreeId", id.str)
    treeErrors += pdxError

  /**
   * List of all focus IDs in this focus tree.
   *
   * @return list containing each focus ID
   */
  def listFocusIDs: Seq[String] = Seq.from(focuses.flatMap(_.id.value))

  def focusFile: Option[File] = _focusFile

  /**
   * List of commented-out focus definitions found in the focus tree file.
   * These are focus blocks that are commented out and therefore not processed
   * as active focuses, but are preserved for potential future activation.
   *
   * @return list containing commented focus block text
   */
  def commentedFocuses: List[String] = _commentedFocuses.toList

  /**
   * Clears the list of commented focuses. Used for testing and reloading.
   */
  def clearCommentedFocuses(): Unit = {
    _commentedFocuses.clear()
  }

  override def toString: String =
    id.value.orElse(country.value.map(_.toString)).getOrElse:
      _focusFile match
        case Some(file) if file.exists() => s"[Unknown ID: ${file.getName}]"
        case _ => "[Unknown]"

  /**
   * Get the minimum X coordinate of all focuses in this focus tree.
   * @return the minimum absolute X coordinate of all focuses
   */
  def minX: Int = focuses.map(_.absoluteX).minOption.getOrElse(0)

  def listFocuses: List[Focus] = focuses.toList

  def addNewFocus(f: Focus): Unit =
    focuses += f

  /**
   * Collects all focus errors from this tree and registers them with FocusTreeManager
   */
  def collectAndRegisterErrors(): Unit =
    val focusErrorGroups = ListBuffer[FocusErrorGroup]()

    // Add tree-level errors as a special "File Errors" group
    if treeErrors.nonEmpty then
      focusErrorGroups += new FocusErrorGroup("File Errors", treeErrors)

    // Add errors from individual focuses
    focuses.foreach { focus =>
      if focus.focusErrors.nonEmpty then
        focusErrorGroups += new FocusErrorGroup(focus.id.str, focus.focusErrors)
    }

    if focusErrorGroups.nonEmpty then
      val treeErrorGroup = new FocusTreeErrorGroup(id.str, focusErrorGroups)
      FocusTreeManager.focusTreeErrors += treeErrorGroup

  /**
   * Get the next temporary focus ID.
   * @return the next temporary focus ID
   *
   * @note for Java compatibility -_- (as opposed to being able to just use Scala's default parameter)
   */
  def nextTempFocusID(): String =
    nextTempFocusID(focuses.size)

  /**
   * Get the next temporary focus ID.
   * @param lastIntID the last integer ID (should default to size of the focus tree)
   * @return the next temporary focus ID
   */
  private def nextTempFocusID(lastIntID: Int): String =
    val id = "focus_" + (lastIntID + 1)
    if focuses.exists(_.id.value.contains(id)) then return nextTempFocusID(lastIntID)
    id

  def setID(s: String): Unit =
    this.id.set(s)

  def setCountryTag(tag: CountryTag): Unit =
    country.modifier.length match
      case 0 => //country.modifier.add(new country.TagModifierPDX)
      case _ => country.modifier.head.tag.set(tag)

  def setFile(file: File): Unit =
    _focusFile = Some(file)
    _focusFile.foreach(file => focusTreeFileMap.put(file, this))

  override def getFile: Option[File] =
    _focusFile

  def countryTag: Option[CountryTag] =
    country.modifier.length match
      case 0 => None
      case _ => country.modifier.head.tag.value

  def hasFocuses: Boolean = focuses.nonEmpty

  override def compareTo(o: FocusTree): Int =
    (this.countryTag, this.id.value) match
      case (Some(countryTag), Some(id)) =>
        (o.countryTag, o.id.value) match
          case (Some(otherCountryTag), Some(otherID)) =>
            val c = countryTag.compareTo(otherCountryTag)
            if c == 0 then id.compareTo(otherID) else c
          case _ => 0
      case _ => 0

  override def iterator: Iterator[Focus] = focuses.iterator

  override def getLocalizableProperties: mutable.HashMap[Property, String] =
    // lets us map null if we use hashmap instead of generic of() method
    val properties = new mutable.HashMap[Property, String]
    id.value match
      case Some(id) => properties.put(Property.NAME, id)
      case None => //properties.put(Property.NAME, null)
    properties

  /**
   * Get the localizable group of this focus tree, which is the list of focuses.
   *
   * @return
   */
  override def getLocalizableGroup: Iterable[? <: Localizable] = focuses

  override def equals(other: PDXScript[?]): Boolean =
    if other.isInstanceOf[FocusTree] then return this == other
    false

  class FocusTreeCountry extends StructuredPDX("country"):
    final val base = new DoublePDX("base")
    final val factor = new DoublePDX("factor")
    final val add = new DoublePDX("add")
    final val modifier = new MultiPDX[TagModifier](None, Some(() => new TagModifier), "modifier")

    override protected def childScripts: mutable.Iterable[? <: PDXScript[?]] = ListBuffer(base, factor, add, modifier)

    override def getPDXTypeName: String = "AI Willingness"

    class TagModifier extends StructuredPDX("modifier"):
      final val base = new DoublePDX("base")
      final val factor = new DoublePDX("factor")
      final val add = new DoublePDX("add")
      final val tag = new ReferencePDX[CountryTag](() => CountryTag.toList, "country")

      override protected def childScripts: mutable.Iterable[? <: PDXScript[?]] = ListBuffer(base, factor, add, tag)

      override def getPDXTypeName: String = "Modifier"

  // TODO: make sure this is all good and implemented once UnionPDX is implemented
  class InitialShowPosition(pdxIdentifier: String) extends UnionPDX[StructuredPDX](pdxIdentifier):

    override protected def schemas: Seq[() => InitialShowPositionSchema] = Seq(
      () => new InitialShowPosition_Pos,
      () => new InitialShowPosition_Focus,
    )

    trait InitialShowPositionSchema extends StructuredPDX

    class InitialShowPosition_Pos extends PointPDX(pdxIdentifier) with InitialShowPositionSchema:
      val offset: PointPDX = PointPDX("offset")
//        val trigger: TriggerPDX

      override def childScripts: mutable.Iterable[? <: PDXScript[?]] = ListBuffer(offset) ++ super.childScripts
      
    class InitialShowPosition_Focus extends StructuredPDX(pdxIdentifier) with InitialShowPositionSchema:
      val focus: ReferencePDX[Focus] = ReferencePDX[Focus](() => focuses.toList, "focus")

      override def childScripts: mutable.Iterable[? <: PDXScript[?]] = ListBuffer(focus)

  class Shortcut extends StructuredPDX("shortcut"):
    val name = StringPDX("name")  // loc_key
    val target = ReferencePDX[Focus](() => listFocuses, "target")
    /** Controls zoom */
    val scrollWheelFactor = DoublePDX("scroll_wheel_factor", ExpectedRange.ofUnitInterval)
//    val trigger = TriggerPDX("trigger") // TODO IMPL TRIGGER PDX
    
    override protected def childScripts: mutable.Iterable[? <: PDXScript[?]] =
      ListBuffer(name, target, scrollWheelFactor)

  /* Error handling overrides to log detailed information about issues encountered during parsing. */

  override def handleUnexpectedIdentifier(node: Node, exception: Exception): Unit =
    val fullMessage =
      s"""Focus Tree - Unexpected Identifier Error:
         |	Exception: ${exception.getMessage}
         |	Focus Tree ID: ${id.value.getOrElse("undefined")}
         |	Country Tag: ${countryTag.map(_.toString).getOrElse("undefined")}
         |	Focus Count: ${focuses.size}
         |	Node Identifier: ${node.identifier.getOrElse("none")}
         |	Expected Identifiers: ${pdxIdentifiers.mkString("[", ", ", "]")}
         |	Node Value: ${Option(node.$).map(_.toString).getOrElse("null")}
         |	Node Type: ${Option(node.$).map(_.getClass.getSimpleName).getOrElse("null")}
         |	File Path: ${_focusFile.map(_.getAbsolutePath).getOrElse("N/A")}""".stripMargin

    focusTreeErrors += fullMessage
  //    logger.error("Focus Tree - Unexpected Identifier Error:")
  //    errorDetails.foreach(detail => logger.error(s"\t$detail"))

  override def handleNodeValueTypeError(node: Node, exception: Exception): Unit =
    val fullMessage =
      s"""Focus Tree - Node Value Type Error:
         |	Exception: ${exception.getMessage}
         |	Focus Tree ID: ${id.value.getOrElse("undefined")}
         |	Country Tag: ${countryTag.map(_.toString).getOrElse("undefined")}
         |	Focus Count: ${focuses.size}
         |	Node Identifier: ${node.identifier.getOrElse("none")}
         |	Node Value: ${Option(node.$).map(_.toString).getOrElse("null")}
         |	Node Type: ${Option(node.$).map(_.getClass.getSimpleName).getOrElse("null")}
         |	File Path: ${_focusFile.map(_.getAbsolutePath).getOrElse("N/A")}""".stripMargin

    focusTreeErrors += fullMessage
  //    logger.error("Focus Tree - Node Value Type Error:")
  //    errorDetails.foreach(detail => logger.error(s"\t$detail"))

  override def handleParserException(file: File, exception: Exception): Unit =
    val fullMessage =
      s"""Focus Tree - Parser Exception (File):
         |	Exception: ${exception.getMessage}
         |	Focus Tree ID: ${id.value.getOrElse("undefined")}
         |	Country Tag: ${countryTag.map(_.toString).getOrElse("undefined")}
         |	Focus Count: ${focuses.size}
         |	Current Focuses: ${if focuses.nonEmpty then focuses.flatMap(_.id.value).take(5).mkString("[", ", ", if focuses.size > 5 then ", ...]" else "]") else "none"}
         |	Total Focus Trees Loaded: ${focusTrees.size}
         |	File Last Modified: ${if file.exists() then new java.util.Date(file.lastModified()).toString else "N/A"}
         |	File Path: ${file.getAbsolutePath}""".stripMargin

    focusTreeErrors += fullMessage
//    logger.error("Focus Tree - Parser Exception (File):")
//    errorDetails.foreach(detail => logger.error(s"\t$detail"))

case class Point(x: Int, y: Int)