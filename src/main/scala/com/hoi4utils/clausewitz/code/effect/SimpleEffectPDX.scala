package com.hoi4utils.clausewitz.code.effect

import com.hoi4utils.clausewitz.script._
import com.hoi4utils.clausewitz.code.scope.ScopeType

// private val tSupplier: () => Effect  // todo
// extends AbstractPDX[T](identifiers)  todo? 
// todo
trait SimpleEffectPDX extends Effect {
  
  _supportedScopes = Set(ScopeType.country, ScopeType.state) // Example scope types
  _supportedTargets = Set(ScopeType.country) // Example target types

}