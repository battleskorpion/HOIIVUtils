package com.hoi4utils.script

import com.hoi4utils.exceptions.{NodeValueTypeException, UnexpectedIdentifierException}
import com.hoi4utils.parser.{Node, NodeSeq, SeqNode}

import scala.collection.mutable.ListBuffer

trait HeadlessPDX { this: StructuredPDX =>

  /**
   * Overrides the default set behavior to ignore the identifier check.
   * This is useful for headless files where there is no top-level key.
   */
  @throws[NodeValueTypeException]
  override def set(expression: SeqNode): Unit =
    // Skip identifier checking since headless files do not have a named header.
    this.node = Some(expression)
    // then load each sub-PDXScript
    var remainingNodes: NodeSeq = Seq.from(expression.$)
    for pdxScript <- childScripts do
      remainingNodes = pdxScript.loadPDX(remainingNodes)
    badNodesList = remainingNodes

  override def isValidIdentifier(node: Node[?]): Boolean = true

  override def isExpressionIdentifierExpected: Boolean = false

  /**
   * Optionally, override loadPDX if you want to further simplify handling of headless files.
   * Here, we assume that a headless node does not have a name and can be processed directly.
   * TODO: Throw the exceptions to the caller instead of catching them here? so we can use a proper logger.
   * @param expression The root node of the PDX script.
   */
//  override def loadPDX(expression: Node): Unit = {
//    try {
//      set(expression)
//    } catch {
//      case e: UnexpectedIdentifierException =>
//        logger.error("Unexpected identifier in headless PDX script: " + e.getMessage)
//      case e: NodeValueTypeException =>
//        logger.error("Node value type error in headless PDX script: " + e.getMessage)
//    }
//  }
}
