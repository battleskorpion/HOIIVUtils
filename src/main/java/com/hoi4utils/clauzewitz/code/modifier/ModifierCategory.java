package main.java.com.hoi4utils.clauzewitz.code.modifier;

/**
 * More specific scope for modifiers - helps define in what context each modifier can be used.
 * Modifier can belong to multiple categories.
 */
public enum ModifierCategory {
	all,        // hoi4 default
	aggressive,
	AI,
	air,
	army,
	autonomy,
	country,
	defensive,
	government_in_exile,
	intelligence_agency,
	military_advancements,
	military_equipment,
	naval,
	peace,
	politics,
	state,
	unit_leader,
	war_production,
	UNKNOWN
}
