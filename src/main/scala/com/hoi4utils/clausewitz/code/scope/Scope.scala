package com.hoi4utils.clausewitz.code.scope

import com.hoi4utils.clausewitz.code.effect.Effect
import com.hoi4utils.clausewitz.data.country.CountryTag
import com.hoi4utils.clausewitz.map.state.State
import com.hoi4utils.clausewitz.script.PDXScript

import scala.collection.mutable
import scala.collection.mutable.HashMap
import scala.Cloneable


object Scope {
  var scopes = new mutable.HashMap[String, Scope]

  @throws[NotPermittedInScopeException]
  def of(name: String, within: Scope): Scope = {
    try {
      val id = name.toInt
      if (within.canTargetCountry) {
        // state?
        // State.isValidStateID(id)
        //				System.out.println(name + ", ?");
        val state_scope = of(State.get(id))
        if (state_scope == null) {
          System.out.println("invalid state id: " + id + ", in Scope.of()")
          return null
        }
        state_scope.setWithin(within)
        return state_scope
      }
    } catch {
      case ignored: NumberFormatException =>

    }
    val scope = getClone(name)
    if (scope == null) return null
    scope.setWithin(within)
    //		scope.setTarget(target);
    scope
  }

  // todo hopefully theres no conflicts here? or refactor necessary here
  // dont think its possible that there would be.
  def of(countryTag: CountryTag): Scope = {
    if (countryTag == null) return null
    val tag = countryTag.toString
    val s = getClone(tag)
    if (s == null) new Scope(countryTag.toString, ScopeType.any, ScopeType.country, ScopeCategory.DUAL)
    else s
  }

  private def of(state: State): Scope = {
    if (state == null) return null
    val state_str = state.id + "@state"
    val s = getClone(state_str)
    if (s == null) new Scope(state.id + "@state", ScopeType.any, ScopeType.state, ScopeCategory.DUAL)
    else s
  }

  def of(effect: Effect): Scope = {
    if (effect == null || !effect.isScope) return null
    val effect_identifier = effect.getPDXIdentifier
    val s = getClone(effect_identifier)
    if (s == null) null
    else s
  }

  def getClone(str: String): Scope = {
    scopes.find(_._1 == str) match {
      case Some((_, scope)) => scope.clone()
      case None => null
    }
  }

  // TODO: add catch?
  try
    new Scope("all_unit_leader", ScopeType.country, ScopeType.leader, ScopeCategory.TRIGGER)
    new Scope("any_unit_leader", ScopeType.country, ScopeType.leader, ScopeCategory.TRIGGER)
    new Scope("all_army_leader", ScopeType.country, ScopeType.leader, ScopeCategory.TRIGGER)
    new Scope("any_army_leader", ScopeType.country, ScopeType.leader, ScopeCategory.TRIGGER)
    new Scope("all_navy_leader", ScopeType.country, ScopeType.leader, ScopeCategory.TRIGGER)
    new Scope("any_navy_leader", ScopeType.country, ScopeType.leader, ScopeCategory.TRIGGER)
    new Scope("random_unit_leader", ScopeType.country, ScopeType.leader, ScopeCategory.EFFECT)
    new Scope("every_unit_leader", ScopeType.country, ScopeType.leader, ScopeCategory.EFFECT)
    new Scope("random_army_leader", ScopeType.country, ScopeType.leader, ScopeCategory.EFFECT)
    new Scope("every_army_leader", ScopeType.country, ScopeType.leader, ScopeCategory.EFFECT)
    new Scope("random_navy_leader", ScopeType.country, ScopeType.leader, ScopeCategory.EFFECT)
    new Scope("every_navy_leader", ScopeType.country, ScopeType.leader, ScopeCategory.EFFECT)
    new Scope("global_every_army_leader", ScopeType.any, ScopeType.leader, ScopeCategory.EFFECT)
    new Scope("overlord", ScopeType.country, ScopeType.country, ScopeCategory.DUAL)
    new Scope("faction_leader", ScopeType.country, ScopeType.country, ScopeCategory.DUAL)
    // scoepDef("TAG");
    new Scope(CountryTag.NULL_TAG.toString, ScopeType.any, ScopeType.country, ScopeCategory.DUAL)
    new Scope("any_country", ScopeType.any, ScopeType.country, ScopeCategory.TRIGGER)
    new Scope("any_country_with_original_tag", ScopeType.any, ScopeType.country, ScopeCategory.TRIGGER)
    new Scope("any_neighbor_country", ScopeType.country, ScopeType.country, ScopeCategory.TRIGGER)
    new Scope("any_home_area_neighbor_country", ScopeType.country, ScopeType.country, ScopeCategory.TRIGGER)
    new Scope("any_guaranteed_country", ScopeType.country, ScopeType.country, ScopeCategory.TRIGGER)
    new Scope("any_allied_country", ScopeType.country, ScopeType.country, ScopeCategory.TRIGGER)
    new Scope("any_other_country", ScopeType.country, ScopeType.country, ScopeCategory.TRIGGER)
    new Scope("any_enemy_country", ScopeType.country, ScopeType.country, ScopeCategory.TRIGGER)
    new Scope("any_occupied_country", ScopeType.country, ScopeType.country, ScopeCategory.TRIGGER)
    new Scope("all_neighbor_country", ScopeType.country, ScopeType.country, ScopeCategory.TRIGGER)
    new Scope("all_country", ScopeType.any, ScopeType.country, ScopeCategory.TRIGGER)
    new Scope("all_country_with_original_tag", ScopeType.any, ScopeType.country, ScopeCategory.TRIGGER)
    new Scope("all_allied_country", ScopeType.country, ScopeType.country, ScopeCategory.TRIGGER)
    new Scope("all_guaranteed_country", ScopeType.country, ScopeType.country, ScopeCategory.TRIGGER)
    new Scope("all_enemy_country", ScopeType.country, ScopeType.country, ScopeCategory.TRIGGER)
    new Scope("all_occupied_country", ScopeType.country, ScopeType.country, ScopeCategory.TRIGGER)
    // scopeDef("state_id");
    new Scope("any_state", ScopeType.any, ScopeType.state, ScopeCategory.TRIGGER)
    new Scope("any_controlled_state", ScopeType.country, ScopeType.state, ScopeCategory.TRIGGER)
    new Scope("any_owned_state", ScopeType.country, ScopeType.state, ScopeCategory.TRIGGER)
    new Scope("any_neighbor_state", ScopeType.state, ScopeType.state, ScopeCategory.TRIGGER)
    new Scope("all_state", ScopeType.any, ScopeType.state, ScopeCategory.TRIGGER)
    new Scope("all_controlled_state", ScopeType.country, ScopeType.state, ScopeCategory.TRIGGER)
    new Scope("all_owned_state", ScopeType.country, ScopeType.state, ScopeCategory.TRIGGER)
    new Scope("all_neighbor_state", ScopeType.state, ScopeType.state, ScopeCategory.TRIGGER)
    new Scope("any_country_with_core", ScopeType.state, ScopeType.country, ScopeCategory.TRIGGER)
    new Scope("any_country_division", ScopeType.country, ScopeType.division, ScopeCategory.TRIGGER)
    new Scope("any_state_division", ScopeType.state, ScopeType.division, ScopeCategory.TRIGGER)
    new Scope("all_subject_countries", ScopeType.country, ScopeType.country, ScopeCategory.TRIGGER)
    new Scope("any_subject_country", ScopeType.country, ScopeType.country, ScopeCategory.TRIGGER)
    new Scope("all_core_state", ScopeType.country, ScopeType.state, ScopeCategory.TRIGGER)
    new Scope("any_core_state", ScopeType.country, ScopeType.state, ScopeCategory.TRIGGER)
    new Scope("all_character", ScopeType.country, ScopeType.character, ScopeCategory.TRIGGER)
    new Scope("any_character", ScopeType.country, ScopeType.character, ScopeCategory.TRIGGER)
    new Scope("every_country", ScopeType.any, ScopeType.country, ScopeCategory.EFFECT)
    new Scope("every_country_with_original_tag", ScopeType.any, ScopeType.country, ScopeCategory.EFFECT)
    new Scope("every_other_country", ScopeType.country, ScopeType.country, ScopeCategory.EFFECT)
    new Scope("every_neighbor_country", ScopeType.country, ScopeType.country, ScopeCategory.EFFECT)
    new Scope("every_enemy_country", ScopeType.country, ScopeType.country, ScopeCategory.EFFECT)
    new Scope("every_occupied_country", ScopeType.country, ScopeType.country, ScopeCategory.EFFECT)
    new Scope("random_country", ScopeType.any, ScopeType.country, ScopeCategory.EFFECT)
    new Scope("random_country_with_original_tag", ScopeType.any, ScopeType.country, ScopeCategory.EFFECT)
    new Scope("random_neighbor_country", ScopeType.country, ScopeType.country, ScopeCategory.EFFECT)
    new Scope("random_enemy_country", ScopeType.country, ScopeType.country, ScopeCategory.EFFECT)
    new Scope("random_occupied_country", ScopeType.country, ScopeType.country, ScopeCategory.EFFECT)
    new Scope("random_state", ScopeType.any, ScopeType.state, ScopeCategory.EFFECT)
    new Scope("random_owned_state", ScopeType.country, ScopeType.state, ScopeCategory.EFFECT)
    new Scope("random_controlled_state", ScopeType.country, ScopeType.state, ScopeCategory.EFFECT)
    new Scope("random_owned_controlled_state", ScopeType.country, ScopeType.state, ScopeCategory.EFFECT)
    new Scope("random_neighbor_state", ScopeType.state, ScopeType.state, ScopeCategory.EFFECT)
    new Scope("every_state", ScopeType.any, ScopeType.state, ScopeCategory.EFFECT)
    new Scope("every_controlled_state", ScopeType.country, ScopeType.state, ScopeCategory.EFFECT)
    new Scope("every_owned_state", ScopeType.country, ScopeType.state, ScopeCategory.EFFECT)
    new Scope("every_neighbor_state", ScopeType.state, ScopeType.state, ScopeCategory.EFFECT)
    new Scope("capital_scope", ScopeType.country, ScopeType.state, ScopeCategory.DUAL)
    new Scope("controller", ScopeType.state, ScopeType.country, ScopeCategory.DUAL)
    new Scope("all_operative_leader", ScopeType.country, ScopeType.operative, ScopeCategory.TRIGGER)
    new Scope("any_operative_leader", ScopeType.country, ScopeType.operative, ScopeCategory.TRIGGER)
    new Scope("every_operative", ScopeType.country, ScopeType.operative, ScopeCategory.EFFECT)
    new Scope("random_operative", ScopeType.country, ScopeType.operative, ScopeCategory.EFFECT)
    new Scope("every_country_division", ScopeType.country, ScopeType.division, ScopeCategory.EFFECT)
    new Scope("random_country_division", ScopeType.country, ScopeType.division, ScopeCategory.EFFECT)
    new Scope("every_state_division", ScopeType.state, ScopeType.division, ScopeCategory.EFFECT)
    new Scope("random_state_division", ScopeType.state, ScopeType.division, ScopeCategory.EFFECT)
    new Scope("every_possible_country", ScopeType.any, ScopeType.country, ScopeCategory.EFFECT)
    new Scope("every_subject_country", ScopeType.country, ScopeType.country, ScopeCategory.EFFECT)
    new Scope("random_subject_country", ScopeType.country, ScopeType.country, ScopeCategory.EFFECT)
    new Scope("every_core_state", ScopeType.country, ScopeType.state, ScopeCategory.EFFECT)
    new Scope("random_core_state", ScopeType.country, ScopeType.state, ScopeCategory.EFFECT)
    new Scope("every_character", ScopeType.country, ScopeType.character, ScopeCategory.EFFECT)
    new Scope("random_character", ScopeType.country, ScopeType.character, ScopeCategory.EFFECT)
    /* special */
    new Scope("owner", Set(ScopeType.state, ScopeType.character, ScopeType.combatant), ScopeType.country, ScopeCategory.DUAL)
    /* array scopes */
    new Scope("any_of_scopes", ScopeType.any, ScopeType.any, ScopeCategory.TRIGGER)
    new Scope("all_of_scopes", ScopeType.any, ScopeType.any, ScopeCategory.TRIGGER)
    new Scope("for_each_scope_loop", ScopeType.any, ScopeType.any, ScopeCategory.EFFECT)
    new Scope("random_scope_in_array", ScopeType.any, ScopeType.any, ScopeCategory.EFFECT)

}

class Scope(final val name: String, final val withinScopeAllowed: Set[ScopeType],
            final val targetScopeType: ScopeType, final val scopeCategory: ScopeCategory,
            private var withinScope: Option[Scope]) extends Cloneable {

  //private Scope targetScope = null;   // null if is the target?
  def this(name: String, fromScopeAllowed: ScopeType, targetScopeType: ScopeType, scopeCategory: ScopeCategory) = {
    this(name, Set(fromScopeAllowed), targetScopeType, scopeCategory, None)
  }

  def this(name: String, withinScopeAllowed: Set[ScopeType], targetScopeType: ScopeType, scopeCategory: ScopeCategory) = {
    this(name, withinScopeAllowed, targetScopeType, scopeCategory, None)
    //Scope.scopes.put(name, this)  // todo idfk
  }

  @throws[NotPermittedInScopeException]
  private def setWithin(scope: Scope): Unit = {
    if (!permittedWithinScope(scope)) throw new NotPermittedInScopeException("From scope is not allowed for this scope")
    withinScope = Some(scope)
  }

  private def permittedWithinScope(scope: Scope) = if (withinScopeAllowed.contains(ScopeType.any)) true
  else withinScopeAllowed.contains(scope.targetScopeType)

  private def canTargetCountry = targetScopeType == ScopeType.country

  override def toString: String = name

  def isPotentialEffectTarget: Boolean = (scopeCategory eq ScopeCategory.EFFECT) || (scopeCategory eq ScopeCategory.DUAL)

  def displayScript: String = displayScript(1)

  def displayScript(tabs: Int): String = null

  @throws[CloneNotSupportedException]
  override def clone(): Scope = {
    super.clone().asInstanceOf[Scope]
  }
}


