package com.HOIIVUtils.hoi4utils.clausewitz_code.scope;

import com.HOIIVUtils.hoi4utils.clausewitz_code.HOI4Script;
import com.HOIIVUtils.hoi4utils.clausewitz_code.effect.Effect;
import com.HOIIVUtils.hoi4utils.clausewitz_data.country.CountryTag;
import com.HOIIVUtils.hoi4utils.clausewitz_data.state.State;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;

public class Scope implements Cloneable, HOI4Script {
	public static HashMap<String, Scope> scopes = new HashMap<>();

	public final String name;
	private final EnumSet<ScopeType> withinScopeAllowed;     // 'from' (in the context of)
//	// usually 'country' or 'any'
	private final ScopeType targetScopeType;             // 'to'   (targeting)
	public final ScopeCategory scopeCategory;
	private List<HOI4Script> content;

	private Scope withinScope = null;
	//private Scope targetScope = null;   // null if is the target?

	protected Scope(String name, ScopeType fromScopeAllowed, ScopeType targetScopeType,
	                ScopeCategory scopeCategory) {
		this(name, EnumSet.of(fromScopeAllowed), targetScopeType, scopeCategory);
	}

	protected Scope(String name, EnumSet<ScopeType> withinScopeAllowed, ScopeType targetScopeType,
	                ScopeCategory scopeCategory) {
		this.name = name;
		this.withinScopeAllowed = withinScopeAllowed;
		this.targetScopeType = targetScopeType;
		this.scopeCategory = scopeCategory;

		scopes.put(name, this);
	}

	public static Scope of(String name, Scope within) throws NotPermittedInScopeException {
		try {
			int id = Integer.parseInt(name);
			if (within.canTargetCountry()) {
				// state?
				// State.isValidStateID(id)
//				System.out.println(name + ", ?");
				Scope state_scope = of(State.get(id));
				if (state_scope == null) {
					System.out.println("invalid state id: " + id + ", in Scope.of()");
					return null;
				}
				state_scope.setWithin(within);
				return state_scope;
			}
		} catch (NumberFormatException ignored) {

		}
		Scope scope = getClone(name);
		if (scope == null) return null;
		scope.setWithin(within);
//		scope.setTarget(target);

		return scope;
	}

	// todo hopefully theres no conflicts here? or refactor necessary here
	// dont think its possible that there would be.
	public static Scope of(CountryTag countryTag) {
		if (countryTag == null) {
			return null;
		}
		String tag = countryTag.toString();
		Scope s = getClone(tag);
		if (s == null) {
			return new Scope(countryTag.toString(), ScopeType.any, ScopeType.country, ScopeCategory.DUAL);
		} else {
			return s;
		}
	}

	private static Scope of(State state) {
		if (state == null) {
			return null;
		}
		String state_str = state.id() + "@state";
		Scope s = getClone(state_str);
		if (s == null) {
			return new Scope(state.id() + "@state", ScopeType.any, ScopeType.state, ScopeCategory.DUAL);
		} else {
			return s;
		}
	}

	public static Scope of(Effect effect) {
		if (effect == null || !effect.isScope()) {
			return null;
		}
		String effect_identifier = effect.name();
		Scope s = getClone(effect_identifier);
		if (s == null) {
			return null;
		} else {
			return s;
		}
	}

	private void setWithin(Scope scope) throws NotPermittedInScopeException {
		if (!permittedWithinScope(scope)) {
			throw new NotPermittedInScopeException("From scope is not allowed for this scope");
		}
		withinScope = scope;
	}

	public void setContains(List<HOI4Script> script) {
		this.content = script;
	}

	public void setContains(HOI4Script script) {
		this.content = new ArrayList<>();
		content.add(script);
	}

	private boolean permittedWithinScope(Scope scope) {
		if (withinScopeAllowed.contains(ScopeType.any)) {
			return true;
		} else {
			return withinScopeAllowed.contains(scope.targetScopeType);
		}
	}

//	private void setTarget(Scope target) throws Exception {
//		if (!target.withinScopeAllowed.contains(targetScopeType)) { // todo ?
//			throw new Exception("From scope is not allowed for this scope");
//		}
//		target.setWithin(this);
//		//targetScope = target;
//	}

	private boolean canTargetCountry() {
		return targetScopeType.equals(ScopeType.country);
	}

	private static Scope getClone(String name) {
		Scope clone;
		try {
			Scope scope = scopes.get(name);
			if (scope == null) return null;
			clone = (Scope) scope.clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
		return clone;
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		Scope c = (Scope) super.clone();
		return c;
	}

	@Override
	public String toString() {
		return name;
	}

	/* base scopes */ // todo! double check scope categories esp ones that are supposedly 'effect'
	// todo: <character>
	// tag, state id?
	// mio: <?>
	// root, this, prev, from?
	// event target, var:<var>?
	static {
		new Scope("all_unit_leader",                ScopeType.country,  ScopeType.leader,       ScopeCategory.TRIGGER);
		new Scope("any_unit_leader",                ScopeType.country,  ScopeType.leader,       ScopeCategory.TRIGGER);
		new Scope("all_army_leader",                ScopeType.country,  ScopeType.leader,       ScopeCategory.TRIGGER);
		new Scope("any_army_leader",                ScopeType.country,  ScopeType.leader,       ScopeCategory.TRIGGER);
		new Scope("all_navy_leader",                ScopeType.country,  ScopeType.leader,       ScopeCategory.TRIGGER);
		new Scope("any_navy_leader",                ScopeType.country,  ScopeType.leader,       ScopeCategory.TRIGGER);
		new Scope("random_unit_leader",             ScopeType.country,  ScopeType.leader,       ScopeCategory.EFFECT);
		new Scope("every_unit_leader",              ScopeType.country,  ScopeType.leader,       ScopeCategory.EFFECT);
		new Scope("random_army_leader",             ScopeType.country,  ScopeType.leader,       ScopeCategory.EFFECT);
		new Scope("every_army_leader",              ScopeType.country,  ScopeType.leader,       ScopeCategory.EFFECT);
		new Scope("random_navy_leader",             ScopeType.country,  ScopeType.leader,       ScopeCategory.EFFECT);
		new Scope("every_navy_leader",              ScopeType.country,  ScopeType.leader,       ScopeCategory.EFFECT);
		new Scope("global_every_army_leader",       ScopeType.any,      ScopeType.leader,       ScopeCategory.EFFECT);
		new Scope("overlord",                       ScopeType.country,  ScopeType.country,      ScopeCategory.DUAL);
		new Scope("faction_leader",                 ScopeType.country,  ScopeType.country,      ScopeCategory.DUAL);
		// scoepDef("TAG");
		new Scope(CountryTag.NULL_TAG.toString(),         ScopeType.any,      ScopeType.country,      ScopeCategory.DUAL);
		new Scope("any_country",                    ScopeType.any,      ScopeType.country,      ScopeCategory.TRIGGER);
		new Scope("any_country_with_original_tag",  ScopeType.any,      ScopeType.country,      ScopeCategory.TRIGGER);
		new Scope("any_neighbor_country",           ScopeType.country,  ScopeType.country,      ScopeCategory.TRIGGER);
		new Scope("any_home_area_neighbor_country", ScopeType.country,  ScopeType.country,      ScopeCategory.TRIGGER);
		new Scope("any_guaranteed_country",         ScopeType.country,  ScopeType.country,      ScopeCategory.TRIGGER);
		new Scope("any_allied_country",             ScopeType.country,  ScopeType.country,      ScopeCategory.TRIGGER);
		new Scope("any_other_country",              ScopeType.country,  ScopeType.country,      ScopeCategory.TRIGGER);
		new Scope("any_enemy_country",              ScopeType.country,  ScopeType.country,      ScopeCategory.TRIGGER);
		new Scope("any_occupied_country",           ScopeType.country,  ScopeType.country,      ScopeCategory.TRIGGER);
		new Scope("all_neighbor_country",           ScopeType.country,  ScopeType.country,      ScopeCategory.TRIGGER);
		new Scope("all_country",                    ScopeType.any,      ScopeType.country,      ScopeCategory.TRIGGER);
		new Scope("all_country_with_original_tag",  ScopeType.any,      ScopeType.country,      ScopeCategory.TRIGGER);
		new Scope("all_allied_country",             ScopeType.country,  ScopeType.country,      ScopeCategory.TRIGGER);
		new Scope("all_guaranteed_country",         ScopeType.country,  ScopeType.country,      ScopeCategory.TRIGGER);
		new Scope("all_enemy_country",              ScopeType.country,  ScopeType.country,      ScopeCategory.TRIGGER);
		new Scope("all_occupied_country",           ScopeType.country,  ScopeType.country,      ScopeCategory.TRIGGER);
		// scopeDef("state_id");
		new Scope("any_state",                      ScopeType.any,      ScopeType.state,        ScopeCategory.TRIGGER);
		new Scope("any_controlled_state",           ScopeType.country,  ScopeType.state,        ScopeCategory.TRIGGER);
		new Scope("any_owned_state",                ScopeType.country,  ScopeType.state,        ScopeCategory.TRIGGER);
		new Scope("any_neighbor_state",             ScopeType.state,    ScopeType.state,        ScopeCategory.TRIGGER);
		new Scope("all_state",                      ScopeType.any,      ScopeType.state,        ScopeCategory.TRIGGER);
		new Scope("all_controlled_state",           ScopeType.country,  ScopeType.state,        ScopeCategory.TRIGGER);
		new Scope("all_owned_state",                ScopeType.country,  ScopeType.state,        ScopeCategory.TRIGGER);
		new Scope("all_neighbor_state",             ScopeType.state,    ScopeType.state,        ScopeCategory.TRIGGER);
		new Scope("any_country_with_core",          ScopeType.state,    ScopeType.country,      ScopeCategory.TRIGGER);
		new Scope("any_country_division",           ScopeType.country,  ScopeType.division,     ScopeCategory.TRIGGER);
		new Scope("any_state_division",             ScopeType.state,    ScopeType.division,     ScopeCategory.TRIGGER);
		new Scope("all_subject_countries",          ScopeType.country,  ScopeType.country,      ScopeCategory.TRIGGER);
		new Scope("any_subject_country",            ScopeType.country,  ScopeType.country,      ScopeCategory.TRIGGER);
		new Scope("all_core_state",                 ScopeType.country,  ScopeType.state,        ScopeCategory.TRIGGER);
		new Scope("any_core_state",                 ScopeType.country,  ScopeType.state,        ScopeCategory.TRIGGER);
		new Scope("all_character",                  ScopeType.country,  ScopeType.character,    ScopeCategory.TRIGGER);
		new Scope("any_character",                  ScopeType.country,  ScopeType.character,    ScopeCategory.TRIGGER);
		new Scope("every_country",                  ScopeType.any,      ScopeType.country,      ScopeCategory.EFFECT);
		new Scope("every_country_with_original_tag", ScopeType.any,     ScopeType.country,      ScopeCategory.EFFECT);
		new Scope("every_other_country",            ScopeType.country,  ScopeType.country,      ScopeCategory.EFFECT);
		new Scope("every_neighbor_country",         ScopeType.country,  ScopeType.country,      ScopeCategory.EFFECT);
		new Scope("every_enemy_country",            ScopeType.country,  ScopeType.country,      ScopeCategory.EFFECT);
		new Scope("every_occupied_country",         ScopeType.country,  ScopeType.country,      ScopeCategory.EFFECT);
		new Scope("random_country",                 ScopeType.any,      ScopeType.country,      ScopeCategory.EFFECT);
		new Scope("random_country_with_original_tag", ScopeType.any,    ScopeType.country,      ScopeCategory.EFFECT);
		new Scope("random_neighbor_country",        ScopeType.country,  ScopeType.country,      ScopeCategory.EFFECT);
		new Scope("random_enemy_country",           ScopeType.country,  ScopeType.country,      ScopeCategory.EFFECT);
		new Scope("random_occupied_country",        ScopeType.country,  ScopeType.country,      ScopeCategory.EFFECT);
		new Scope("random_state",                   ScopeType.any,      ScopeType.state,        ScopeCategory.EFFECT);
		new Scope("random_owned_state",             ScopeType.country,  ScopeType.state,        ScopeCategory.EFFECT);
		new Scope("random_controlled_state",        ScopeType.country,  ScopeType.state,        ScopeCategory.EFFECT);
		new Scope("random_owned_controlled_state",  ScopeType.country,  ScopeType.state,        ScopeCategory.EFFECT);
		new Scope("random_neighbor_state",          ScopeType.state,    ScopeType.state,        ScopeCategory.EFFECT);
		new Scope("every_state",                    ScopeType.any,      ScopeType.state,        ScopeCategory.EFFECT);
		new Scope("every_controlled_state",         ScopeType.country,  ScopeType.state,        ScopeCategory.EFFECT);
		new Scope("every_owned_state",              ScopeType.country,  ScopeType.state,        ScopeCategory.EFFECT);
		new Scope("every_neighbor_state",           ScopeType.state,    ScopeType.state,        ScopeCategory.EFFECT);
		new Scope("capital_scope",                  ScopeType.country,  ScopeType.state,        ScopeCategory.DUAL);
		new Scope("controller",                     ScopeType.state,    ScopeType.country,      ScopeCategory.DUAL);
		new Scope("all_operative_leader",           ScopeType.country,  ScopeType.operative,    ScopeCategory.TRIGGER);
		new Scope("any_operative_leader",           ScopeType.country,  ScopeType.operative,    ScopeCategory.TRIGGER);
		new Scope("every_operative",                ScopeType.country,  ScopeType.operative,    ScopeCategory.EFFECT);
		new Scope("random_operative",               ScopeType.country,  ScopeType.operative,    ScopeCategory.EFFECT);
		new Scope("every_country_division",         ScopeType.country,  ScopeType.division,     ScopeCategory.EFFECT);
		new Scope("random_country_division",        ScopeType.country,  ScopeType.division,     ScopeCategory.EFFECT);
		new Scope("every_state_division",           ScopeType.state,    ScopeType.division,     ScopeCategory.EFFECT);
		new Scope("random_state_division",          ScopeType.state,    ScopeType.division,     ScopeCategory.EFFECT);
		new Scope("every_possible_country",         ScopeType.any,      ScopeType.country,      ScopeCategory.EFFECT);
		new Scope("every_subject_country",          ScopeType.country,  ScopeType.country,      ScopeCategory.EFFECT);
		new Scope("random_subject_country",         ScopeType.country,  ScopeType.country,      ScopeCategory.EFFECT);
		new Scope("every_core_state",               ScopeType.country,  ScopeType.state,        ScopeCategory.EFFECT);
		new Scope("random_core_state",              ScopeType.country,  ScopeType.state,        ScopeCategory.EFFECT);
		new Scope("every_character",                ScopeType.country,  ScopeType.character,    ScopeCategory.EFFECT);
		new Scope("random_character",               ScopeType.country,  ScopeType.character,    ScopeCategory.EFFECT);

		/* special */
		new Scope("owner", EnumSet.of(ScopeType.state, ScopeType.character, ScopeType.combatant),
				ScopeType.country, ScopeCategory.DUAL);

		/* array scopes */
		new Scope("any_of_scopes",           ScopeType.any, ScopeType.any, ScopeCategory.TRIGGER);
		new Scope("all_of_scopes",           ScopeType.any, ScopeType.any, ScopeCategory.TRIGGER);
		new Scope("for_each_scope_loop",     ScopeType.any, ScopeType.any, ScopeCategory.EFFECT);
		new Scope("random_scope_in_array",   ScopeType.any, ScopeType.any, ScopeCategory.EFFECT);
	}

	public ScopeType targetScopeType() {
		return targetScopeType;
	}

	public boolean isPotentialEffectTarget() {
		return scopeCategory == ScopeCategory.EFFECT || scopeCategory == ScopeCategory.DUAL;
	}

	@Override
	public String displayScript() {
		return displayScript(1);
	}

	public String displayScript(int tabs) {
		if (content == null || content.isEmpty()) {
			return name;
		} else {
			StringBuilder s = new StringBuilder();

			s.append("\t".repeat(Math.max(0, tabs - 1)));
			s.append(name);
			s.append(" = ");

			s.append("{\n");
			for (HOI4Script p : content) {
				s.append("\t".repeat(tabs));
				s.append(p == null ? "[scope parameter was null]" : p.displayScript());
				s.append("\n");
			}
			s.append("\t".repeat(Math.max(0, tabs - 1)));
			s.append("}\n");
			return s.toString();
		}
	}

}


