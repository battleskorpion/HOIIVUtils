package com.hoi4utils.clausewitz.code.effect

import com.hoi4utils.clausewitz.code.scope.ScopeType
import com.hoi4utils.clausewitz.script.*

import scala.collection.mutable

// todo with StructuredPDX
// extends StructuredPDX(_identifiers)  todo???
trait BlockEffect(private val structuredBlock: StructuredPDX) extends Effect with Cloneable {
  _supportedScopes = Set(ScopeType.country) // Example scope types
  _supportedTargets = Set(ScopeType.state) // Example target types

  //override def isScope: Boolean = true

  //override protected def childScripts: mutable.Iterable[? <: PDXScript[?]]

  override def clone(): AnyRef = {
    super.clone().asInstanceOf[BlockEffect]
  }
}