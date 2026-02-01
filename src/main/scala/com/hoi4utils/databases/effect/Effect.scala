package com.hoi4utils.databases.effect

import com.hoi4utils.hoi4.scope.{Scope, ScopeType}
import com.hoi4utils.script.{PDXScript, ScopedPDXScript}

import scala.annotation.nowarn

// todo should be sealed? 
// todo  with AbstractPDX[Effect](identifiers) ????
trait Effect extends ScopedPDXScript with PDXScript[?, ?] with Cloneable {
  protected var _definitionScope: Option[Scope] = None
  protected var _targetScope: Option[Scope] = None
  protected var _supportedScopes: Set[ScopeType] = Set.empty
  protected var _supportedTargets: Set[ScopeType] = Set.empty
  protected var parameterValueType: Option[ParameterValueType] = None
  
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

  def setParameterValueType(parameterValueType: ParameterValueType): Unit = {
    this.parameterValueType = Some(parameterValueType)
  }

  @nowarn
  def effectCategory: String = {
    parameterValueType match {
      case Some(valueType) => valueType match {
        case ParameterValueType.country => "diplomacy"
        case ace_type => "military"
        case ai_strategy => "military"
        case character => "character"
        case cw_bool => "misc"
        case cw_float => "misc"
        case cw_int => "misc"
        case cw_list => "misc"
        case cw_string => "misc"
        case cw_trait => "character"
        case decision => "decision"
        case doctrine_category => "tech"
        case flag => "misc"
        case idea => "idea"
        case mission => "military"
        case modifier => "modifier"
        case scope => "misc"
        case state => "map"
        case equipment => "military"
        case strategic_region => "map"
        case building => "map"
        case operation_token => "military"
        case ideology => "diplomacy"
        case sub_ideology => "diplomacy"
        case province => "map"
        case resource => "map"
        case tech_category => "tech"
        case advisor_slot => "country"
        case event => "country"
        case wargoal => "diplomacy"
      }
      case None => "unknown"
    }
  }

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
