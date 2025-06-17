package com.hoi4utils.script

import com.hoi4utils.exceptions.{NodeValueTypeException, UnexpectedIdentifierException}
import com.hoi4utils.parser.Node

import scala.collection.mutable.ListBuffer
import scala.util.{Failure, Success, Try}

abstract class StructuredPDX(pdxIdentifiers: List[String]) extends AbstractPDX[ListBuffer[Node]](pdxIdentifiers):

  def this(pdxIdentifiers: String*) =
    this(pdxIdentifiers.toList)

  var structuredPDXBadNodesList: Iterable[Node] = ListBuffer.empty

  def getStructuredPDXBadNodesList: ListBuffer[String] = {
    val structuredPDXBadNodesList = ListBuffer.empty[String]
    for node <- this.structuredPDXBadNodesList do
      node.identifier match
        case Some(id) => structuredPDXBadNodesList += id
        case None => structuredPDXBadNodesList += s"Node without identifier: ${node.$}"
    structuredPDXBadNodesList
  }

  protected def childScripts: collection.mutable.Iterable[? <: PDXScript[?]]

  /**
   * Sets the current node to the provided expression and processes it based on its type.
   * If the expression is a ListBuffer of nodes, it will load each sub-PDXScript for processing.
   * Otherwise, an exception will be thrown indicating the mismatch in expected node value type.
   *
   * @param expression the node expression to set.
   * @throws UnexpectedIdentifierException if the identifier used in the expression is unexpected.
   * @throws NodeValueTypeException        if the expression is not of type ListBuffer[Node], but was expected to be.
   */
  @throws[UnexpectedIdentifierException]
  @throws[NodeValueTypeException]
  override def set(expression: Node): Unit =
    usingIdentifier(expression)
    this.node = Some(expression)
    expression.$ match
      case l: ListBuffer[Node] =>
        // Load each sub-PDXScript
        var remaining = Iterable.from(l)
        for pdxScript <- childScripts do
          remaining = pdxScript.loadPDX(remaining)
        structuredPDXBadNodesList = remaining
      case _ =>
        throw NodeValueTypeException(expression, "A List", s"${expression.$}")

  override def set(value: ListBuffer[Node]): ListBuffer[Node] =
    // TODO: Consider if this implementation is complete
    super.setNode(value)
    value

  /**
   * Loads the PDX script represented by the given expression.
   * If the expression is a ListBuffer of nodes, it will load each sub-PDXScript for processing.
   * If the expression does not have an identifier, it will attempt to set it directly.
   *
   * @param expression The expression to load.
   * @throws UnexpectedIdentifierException if the identifier used in the expression is unexpected.
   * @throws NodeValueTypeException        if the expression is not of type ListBuffer[Node], but was expected to be.
   */
  @throws[UnexpectedIdentifierException]
  @throws[NodeValueTypeException]
  override def loadPDX(expression: Node): Unit =
    expression.identifier match
      case None =>
        expression.$ match
          case listBuffer: ListBuffer[Node] => loadPDX(listBuffer)
          case _ => println(s"Error loading PDX script: $expression")
      case Some(_) => set(expression)

  /**
   * Loads a collection of PDXScripts from the provided expressions.
   * @param expressions the iterable collection of Node expressions to load
   * @throws UnexpectedIdentifierException
   * @throws NodeValueTypeException
   *  @return remaining unloaded expressions
   */
  @throws[UnexpectedIdentifierException]
  @throws[NodeValueTypeException]
  override def loadPDX(expressions: Iterable[Node]): Iterable[Node] =
    expressions match
      case null => ListBuffer.empty
      case _ =>
        val remaining = ListBuffer.from(expressions)
        val validExpressions = expressions.filter(this.isValidIdentifier)

        for expression <- validExpressions do
          Try(loadPDX(expression)) match
            case Success(_) => remaining -= expression
            case Failure(e) => throw e // Re-throw unexpected exceptions
        remaining

  /**
   * Gets the child PDX property with the current identifier matching the given string.
   */
  def getPDXProperty(identifier: String): Option[PDXScript[?]] =
    childScripts.find(_.pdxIdentifier == identifier)

  /**
   * Gets the child PDX property with the current identifier matching any of the given strings.
   */
  def getPDXProperty(identifiers: List[String]): Option[PDXScript[?]] =
    identifiers.view
      .map(getPDXProperty)
      .find(_.isDefined)
      .flatten

  /**
   * Gets the child PDX property with the current identifier matching the given string,
   * with type safety for the return type.
   */
  def getPDXPropertyOfType[R](identifier: String): Option[PDXScript[R]] =
    childScripts.collectFirst {
      case pdxScript: PDXScript[R] if pdxScript.pdxIdentifier == identifier => pdxScript
    }

  /**
   * Gets the child PDX property with the current identifier matching any of the given strings,
   * with type safety for the return type.
   */
  def getPDXPropertyOfType[R](identifiers: List[String]): Option[PDXScript[R]] =
    identifiers.view
      .map(getPDXPropertyOfType[R])
      .find(_.isDefined)
      .flatten

  def pdxProperties: Iterable[PDXScript[?]] =
    Option(childScripts).getOrElse(Iterable.empty)

  /**
   * Rebuilds the underlying Node tree on demand by gathering the child nodes from childScripts.
   * This ensures that any changes in the child PDXScript objects are reflected in the output.
   */
  override def updateNodeTree(): Unit =
    // Record the original positions of nodes in the current node's value
    val originalPositions: Map[String, Int] = node match
      case Some(n) =>
        n.$ match
          case lb: ListBuffer[Node] =>
            lb.zipWithIndex
              .map((node, index) => node.identifier.getOrElse("") -> index)
              .toMap
          case _ => Map.empty
      case None => Map.empty

    // Update each child script's node tree
    childScripts.foreach(_.updateNodeTree())

    // Get the loaded child nodes
    val loadedChildNodes = childScripts.flatMap(_.getNodes).to(ListBuffer)

    // Sort the loaded nodes based on their original positions
    val sortedLoadedNodes = loadedChildNodes.sortBy { child =>
      child.identifier.fold(Int.MaxValue)(originalPositions.getOrElse(_, Int.MaxValue))
    }

    // Merge loaded nodes and preserved nodes, then re-sort by original order
    val combinedNodes = (sortedLoadedNodes ++ structuredPDXBadNodesList)
      .sortBy(node =>
        node.identifier.fold(Int.MaxValue)(originalPositions.getOrElse(_, Int.MaxValue))
      )

    // Update the current node's value
    if combinedNodes.nonEmpty then
      node match
        case Some(n) => n.setValue(combinedNodes)
        case None => node = Some(Node(pdxIdentifier, "=", combinedNodes))
    else
      node = None

  override def clone(): AnyRef =
    val clonedInstance = super.clone().asInstanceOf[StructuredPDX]
    clonedInstance.node = Some(Node(pdxIdentifier, "=", ListBuffer.empty))
    clonedInstance.structuredPDXBadNodesList = this.structuredPDXBadNodesList
    clonedInstance