package com.hoi4utils.clausewitz.code.effect

import com.hoi4utils.clausewitz.script._
import com.hoi4utils.clausewitz.code.scope.ScopeType

// todo
trait SimpleEffect(private val _identifier: String, private val tSupplier: () => ReferencePDX[Effect]) 
  extends AbstractPDX[Effect] with Effect {
  
  _supportedScopes = Set(ScopeType.country, ScopeType.state) // Example scope types
  _supportedTargets = Set(ScopeType.country) // Example target types

  //override def isScope: Boolean = true

  override def identifier: String = _identifier

}