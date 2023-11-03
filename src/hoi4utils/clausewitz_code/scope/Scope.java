package hoi4utils.clausewitz_code.scope;

import java.util.HashMap;

public class Scope {
	public static HashMap<String, Scope> scopes = new HashMap<>();

	public final String name;
	public ScopeType fromScope;     // 'from' (in the context of)
									// usually 'country' or 'any'
	public ScopeType targetScope;   // 'to'   (targeting)
	public ScopeCategory scopeCategory;

	protected Scope(String name, ScopeType fromScope, ScopeType targetScope, ScopeCategory scopeCategory) {
		this.name = name;
		this.fromScope = fromScope;
		this.targetScope = targetScope;
		this.scopeCategory = scopeCategory;

		scopes.put(name, this);
	}

	/* base scopes */ // todo! double check scope categories esp ones that are supposedly 'effect'
	static {
		new Scope("all_unit_leader",                ScopeType.COUNTRY,  ScopeType.LEADER,       ScopeCategory.TRIGGER);
		new Scope("any_unit_leader",                ScopeType.COUNTRY,  ScopeType.LEADER,       ScopeCategory.TRIGGER);
		new Scope("all_army_leader",                ScopeType.COUNTRY,  ScopeType.LEADER,       ScopeCategory.TRIGGER);
		new Scope("any_army_leader",                ScopeType.COUNTRY,  ScopeType.LEADER,       ScopeCategory.TRIGGER);
		new Scope("all_navy_leader",                ScopeType.COUNTRY,  ScopeType.LEADER,       ScopeCategory.TRIGGER);
		new Scope("any_navy_leader",                ScopeType.COUNTRY,  ScopeType.LEADER,       ScopeCategory.TRIGGER);
		new Scope("random_unit_leader",             ScopeType.COUNTRY,  ScopeType.LEADER,       ScopeCategory.EFFECT);
		new Scope("every_unit_leader",              ScopeType.COUNTRY,  ScopeType.LEADER,       ScopeCategory.EFFECT);
		new Scope("random_army_leader",             ScopeType.COUNTRY,  ScopeType.LEADER,       ScopeCategory.EFFECT);
		new Scope("every_army_leader",              ScopeType.COUNTRY,  ScopeType.LEADER,       ScopeCategory.EFFECT);
		new Scope("random_navy_leader",             ScopeType.COUNTRY,  ScopeType.LEADER,       ScopeCategory.EFFECT);
		new Scope("every_navy_leader",              ScopeType.COUNTRY,  ScopeType.LEADER,       ScopeCategory.EFFECT);
		new Scope("global_every_army_leader",       ScopeType.ANY,      ScopeType.LEADER,       ScopeCategory.EFFECT);
		new Scope("overlord",                       ScopeType.COUNTRY,  ScopeType.COUNTRY,      ScopeCategory.DUAL);
		new Scope("faction_leader",                 ScopeType.COUNTRY,  ScopeType.COUNTRY,      ScopeCategory.DUAL);
		// scoepDef("TAG");
		new Scope("any_country",                    ScopeType.ANY,      ScopeType.COUNTRY,      ScopeCategory.TRIGGER);
		new Scope("any_country_with_original_tag",  ScopeType.ANY,      ScopeType.COUNTRY,      ScopeCategory.TRIGGER);
		new Scope("any_neighbor_country",           ScopeType.COUNTRY,  ScopeType.COUNTRY,      ScopeCategory.TRIGGER);
		new Scope("any_home_area_neighbor_country", ScopeType.COUNTRY,  ScopeType.COUNTRY,      ScopeCategory.TRIGGER);
		new Scope("any_guaranteed_country",         ScopeType.COUNTRY,  ScopeType.COUNTRY,      ScopeCategory.TRIGGER);
		new Scope("any_allied_country",             ScopeType.COUNTRY,  ScopeType.COUNTRY,      ScopeCategory.TRIGGER);
		new Scope("any_other_country",              ScopeType.COUNTRY,  ScopeType.COUNTRY,      ScopeCategory.TRIGGER);
		new Scope("any_enemy_country",              ScopeType.COUNTRY,  ScopeType.COUNTRY,      ScopeCategory.TRIGGER);
		new Scope("any_occupied_country",           ScopeType.COUNTRY,  ScopeType.COUNTRY,      ScopeCategory.TRIGGER);
		new Scope("all_neighbor_country",           ScopeType.COUNTRY,  ScopeType.COUNTRY,      ScopeCategory.TRIGGER);
		new Scope("all_country",                    ScopeType.ANY,      ScopeType.COUNTRY,      ScopeCategory.TRIGGER);
		new Scope("all_country_with_original_tag",  ScopeType.ANY,      ScopeType.COUNTRY,      ScopeCategory.TRIGGER);
		new Scope("all_allied_country",             ScopeType.COUNTRY,  ScopeType.COUNTRY,      ScopeCategory.TRIGGER);
		new Scope("all_guaranteed_country",         ScopeType.COUNTRY,  ScopeType.COUNTRY,      ScopeCategory.TRIGGER);
		new Scope("all_enemy_country",              ScopeType.COUNTRY,  ScopeType.COUNTRY,      ScopeCategory.TRIGGER);
		new Scope("all_occupied_country",           ScopeType.COUNTRY,  ScopeType.COUNTRY,      ScopeCategory.TRIGGER);
		// scopeDef("state_id");
		new Scope("any_state",                      ScopeType.ANY,      ScopeType.STATE,        ScopeCategory.TRIGGER);
		new Scope("any_controlled_state",           ScopeType.COUNTRY,  ScopeType.STATE,        ScopeCategory.TRIGGER);
		new Scope("any_owned_state",                ScopeType.COUNTRY,  ScopeType.STATE,        ScopeCategory.TRIGGER);
		new Scope("any_neighbor_state",             ScopeType.STATE,    ScopeType.STATE,        ScopeCategory.TRIGGER);
		new Scope("all_state",                      ScopeType.ANY,      ScopeType.STATE,        ScopeCategory.TRIGGER);
		new Scope("all_controlled_state",           ScopeType.COUNTRY,  ScopeType.STATE,        ScopeCategory.TRIGGER);
		new Scope("all_owned_state",                ScopeType.COUNTRY,  ScopeType.STATE,        ScopeCategory.TRIGGER);
		new Scope("all_neighbor_state",             ScopeType.STATE,    ScopeType.STATE,        ScopeCategory.TRIGGER);
		new Scope("any_country_with_core",          ScopeType.STATE,    ScopeType.COUNTRY,      ScopeCategory.TRIGGER);
		new Scope("any_country_division",           ScopeType.COUNTRY,  ScopeType.DIVISION,     ScopeCategory.TRIGGER);
		new Scope("any_state_division",             ScopeType.STATE,    ScopeType.DIVISION,     ScopeCategory.TRIGGER);
		new Scope("all_subject_countries",          ScopeType.COUNTRY,  ScopeType.COUNTRY,      ScopeCategory.TRIGGER);
		new Scope("any_subject_country",            ScopeType.COUNTRY,  ScopeType.COUNTRY,      ScopeCategory.TRIGGER);
		new Scope("all_core_state",                 ScopeType.COUNTRY,  ScopeType.STATE,        ScopeCategory.TRIGGER);
		new Scope("any_core_state",                 ScopeType.COUNTRY,  ScopeType.STATE,        ScopeCategory.TRIGGER);
		new Scope("all_character",                  ScopeType.COUNTRY,  ScopeType.CHARACTER,    ScopeCategory.TRIGGER);
		new Scope("any_character",                  ScopeType.COUNTRY,  ScopeType.CHARACTER,    ScopeCategory.TRIGGER);
		new Scope("every_country",                  ScopeType.ANY,      ScopeType.COUNTRY,      ScopeCategory.EFFECT);
		new Scope("every_country_with_original_tag", ScopeType.ANY,     ScopeType.COUNTRY,      ScopeCategory.EFFECT);
		new Scope("every_other_country",            ScopeType.COUNTRY,  ScopeType.COUNTRY,      ScopeCategory.EFFECT);
		new Scope("every_neighbor_country",         ScopeType.COUNTRY,  ScopeType.COUNTRY,      ScopeCategory.EFFECT);
		new Scope("every_enemy_country",            ScopeType.COUNTRY,  ScopeType.COUNTRY,      ScopeCategory.EFFECT);
		new Scope("every_occupied_country",         ScopeType.COUNTRY,  ScopeType.COUNTRY,      ScopeCategory.EFFECT);
		new Scope("random_country",                 ScopeType.ANY,      ScopeType.COUNTRY,      ScopeCategory.EFFECT);
		new Scope("random_country_with_original_tag", ScopeType.ANY,    ScopeType.COUNTRY,      ScopeCategory.EFFECT);
		new Scope("random_neighbor_country",        ScopeType.COUNTRY,  ScopeType.COUNTRY,      ScopeCategory.EFFECT);
		new Scope("random_enemy_country",           ScopeType.COUNTRY,  ScopeType.COUNTRY,      ScopeCategory.EFFECT);
		new Scope("random_occupied_country",        ScopeType.COUNTRY,  ScopeType.COUNTRY,      ScopeCategory.EFFECT);
		new Scope("random_state",                   ScopeType.ANY,      ScopeType.STATE,        ScopeCategory.EFFECT);
		new Scope("random_owned_state",             ScopeType.COUNTRY,  ScopeType.STATE,        ScopeCategory.EFFECT);
		new Scope("random_controlled_state",        ScopeType.COUNTRY,  ScopeType.STATE,        ScopeCategory.EFFECT);
		new Scope("random_owned_controlled_state",  ScopeType.COUNTRY,  ScopeType.STATE,        ScopeCategory.EFFECT);
		new Scope("random_neighbor_state",          ScopeType.STATE,    ScopeType.STATE,        ScopeCategory.EFFECT);
		new Scope("every_state",                    ScopeType.ANY,      ScopeType.STATE,        ScopeCategory.EFFECT);
		new Scope("every_controlled_state",         ScopeType.COUNTRY,  ScopeType.STATE,        ScopeCategory.EFFECT);
		new Scope("every_owned_state",              ScopeType.COUNTRY,  ScopeType.STATE,        ScopeCategory.EFFECT);
		new Scope("every_neighbor_state",           ScopeType.STATE,    ScopeType.STATE,        ScopeCategory.EFFECT);
		new Scope("capital_scope",                  ScopeType.COUNTRY,  ScopeType.STATE,        ScopeCategory.EFFECT);
		new Scope("owner",                          ScopeType.STATE,    ScopeType.COUNTRY,      ScopeCategory.EFFECT);
		new Scope("controller",                     ScopeType.STATE,    ScopeType.COUNTRY,      ScopeCategory.EFFECT);
		new Scope("all_operative_leader",           ScopeType.COUNTRY,  ScopeType.OPERATIVE,    ScopeCategory.TRIGGER);
		new Scope("any_operative_leader",           ScopeType.COUNTRY,  ScopeType.OPERATIVE,    ScopeCategory.TRIGGER);
		new Scope("every_operative",                ScopeType.COUNTRY,  ScopeType.OPERATIVE,    ScopeCategory.EFFECT);
		new Scope("random_operative",               ScopeType.COUNTRY,  ScopeType.OPERATIVE,    ScopeCategory.EFFECT);
		new Scope("every_country_division",         ScopeType.COUNTRY,  ScopeType.DIVISION,     ScopeCategory.EFFECT);
		new Scope("random_country_division",        ScopeType.COUNTRY,  ScopeType.DIVISION,     ScopeCategory.EFFECT);
		new Scope("every_state_division",           ScopeType.STATE,    ScopeType.DIVISION,     ScopeCategory.EFFECT);
		new Scope("random_state_division",          ScopeType.STATE,    ScopeType.DIVISION,     ScopeCategory.EFFECT);
		new Scope("every_possible_country",         ScopeType.ANY,      ScopeType.COUNTRY,      ScopeCategory.EFFECT);
		new Scope("every_subject_country",          ScopeType.COUNTRY,  ScopeType.COUNTRY,      ScopeCategory.EFFECT);
		new Scope("random_subject_country",         ScopeType.COUNTRY,  ScopeType.COUNTRY,      ScopeCategory.EFFECT);
		new Scope("every_core_state",               ScopeType.COUNTRY,  ScopeType.STATE,        ScopeCategory.EFFECT);
		new Scope("random_core_state",              ScopeType.COUNTRY,  ScopeType.STATE,        ScopeCategory.EFFECT);
		new Scope("every_character",                ScopeType.COUNTRY,  ScopeType.CHARACTER,    ScopeCategory.EFFECT);
		new Scope("random_character",               ScopeType.COUNTRY,  ScopeType.CHARACTER,    ScopeCategory.EFFECT);
	}
}
