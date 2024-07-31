package com.hoi4utils.clausewitz.code.effect

import com.hoi4utils.clausewitz.script._
import com.hoi4utils.clausewitz.code.scope.ScopeType

trait SimpleEffect(private val identifier: String, private val tSupplier: () => ReferencePDX[Effect]) extends Effect {
  super.identifier = identifier
  super.tSupplier = tSupplier

  supportedScopes = Set(ScopeType.country, ScopeType.state) // Example scope types
  supportedTargets = Set(ScopeType.country) // Example target types

  //override def isScope: Boolean = true
}