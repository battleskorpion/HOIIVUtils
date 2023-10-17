package hoi4utils.clausewitz_code.modifier;

//import java.sql.Array;
//import java.util.ArrayList;
import java.util.List;

public interface Modifier {
	enum Scope {
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
	}

	// private String modifierID;
	List<Scope> scope = null; // can have multiple categories?

	// public Modifier(Scope scope) {
	// this.scope = new ArrayList<>();
	// this.scope.add(scope);
	// }

	// public abstract Modifier getModifier();

	/**
	 * Returns the main scope of the modifier
	 * 
	 * @return modifier's main scope
	 */
	Scope getScope();
}
