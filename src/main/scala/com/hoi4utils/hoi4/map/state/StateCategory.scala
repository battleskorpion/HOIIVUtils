package com.hoi4utils.hoi4.map.state

import com.hoi4utils.hoi4.map.state.StateCategoryDefinition
import com.hoi4utils.parser.Node
import com.hoi4utils.script.*
/**
 * Represents a valid state category.
 *
 * @param id
 */
class StateCategory(id: String) extends ReferencePDX[StateCategoryDefinition](() => StateCategories.list, id) {
  /* init */
  require(isValidID(id), s"Invalid state category identifier: $id")

  def this(node: Node) = {
    this(node.name)
    loadPDX(node)
  }

  def this(stateCategoryDef: StateCategoryDefinition) = {
    this(stateCategoryDef.name)
  }

  def sameStateCategory(stateCategory: StateCategory): Boolean = this.isValidID(stateCategory.identifier)

  def sameStateCategory(identifier: String): Boolean = this.isValidID(identifier)

  def identifier: String = this.pdxIdentifier
}
/*
 * StateCategory File
 * //todo refactor stuff here
 */

object StateCategory {

  def list: List[StateCategory] = {
    StateCategories.list.map(sc_def => new StateCategory(sc_def))
  }

  def apply(): PDXSupplier[StateCategory] = {
    new PDXSupplier[StateCategory] {
      override def simplePDXSupplier(): Option[Node => Option[StateCategory]] = {
        Some((expr: Node) => {
          Some(new StateCategory(expr))
        })
      }

      override def blockPDXSupplier(): Option[Node => Option[StateCategory]] = {
        Some((expr: Node) => {
          Some(new StateCategory(expr))
        })
      }
    }
  }
}