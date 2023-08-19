package clausewitz_coding.code.modifier;

public class AIModifier implements Modifier {
	public AIModifier() {
//		super(Scope.ai);
	}

	@Override
	public Scope getScope() {
		return Scope.ai;
	}

	public enum modifiers {
		ai_badass_factor,
		ai_call_ally_desire_factor,
		ai_desired_divisions_factor,
		ai_focus_aggressive_factor ,
		ai_focus_aviation_factor,
		ai_focus_defense_factor ,
		ai_focus_military_advancements_factor,
		ai_focus_military_equipment_factor ,
		ai_focus_naval_air_factor,
		ai_focus_naval_factor ,
		ai_focus_peaceful_factor,
		ai_focus_war_production_factor ,
		ai_get_ally_desire_factor,
		ai_join_ally_desire_factor,
		ai_license_acceptance,
	}
}
