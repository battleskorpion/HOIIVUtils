package com.hoi4utils.clausewitz.code.effect

import com.hoi4utils.clausewitz.code.scope.ScopeType
import com.hoi4utils.clausewitz.script._

trait BlockEffect(private val _identifier: String, private val structuredBlock: StructuredPDX)
  extends StructuredPDX with Effect {
  _supportedScopes = Set(ScopeType.country) // Example scope types
  _supportedTargets = Set(ScopeType.state) // Example target types

  //override def isScope: Boolean = true
  
  override def identifier: String = _identifier
}