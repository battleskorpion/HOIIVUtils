package hoi4utils.clausewitz_code.trigger;

public enum BooleanTrigger {
	// Country scope
	exists,
	is_ai,
	has_country_custom_difficulty_setting,
	is_dynamic_country;

	boolean value;
}