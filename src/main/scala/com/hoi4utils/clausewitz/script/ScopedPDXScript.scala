package com.hoi4utils.clausewitz.script

import com.hoi4utils.clausewitz.code.scope.Scope
import com.hoi4utils.clausewitz.code.scope.ScopeType

trait ScopedPDXScript {
  def getDefinitionScope: Scope

  def isSupportedInScope(scope: Scope): Boolean = {
    if (supportedScopes.contains(ScopeType.any)) return true
    supportedScopes.contains(scope.targetScopeType)
  }

  def supportedScopes: Iterable[ScopeType]
}
