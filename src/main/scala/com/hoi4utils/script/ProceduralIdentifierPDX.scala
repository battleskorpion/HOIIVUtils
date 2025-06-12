package com.hoi4utils.script

import com.hoi4utils.exceptions.UnexpectedIdentifierException
import com.hoi4utils.parser.Node

trait ProceduralIdentifierPDX(p: String => Boolean) { self: AbstractPDX[?] =>

  @throws[UnexpectedIdentifierException]
  override protected def usingIdentifier(expr: Node): Unit =
    pdxIdentifiers match
      case Nil =>
        expr.identifier.filter(p) match
          case Some(id) => pdxIdentifiers = List(id)
          case None => throw UnexpectedIdentifierException(
            expr,
            s"Expected identifier matching predicate, but found none: ${expr.$}"
          )
      case _ if !pdxIdentifiers.exists(expr.nameEquals) =>
        // TODO: add to list to be used later
        ()
      case _ =>
        // Already have identifiers and current expr matches existing one
        ()

  /**
   * @inheritdoc
   */
  override def isValidIdentifier(node: Node): Boolean = {
    if (pdxIdentifiers.isEmpty) node.identifier match {
      case Some(id) => p(id)
      case None => false
    }
    else pdxIdentifiers.indexWhere(node.nameEquals) != -1
  }

  override def isValidID(identifier: String): Boolean = {
    if (pdxIdentifiers.isEmpty) p(identifier)
    else pdxIdentifiers.indexWhere(identifier.equalsIgnoreCase) != -1
  }
}
