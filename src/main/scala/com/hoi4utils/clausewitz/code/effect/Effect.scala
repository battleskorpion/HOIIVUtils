package com.hoi4utils.clausewitz.code.effect

import com.hoi4utils.clausewitz.code.scope.{Scope, ScopeType}
import com.hoi4utils.clausewitz.script._
import org.jetbrains.annotations.NotNull

import scala.collection.mutable

// todo should be sealed? 
// todo  with AbstractPDX[Effect](identifiers) ????
trait Effect extends ScopedPDXScript with PDXScript[?] with Cloneable {
  
//  protected var tSupplier: () => ReferencePDX[Effect]
//  protected var structuredBlock: StructuredPDX

  protected var _definitionScope: Option[Scope] = None
  protected var _targetScope: Option[Scope] = None
  protected var _supportedScopes: Set[ScopeType] = Set.empty
  protected var _supportedTargets: Set[ScopeType] = Set.empty
  
  override def supportedScopes: Set[ScopeType] = _supportedScopes

  def supportedTargets: Set[ScopeType] = _supportedTargets

  def hasSupportedTargets: Boolean = _supportedTargets.nonEmpty

  def setTarget(target: Scope): Unit = {
    this._targetScope = Some(target)
  }

  def setTarget(string: String, within: Scope): Unit = {
    setTarget(Scope.of(string, within))
  }

  override def definitionScope: Option[Scope] = _definitionScope

  def targetScope: Option[Scope] = _targetScope

  def target: String = _targetScope.map(_.name).getOrElse("[null target]")

  def hasTarget: Boolean = _targetScope.isDefined

  def isScope: Boolean = false

  @throws[CloneNotSupportedException]
  override def clone(): AnyRef = {
    val clone = super.clone().asInstanceOf[Effect]
    clone._definitionScope = _definitionScope
    clone._targetScope = _targetScope
    clone._supportedScopes = _supportedScopes
    clone._supportedTargets = _supportedTargets
    clone
  }
}
