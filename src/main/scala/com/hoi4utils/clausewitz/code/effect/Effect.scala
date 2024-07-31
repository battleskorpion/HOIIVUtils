package com.hoi4utils.clausewitz.code.effect

import com.hoi4utils.clausewitz.code.scope.{Scope, ScopeType}
import com.hoi4utils.clausewitz.script._
import org.jetbrains.annotations.NotNull

import scala.collection.mutable

// todo should be sealed? 
trait Effect extends ScopedPDXScript with PDXScript[?] {

//  protected var tSupplier: () => ReferencePDX[Effect]
//  protected var structuredBlock: StructuredPDX

  protected var definitionScope: Option[Scope] = None
  protected var targetScope: Option[Scope] = None
  protected var supportedScopes: Set[ScopeType] = Set.empty
  protected var supportedTargets: Set[ScopeType] = Set.empty

  def identifier: String

  override def getSupportedScopes: Set[ScopeType] = supportedScopes

  override def getSupportedTargets: Set[ScopeType] = supportedTargets

  override def hasSupportedTargets: Boolean = supportedTargets.nonEmpty

  override def setTarget(target: Scope): Unit = {
    this.targetScope = Some(target)
  }

  override def setTarget(string: String, within: Scope): Unit = {
    setTarget(Scope.of(string, within))
  }

  override def getDefinitionScope: Option[Scope] = definitionScope

  override def getTargetScope: Option[Scope] = targetScope

  def target: String = targetScope.map(_.name).getOrElse("[null target]")

  override def hasTarget: Boolean = targetScope.isDefined

  def isScope: Boolean = false
}
