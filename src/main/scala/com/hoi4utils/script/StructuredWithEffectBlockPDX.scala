package com.hoi4utils.script

import com.hoi4utils.exceptions.{NodeValueTypeException, UnexpectedIdentifierException}
import com.hoi4utils.parser.{Node, NodeSeq, SeqNode}

import java.io.File
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
abstract class StructuredWithEffectBlockPDX(pdxIdentifiers: List[String])
  extends StructuredPDX(pdxIdentifiers) {

  def this(pdxIdentifiers: String*) = this(pdxIdentifiers.toList)

  // Holds extra nodes that are not valid structured properties and are assumed to be effects.
  var effectNodes: ListBuffer[Node[?]] = ListBuffer.empty

  /**
   * Override the set method to catch identifiers that aren’t recognized.
   * If a node cannot be loaded as a structured property, we store it as an effect.
   */
  override def set(expression: SeqNode): Unit = {
    // Try to process as normal structured node.
    try {
      super.set(expression)
    } catch {
      case e: UnexpectedIdentifierException =>
        // Instead of failing, we log and store the node as an effect.
        val message = s"Not a structured property, treating as effect: $node"
        logger.warn(message)
        handlePDXError(e, expression)
        effectNodes += expression
      case e: NodeValueTypeException =>
        // Likewise, if the node value type is not as expected, add it to the effect nodes.
        val message = s"Node value type error, treating as effect: ${e.getMessage}\n\t$node"
        logger.warn(message)
        handlePDXError(e, expression)
        effectNodes += expression
    }
  }

  /**
   * Override loadPDX for a single node.
   * If the node’s identifier is not recognized among the structured child scripts,
   * it is assumed to be an effect.
   */
  override def loadPDX(expression: NodeType, file: Option[File]): Unit = expression.identifier match
    case None => super.loadPDX(expression, file)
    case Some(_) =>
      // If the identifier is not among the expected structured ones, add it as an effect.
      if this.isValidIdentifier(expression) then super.loadPDX(expression, file) else effectNodes += expression


  /**
   * Override loadPDX for an iterable collection.
   * For each node, process structured properties and treat unknown nodes as effects.
   */
  override def loadPDX(expressions: NodeSeq): NodeSeq = {
    // TODO TODO
//    if (expressions == null) return Seq.empty
//    super.loadPDX(expressions.filter(this.isValidIdentifier))
//
//    // Collect effect nodes
//    effectNodes ++= expressions.filterNot(this.isValidIdentifier)
//
//    expressions.filterNot(this.isValidIdentifier)
    Seq.empty
  }

  /**
   * When updating the node tree, merge the structured nodes with the extra effect nodes.
   * In this example, the effect nodes are appended after the structured child nodes.
   */
  override def updateNodeTree(): Unit = {
    // TODO TODO
//    // First update as usual so that the structured child scripts are processed.
//    super.updateNodeTree()
//
//    // Append any extra effect nodes.
//    node match {
//      case Some(n) =>
//        n.$ match {
//          case lb: ListBuffer[Node] =>
//            lb ++= effectNodes
//          case _ =>
//            // If the current value is not a ListBuffer, set it to the effect nodes.
//            n.setValue(effectNodes.toSeq)
//        }
//      case None =>
//        // If there is no node yet, create one using the pdxIdentifier and effect nodes.
////        node = Some(new Node(pdxIdentifier, "=", effectNodes.toSeq)) TODO
//        none = None // TODO TODO
//    }
  }

  /**
   * Ensure that cloning also replicates the extra effect nodes.
   */
  override def clone(): AnyRef = {
    val clone = super.clone().asInstanceOf[StructuredWithEffectBlockPDX]
    clone.effectNodes = this.effectNodes.clone()
    clone
  }
}
