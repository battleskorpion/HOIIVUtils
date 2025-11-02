package com.hoi4utils.script

import com.hoi4utils.exceptions.{NodeValueTypeException, UnexpectedIdentifierException}
import com.hoi4utils.parser.Node
import com.typesafe.scalalogging.LazyLogging

import scala.collection.mutable.ListBuffer

abstract class StructuredPDX(pdxIdentifiers: List[String]) extends AbstractPDX[ListBuffer[Node]](pdxIdentifiers) with LazyLogging {
  def this(pdxIdentifiers: String*) = {
    this(pdxIdentifiers.toList)
  }

  var badNodesList: Iterable[Node] = ListBuffer.empty

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
  override def set(expression: Node): Unit = {
    usingIdentifier(expression)
    this.node = Some(expression)
    expression.$ match {
      case l: ListBuffer[Node] =>
        // then load each sub-PDXScript
        var remainingNodes = Iterable.from(l)
        for pdxScript <- childScripts do
          remainingNodes = pdxScript.loadPDX(remainingNodes)
        badNodesList = remainingNodes
      case _ =>
        throw new NodeValueTypeException(expression, "list", this.getClass)
    }
  }

  override def set(value: ListBuffer[Node]): ListBuffer[Node] = {
    // TODO: Consider if this implementation is complete // second TODO: ?
    super.setNode(value)
    value
  }

  /**
   * @inheritdoc
   */
  override def loadPDX(expression: Node): Unit = {
    if isExpressionIdentifierExpected then
      expression.identifier match
        case None => expression.$ match
          case l: ListBuffer[Node] => loadPDX(l)
          case _ => handleNodeValueTypeError(expression, NodeValueTypeException("PDXScript.loadPDX: Expected list of nodes, got: \n" + expression))
        case Some(_) =>
          super.loadPDX(expression)
    else
      super.loadPDX(expression)
  }

  /**
   * Gets the child pdx property with the current identifier matching
   * the given string.
   *
   * @param identifier
   */
  def getPDXProperty(identifier: String): Option[PDXScript[?]] = childScripts.find(_.pdxIdentifier == identifier)

  /**
   * Gets the child pdx property with the current identifier matching
   * the given string.
   *
   * @param identifiers
   */
  def getPDXProperty(identifiers: List[String]): Option[PDXScript[?]] =
    identifiers.view
      .map(getPDXProperty)
      .find(_.isDefined)
      .flatten

  /**
   * Gets the child pdx property with the current identifier matching
   * the given string.
   *
   * @param identifier
   */
  def getPDXPropertyOfType[R](identifier: String): Option[PDXScript[R]] =
    childScripts.collectFirst {
      case pdxScript: PDXScript[R] if pdxScript.pdxIdentifier == identifier => pdxScript
    }

  /**
   * Gets the child pdx property with the current identifier matching
   * the given string.
   *
   * @param identifiers
   */
  def getPDXPropertyOfType[R](identifiers: List[String]): Option[PDXScript[R]] =
    identifiers.view
      .map(getPDXPropertyOfType[R])
      .find(_.isDefined)
      .flatten

  def pdxProperties: Iterable[? <: PDXScript[?]] =
    Option(childScripts).getOrElse(Nil)

  /**
   * Rebuilds the underlying Node tree on demand by gathering the child nodes from childScripts.
   * This ensures that any changes in the child PDXScript objects are reflected in the output.
   */
  override def updateNodeTree(): Unit = {
    // Record the original positions of the nodes in the current node's value.
    val originalPositions: Map[String, Int] = node match
      case Some(n) =>
        n.$ match
          case lb: ListBuffer[Node] => lb.zipWithIndex.map { case (n, i) => n.identifier.getOrElse("") -> i }.toMap
          case _ => Map.empty
      case None => Map.empty

    // Update each child script's node tree.
    childScripts.foreach(_.updateNodeTree())

    // Get the loaded child nodes.
    val loadedChildNodes = childScripts.flatMap(_.getNodes).to(ListBuffer)

    // Sort the loaded nodes based on their original positions.
    val sortedLoadedNodes = loadedChildNodes.sortBy { child =>
      child.identifier.fold(Int.MaxValue)(originalPositions.getOrElse(_, Int.MaxValue))
    }

//    // Retrieve the original nodes.
//    val originalNodes: ListBuffer[Node] = node match {
//      case Some(n) =>
//        n.$ match {
//          case lb: ListBuffer[Node] => lb
//          case _ => ListBuffer.empty[Node]
//        }
//      case None => ListBuffer.empty[Node]
//    }

//    // Preserve any original nodes that were not loaded by child scripts.
//    val preservedNodes = originalNodes.filterNot(orig =>
//      sortedLoadedNodes.exists(child => child.identifier == orig.identifier)
//    )

    val preservedNodes = badNodesList
    // Merge the loaded nodes and preserved nodes, then re-sort by the original order.
    val combinedNodes = (sortedLoadedNodes ++ preservedNodes)
      .sortBy(node =>
        node.identifier.fold(Int.MaxValue)(originalPositions.getOrElse(_, Int.MaxValue))
      )


    // Update the current node's value.
    if (combinedNodes.nonEmpty) then node match
      case Some(n) => n.setValue(combinedNodes)
      case None    => node = Some(Node(pdxIdentifier, "=", combinedNodes))
    else node = None
  }

  override def clone(): AnyRef = {
    val clone = super.clone().asInstanceOf[StructuredPDX]
    clone.node = Some(Node(pdxIdentifier, "=", ListBuffer.empty))
    clone.badNodesList = this.badNodesList
    clone
  }

  def isExpressionIdentifierExpected: Boolean = true
}