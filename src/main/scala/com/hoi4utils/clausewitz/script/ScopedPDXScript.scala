package com.hoi4utils.clausewitz.script

import com.hoi4utils.clausewitz.code.scope.Scope
import com.hoi4utils.clausewitz.code.scope.ScopeType

trait ScopedPDXScript {
  def definitionScope: Scope

  def isSupportedInScope(scope: Scope): Boolean = {
    if (supportedScopes.exists(_ == ScopeType.any)) return true
    supportedScopes.exists(_ == scope.targetScopeType)
  }

  def supportedScopes: Iterable[ScopeType]
}
