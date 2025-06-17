package com.hoi4utils.script

import com.hoi4utils.exceptions.{NodeValueTypeException, UnexpectedIdentifierException}
import com.hoi4utils.parser.Node

import scala.collection.mutable.ListBuffer

/**
 * A new variant of StructuredPDX that allows for extra effect nodes.
 * In addition to the usual structured child scripts, this class will collect any
 * nodes that do not match the expected identifiers and treat them as effects.
 *
 * This is useful for constructs like:
 *
 *   if = {
 *     limit = { ... }
 *     <effect>
 *   }
 *
 * where you know the structured part (e.g. the "limit") must be present,
 * but arbitrary effect nodes may also be appended.
 */
abstract class StructuredWithEffectBlockPDX(pdxIdentifiers: List[String]) extends StructuredPDX(pdxIdentifiers):

  def this(pdxIdentifiers: String*) = this(pdxIdentifiers.toList)

  // Holds extra nodes that are not valid structured properties and are assumed to be effects.
  var effectNodes: ListBuffer[Node] = ListBuffer.empty

  /**
   * Override the set method to catch identifiers that aren’t recognized.
   * If a node cannot be loaded as a structured property, we store it as an effect.
   */
  @throws[UnexpectedIdentifierException]
  @throws[NodeValueTypeException]
  override def set(expression: Node): Unit = {
    // Try to process as normal structured node.
    super.set(expression)
  }

  /**
   * Override loadPDX for a single node.
   * If the node’s identifier is not recognized among the structured child scripts,
   * it is assumed to be an effect.
   */
  @throws[UnexpectedIdentifierException]
  @throws[NodeValueTypeException]
  override def loadPDX(expression: Node): Unit =
    if expression.identifier.isEmpty then
      expression.$ match
        case lb: ListBuffer[Node] => lb.foreach(loadPDX)
        case _ => throw NodeValueTypeException(expression, "A List", s"${expression.$}")
    else if isValidIdentifier(expression) then
      try set(expression)
      catch case e: Throwable =>
          node = Some(expression)
          throw e
    else
      effectNodes += expression

  /**
   * Override loadPDX for an iterable collection.
   * For each node, process structured properties and treat unknown nodes as effects.
   */
  @throws[UnexpectedIdentifierException]
  @throws[NodeValueTypeException]
  override def loadPDX(expressions: Iterable[Node]): Iterable[Node] =
    Option(expressions) match
      case Some(exprs) =>
        val remaining = ListBuffer.from(exprs)
        exprs.foreach { expression =>
          if isValidIdentifier(expression) then
            loadPDX(expression)
            remaining -= expression
          else
            effectNodes += expression
            remaining -= expression
        }
        remaining
      case None =>
        ListBuffer.empty
  
  /**
   * When updating the node tree, merge the structured nodes with the extra effect nodes.
   * In this example, the effect nodes are appended after the structured child nodes.
   */
  override def updateNodeTree(): Unit = {
    // First update as usual so that the structured child scripts are processed.
    super.updateNodeTree()

    // Append any extra effect nodes.
    node match {
      case Some(n) =>
        n.$ match {
          case lb: ListBuffer[Node] =>
            lb ++= effectNodes
          case _ =>
            // If the current value is not a ListBuffer, set it to the effect nodes.
            n.setValue(effectNodes)
        }
      case None =>
        // If there is no node yet, create one using the pdxIdentifier and effect nodes.
        node = Some(new Node(pdxIdentifier, "=", effectNodes))
    }
  }

  /**
   * Ensure that cloning also replicates the extra effect nodes.
   */
  override def clone(): AnyRef = {
    val clone = super.clone().asInstanceOf[StructuredWithEffectBlockPDX]
    clone.effectNodes = this.effectNodes.clone()
    clone
  }