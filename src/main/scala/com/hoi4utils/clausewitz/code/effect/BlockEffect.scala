package com.hoi4utils.clausewitz.code.effect

import com.hoi4utils.clausewitz.code.scope.ScopeType
import com.hoi4utils.clausewitz.script._

trait BlockEffect(private val identifier: String, private val structuredBlock: StructuredPDX) extends Effect {
  super.identifier = identifier
  super.structuredBlock = structuredBlock

  supportedScopes = Set(ScopeType.country) // Example scope types
  supportedTargets = Set(ScopeType.state) // Example target types

  //override def isScope: Boolean = true
}