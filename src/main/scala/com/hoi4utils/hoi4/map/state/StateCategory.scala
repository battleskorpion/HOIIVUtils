//package com.hoi4utils.hoi4.map.state
//
//import com.hoi4utils.hoi4.map.state.StateCategoryDefinition
//import com.hoi4utils.parser.{Node, PDXValueNode, SeqNode}
//import com.hoi4utils.script.*
///**
// * Represents a valid state category.
// *
// * @param id
// */
//class StateCategory(id: String) extends ReferencePDX[String, StateCategoryDefinition](() => StateCategories.list, id) {
//  /* init */
//  require(isValidID(id), s"Invalid state category identifier: $id")
//
//  def this(node: PDXValueNode[?]) = {
//    this(node.name)
//    var file = None
//    // TODO TODO 
////    loadPDX(node, None)
//  }
//
//  def this(stateCategoryDef: StateCategoryDefinition) = {
//    this(stateCategoryDef.name)
//  }
//
//  def sameStateCategory(stateCategory: StateCategory): Boolean = this.isValidID(stateCategory.identifier)
//
//  def sameStateCategory(identifier: String): Boolean = this.isValidID(identifier)
//
//  def identifier: String = this.pdxIdentifier
//}
///*
// * StateCategory File
// * //todo refactor stuff here
// */
//
//object StateCategory {
//
//  def list: List[StateCategory] = {
//    StateCategories.list.map(sc_def => new StateCategory(sc_def))
//  }
//
//  def apply(): PDXSupplier[StateCategory] = {
//    new PDXSupplier[StateCategory] {
//      override def simplePDXSupplier(): Option[PDXValueNode[?] => Option[StateCategory]] = {
//        Some((expr: PDXValueNode[?]) => {
//          Some(new StateCategory(expr))
//        })
//      }
//
//      override def blockPDXSupplier(): Option[SeqNode => Option[StateCategory]] = {
//        // TODO TODO 
//        None 
////        Some((expr: SeqNode) => {
////          Some(new StateCategory(expr))
////        })
//      }
//    }
//  }
//}
