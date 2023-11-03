package hoi4utils.clausewitz_code.modifier;

//import java.sql.Array;
//import java.util.ArrayList;

import java.util.List;

/**
 * For information: <a href="https://hoi4.paradoxwikis.com/Modifiers">Modifiers Wiki</a>
 * Notes:
 *  - A modifier with the value of 0 "will always do nothing."
 *      - Negative modifiers will always work and have the opposite effect
 *  - Opinion modifiers are not regular modifiers, and should therefore be
 *    implemented separately.
 *  - modifiers do not support if statements
 */
public interface Modifier {
	/* more specific scope for modifiers
	(helps define in what context each modifier can be used)
	 */
	enum ModifierCategory {
		aggressive,
		ai,
		air,
		army,
		autonomy,
		country,
		defensive,
		government_in_exile,
		intelligence_agency,
		military_advancements,
		naval,
		peace,
		politics,
		state,
		unit_leader,
		war_production,
		UNKNOWN
	}

	// private String modifierID;
	////List<ModifierCategory> scope = null; // can have multiple categories?   // todo this needed in some way? 

	// public Modifier(Scope scope) {
	// this.scope = new ArrayList<>();
	// this.scope.add(scope);
	// }

	// public abstract Modifier getModifier();

	/**
	 * Returns the main category of the modifier
	 * 
	 * @return modifier's category
	 */
	ModifierCategory getCategory();
}
