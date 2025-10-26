package com.hoi4utils.script

import com.hoi4utils.hoi4.scope.{Scope, ScopeType}

trait ScopedPDXScript {
  def definitionScope: Option[Scope]

  def isSupportedInScope(scope: Scope): Boolean = {
    if (supportedScopes.exists(_ == ScopeType.any)) return true
    supportedScopes.exists(_ == scope.targetScopeType)
  }

  def supportedScopes: Iterable[ScopeType]
}
