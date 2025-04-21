package com.hoi4utils.clausewitz.script

import com.hoi4utils.clausewitz.code.ClausewitzDate
import com.hoi4utils.clausewitz.exceptions.UnexpectedIdentifierException
import com.hoi4utils.clausewitz_parser.Node

trait ProceduralIdentifierPDX(p: String => Boolean) { self: AbstractPDX[?] =>

  @throws[UnexpectedIdentifierException]
  override protected def usingIdentifier(expr: Node): Unit = {
    if (pdxIdentifiers.isEmpty) expr.identifier match {
      case Some(id) =>
        if (p(id)) {
          LOGGER.debug("Using date identifier: " + id)
          pdxIdentifiers = List(id)
        }
      case None =>
        LOGGER.debug("No identifier found")
    }
    else if (pdxIdentifiers.indexWhere(expr.nameEquals) == -1) {
      LOGGER.error("Unexpected identifier: " + expr.name)
      throw new UnexpectedIdentifierException(expr)
    }
  }

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
