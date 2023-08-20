package hoi4utils.clausewitz_coding.code.modifier;

public class IntelligenceAgencyModifier implements Modifier {
	public IntelligenceAgencyModifier() {
//		super(Scope.intelligence_agency);
	}

	@Override
	public Scope getScope() {
		return Scope.intelligence_agency;
	}

	public enum modifiers {
		airforce_intel_decryption_bonus,
		airforce_intel_factor,
		army_intel_decryption_bonus,
		army_intel_factor,
		boost_ideology_mission_factor,
		boost_resistance_factor,
		civilian_intel_decryption_bonus,
		civilian_intel_factor,
		commando_trait_chance_factor,
		control_trade_mission_factor,
		crypto_department_enabled,
		crypto_strength,
		decryption_power,
		decryption_power_factor,
		defense_impact_on_blueprint_stealing,
		diplomatic_pressure_mission_factor,
		enemy_operative_recruitment_chance,
		female_random_operative_chance,
		intel_from_operatives_factor,
		intel_network_gain,
		intel_network_gain_factor,
		intelligence_agency_defense,
		navy_intel_decryption_bonus,
		navy_intel_factor,
		new_operative_slot_bonus,
		occupied_operative_recruitment_chance,
		operative_death_on_capture_chance,
		operative_slot,
		propaganda_mission_factor,
		root_out_resistance_effectiveness_factor,
		target_sabotage_factor,
	}
}
