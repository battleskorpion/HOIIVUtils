package com.map

import com.hoi4utils.parser.Node
import com.hoi4utils.script.{PDXSupplier, ReferencePDX}
import com.map.StateCategories.stateCategoriesErrors

/**
 * Represents a valid state category.
 *
 * @param id pdx identifier for the state category
 */
@throws[IllegalArgumentException]
class StateCategory(id: String | Node | StateCategoryDef)
  extends ReferencePDX[StateCategoryDef](
    () => StateCategories.list,
    s => Some(s.name),
    id match
      case s: String => s
      case n: Node => n.name
      case d: StateCategoryDef => d.name
  ):

  require(isValidID(id match
    case s: String => s
    case n: Node => n.name
    case d: StateCategoryDef => d.name
  ), s"Invalid state category identifier: $id")

  id match
    case n: Node => loadPDX(n, stateCategoriesErrors)
    case _ => ()

  def sameStateCategory(stateCategory: StateCategory): Boolean = this.isValidID(stateCategory.identifier)
  def sameStateCategory(identifier: String): Boolean = this.isValidID(identifier)
  def identifier: String = this.pdxIdentifier

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