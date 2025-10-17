package com.hoi4utils.script

import com.hoi4utils.exceptions.{NodeValueTypeException, UnexpectedIdentifierException}
import com.hoi4utils.parser.{Node, Parser, ParserException}

import java.io.File
import scala.collection.mutable.ListBuffer
import scala.reflect.ClassTag
import scala.util.boundary

/**
 * Any object that can be converted to a PDX script block, such as a focus, national focus tree,
 * or event.
 * PDX = Paradox Interactive Clauswitz Engine Modding/Scripting Language
 * <p>
 */
trait AbstractPDX[T](protected var pdxIdentifiers: List[String]) extends PDXScript[T]:
  private[script] var activeIdentifier = 0
  protected[script] var node: Option[Node] = None

  /**
   * Sets the active identifier to match the given expression,
   * if it is a valid identifier. Otherwise, throws exception.
   * @param expr the expression to check, and set the active identifier to the identifier of the expression
   * @throws UnexpectedIdentifierException if the expression is not a valid identifier
   */
  @throws[UnexpectedIdentifierException]
  protected def usingIdentifier(expr: Node): Unit =
//    if pdxIdentifiers.isEmpty
//      // all good?
    if pdxIdentifiers.indexWhere(expr.nameEquals) == -1 then throw new UnexpectedIdentifierException(expr)

  /**
   * @inheritdoc
   */
  override protected def setNode(value: T | String | Int | Double | Boolean | ListBuffer[Node] | Null): Unit =
    value match
      case value if node.isEmpty =>
      case null => setNull()
      case s: String => node.get.setValue(s)
      case i: Int => node.get.setValue(i)
      case d: Double => node.get.setValue(d)
      case b: Boolean => node.get.setValue(b)
      case l: ListBuffer[_] => try node.get.setValue(l.asInstanceOf[ListBuffer[Node]])
        catch case _: ClassCastException => throw new RuntimeException(s"Expected ListBuffer[Node], got ListBuffer with different element type")
      case _ => throw new RuntimeException(s"Unsupported type: ${value.getClass}")

  /**
   * @inheritdoc
   */
  @throws[UnexpectedIdentifierException]
  @throws[NodeValueTypeException]
  override def set(expression: Node): Unit =
    usingIdentifier(expression)
    setNode(expression.$)

  /**
   * @inheritdoc
   */
  override def value: Option[T] = node match
    case None => None
    case Some(n) => n.$ match
      case null => None
      case value => try Some(value.asInstanceOf[T])
        catch case _: ClassCastException => None

  /**
   * @inheritdoc
   */
  override def getNode: Option[Node] = node

  override def getNodes: List[Node] = getNode match
    case Some(node) => List(node)
    case None => List.empty

  /**
   * @inheritdoc
   */
  override def loadPDX(expression: Node): Unit =
    if expression.identifier.isEmpty && (pdxIdentifiers.nonEmpty || expression.isEmpty) then
      logger.error("Error loading PDX script: " + expression)
    else 
      try set(expression)
      catch
        case e: UnexpectedIdentifierException =>
          handleUnexpectedIdentifier(expression, e)
          // Preserve the original node in StructuredPDX as well.
          node = Some(expression)
        case e: NodeValueTypeException        =>
          handleNodeValueTypeError(expression, e)
          // Preserve the original node in StructuredPDX as well.
          node = Some(expression)

  /**
   *
   * @param expressions pdx node that is iterable
   * @return remaining unloaded expressions
   */
  def loadPDX(expressions: Iterable[Node]): Iterable[Node] = expressions match
    case null => ListBuffer.empty
    case _ =>
      val remaining = ListBuffer.from(expressions)
      expressions.filter(this.isValidIdentifier).foreach(expression =>
        loadPDX(expression)
        remaining -= expression
      )
      remaining

  protected def loadPDX(file: File): Unit =
    require(file.exists && file.isFile, s"File $file does not exist or is not a file.")
    val pdxParser = new Parser(file)
    try loadPDX(pdxParser.parse)
    catch case e: ParserException => handleParserException(file, e)

  /**
   * @inheritdoc
   */
  override def isValidIdentifier(node: Node): Boolean = isValidID(node.name)

  override def isValidID(identifier: String): Boolean = pdxIdentifiers.contains(identifier)

  /**
   * @inheritdoc
   */
  override def clearNode(): Unit = node = None

  /**
   * @inheritdoc
   */
  override def setNull(): Unit = node.foreach(_.setNull())

  override def loadOrElse(exp: Node, value: T): Unit =
    loadPDX(exp)
    if node.get.valueIsNull then set(value)

  /**
   * Rebuilds the underlying Node tree from the current state.
   * For simple leaf nodes, this is a no-op.
   * Composite types (e.g. StructuredPDX) should override this method to rebuild their Node tree.
   */
  override def updateNodeTree(): Unit = node.foreach(n => setNode(value.orNull)) // Default behavior for leaf nodes: update the node's value from the current state.

  /**
   * Updates the node tree for collection-based PDX scripts.
   * This method handles the common pattern of updating child nodes and rebuilding the parent node.
   *
   * @param items Collection of PDXScript items to process
   * @param identifier Optional identifier for the parent node (defaults to pdxIdentifier)
   * @tparam U Type of PDXScript items in the collection
   */
  protected def updateCollectionNodeTree[U <: PDXScript[?]](
    items: Iterable[U],
    identifier: String = pdxIdentifier
  ): Unit = {
    items.foreach(_.updateNodeTree())
    val childNodes: ListBuffer[Node] = items.flatMap(_.getNode).to(ListBuffer)
    node match {
      case Some(n) => n.setValue(childNodes)
      case None =>
        if (childNodes.nonEmpty) {
          node = if (identifier != null && identifier.nonEmpty) Some(Node(identifier, "=", childNodes))
                 else Some(Node(childNodes))
        } else node = None
    }
  }

  /**
   * Template method for loading PDX collections with standardized error handling.
   * Subclasses should implement addToCollection to define collection-specific behavior.
   *
   * @param expression The node expression to load into the collection
   */
  protected def loadPDXCollection(expression: Node): Unit = {
    try addToCollection(expression)
    catch {
      case e: UnexpectedIdentifierException =>
        handleUnexpectedIdentifier(expression, e)
        node = Some(expression)
      case e: NodeValueTypeException =>
        handleNodeValueTypeError(expression, e)
        node = Some(expression)
    }
  }

  /**
   * Abstract method for adding expressions to collections.
   * Must be implemented by collection-based PDX classes.
   *
   * @param expression The node expression to add to the collection
   */
  protected def addToCollection(expression: Node): Unit = {
    throw new UnsupportedOperationException("addToCollection must be implemented by collection-based PDX classes")
  }

  /**
   * Provides enhanced error context for debugging.
   * Can be overridden by subclasses to add domain-specific context.
   *
   * @return Map of context information for error reporting
   */
  protected def getErrorContext: Map[String, String] = Map(
    "PDX Type" -> Option(pdxIdentifier).getOrElse("undefined"),
    "Node Has Value" -> node.exists(_.$ != null).toString,
    "Node Is Empty" -> node.exists(_.isEmpty).toString,
    "Active Identifier Index" -> activeIdentifier.toString,
    "Expected Identifiers" -> (if pdxIdentifiers.nonEmpty then pdxIdentifiers.mkString("[", ", ", "]") else "any")
  )

  /**
   * Generates the script output.
   * Before returning the script, updateNodeTree() is called so that the underlying Node reflects any changes.
   */
  override def toScript: String =
    updateNodeTree()
    node.map(_.toScript).getOrElse("")

  override def equals(other: PDXScript[?]): Boolean =
    other match
      case pdx: AbstractPDX[?] =>
        if node == null then return false
        if pdx.node == null then return false
        node.equals(pdx.node)
      case _ => false

  /**
   * @inheritdoc
   */
  override def getOrElse(elseValue: T): T = boundary {
    val value = node.getOrElse(boundary.break(elseValue)).value
    value match
      case Some(t) => t.asInstanceOf[T]
      case _ => elseValue
  }

  override def toString: String =
    if node.isEmpty || node.get.isEmpty then
      if value.isEmpty then super.toString
      else value.get.toString
    else node.toString

  override def isUndefined: Boolean = node.isEmpty || node.get.valueIsNull

  override def isDefined: Boolean = !isUndefined

  override def pdxIdentifier: String = if pdxIdentifiers.isEmpty then null else pdxIdentifiers(activeIdentifier)

  /**
   * Checks whether the node's value is an instance of the specified class.
   */
  def valueIsInstanceOf[A](implicit ct: ClassTag[A]): Boolean = node.exists(n => ct.runtimeClass.isInstance(n.$))

  def getPDXTypeName: String = pdxIdentifier

  def schema(): PDXSchema[T] =
    var schema = new PDXSchema[T](pdxIdentifiers*)
    null.asInstanceOf[PDXSchema[T]]  // todo no

  /* Error handling methods */

  def handleUnexpectedIdentifier(node: Node, exception: Exception): Unit = {
    val context = getErrorContext
    val nodeContext = Map(
      "Node Identifier" -> node.identifier.getOrElse("none"),
      "Node Value" -> Option(node.$).map(_.toString).getOrElse("null"),
      "Node Type" -> Option(node.$).map(_.getClass.getSimpleName).getOrElse("null")
    )
    val allContext = context ++ nodeContext

    logger.error(
      s"""Unexpected Identifier Error:
         |	Exception: ${exception.getMessage}
         |${allContext.map { case (k, v) => s"\t$k: $v" }.mkString("\n")}""".stripMargin)
  }

  def handleNodeValueTypeError(node: Node, exception: Exception): Unit = {
    val context = getErrorContext
    val nodeContext = Map(
      "Node Identifier" -> node.identifier.getOrElse("none"),
      "Expected Type" -> "T (generic parameter)",
      "Actual Value" -> Option(node.$).map(_.toString).getOrElse("null"),
      "Actual Type" -> Option(node.$).map(_.getClass.getSimpleName).getOrElse("null"),
      "Node Has Null Value" -> (node.$ == null).toString
    )
    val allContext = context ++ nodeContext

    logger.error(
      s"""Node Value Type Error:
         |	Exception: ${exception.getMessage}
         |${allContext.map { case (k, v) => s"\t$k: $v" }.mkString("\n")}""".stripMargin)
  }

  def handleParserException(file: File, exception: Exception): Unit = {
    val context = getErrorContext
    val fileContext = Map(
      "File Path" -> file.getAbsolutePath,
      "File Exists" -> file.exists().toString,
      "File Last Modified" -> (if (file.exists()) new java.util.Date(file.lastModified()).toString else "N/A")
    )
    val allContext = context ++ fileContext

    logger.error(
      s"""Parser Exception (File):
         |	Exception Message: ${exception.getMessage}
         |${allContext.map { case (k, v) => s"\t$k: $v" }.mkString("\n")}""".stripMargin)
  }