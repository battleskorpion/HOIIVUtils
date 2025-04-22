package com.hoi4utils.script

import com.hoi4utils.exceptions.{NodeValueTypeException, UnexpectedIdentifierException}
import com.hoi4utils.parser.Node
import scala.collection.mutable.ListBuffer

trait HeadlessPDX { this: StructuredPDX =>
  /**
   * Overrides the default set behavior to ignore the identifier check.
   * This is useful for headless files where there is no top-level key.
   */
  @throws[NodeValueTypeException]
  override def set(expression: Node): Unit = {
    // Skip identifier checking since headless files do not have a named header.
    this.node = Some(expression)
    expression.$ match {
      case l: ListBuffer[Node] =>
        // then load each sub-PDXScript
        for (pdxScript <- childScripts) {
          pdxScript.loadPDX(l)
        }
      case _ =>
        throw new NodeValueTypeException(expression, "list", this.getClass)
    }
  }

//  /**
//   * Optionally, override loadPDX if you want to further simplify handling of headless files.
//   * Here, we assume that a headless node does not have a name and can be processed directly.
//   */
//  override def loadPDX(expression: Node): Unit = {
//    try {
//      set(expression)
//    } catch {
//      case e: UnexpectedIdentifierException =>
//        System.out.println("Unexpected identifier in headless PDX script: " + e.getMessage)
//      case e: NodeValueTypeException =>
//        System.out.println("Node value type error in headless PDX script: " + e.getMessage)
//    }
//  }
}
