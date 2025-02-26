package com.hoi4utils.clausewitz.code.effect

import com.hoi4utils.clausewitz.code.scope.ScopeType
import com.hoi4utils.clausewitz.script.*

// private val tSupplier: () => Effect  // todo
// extends AbstractPDX[T](identifiers)  todo? 
// todo
trait SimpleEffect extends Effect {
  
  _supportedScopes = Set(ScopeType.country, ScopeType.state) // Example scope types
  _supportedTargets = Set(ScopeType.country) // Example target types

  override def clone(): AnyRef = {
    super.clone().asInstanceOf[SimpleEffect]
  }
}