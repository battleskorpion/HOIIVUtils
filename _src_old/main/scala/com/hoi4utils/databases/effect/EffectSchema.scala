package com.hoi4utils.databases.effect

import com.hoi4utils.hoi4mod.scope.ScopeType


final class EffectSchema(pdxIdentifiers: List[String], supportedScopes: Set[ScopeType], supportedTargets: Set[ScopeType]) {
  def this(pdxIdentifier: String, supportedScopes: Set[ScopeType]) = {
    this(List(pdxIdentifier), supportedScopes, Set.empty)
  }

  def this(pdxIdentifier: String, supportedScopes: Option[Set[ScopeType]], supportedTargets: Option[Set[ScopeType]]) = {
    this(List(pdxIdentifier), supportedScopes.get, supportedTargets.get)
  }
}
