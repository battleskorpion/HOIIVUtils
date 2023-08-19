package clausewitz_coding.code.modifier;

public class StateModifier implements Modifier {
	public StateModifier() {
//		super(Scope.state);
	}

	@Override
	public Scope getScope() {
		return Scope.state;
	}

	public enum modifiers {
		army_speed_factor_for_controller,
		compliance_gain,
		compliance_growth,
		compliance_growth_on_our_occupied_states,
		disable_strategic_redeployment,
		disable_strategic_redeployment_for_controller,
		enemy_intel_network_gain_factor_over_occupied_tag,
		enemy_spy_negative_status_factor,
		intel_network_gain,
		intel_network_gain_factor,
		local_building_slots,
		local_building_slots_factor,
		local_factories,
		local_factory_sabotage,
		local_intel_to_enemies,
		local_manpower,
		local_non_core_manpower,
		local_resources,
		local_resources_factor,
		local_supplies,
		local_supplies_for_controller,
		mobilization_speed,
		no_compliance_gain,
		non_core_manpower,
		recruitable_population,
		recruitable_population_factor,
		required_garrison_factor,
		resistance_activity,
		resistance_damage_to_garrison,
		resistance_damage_to_garrison_on_our_occupied_states,
		resistance_decay,
		resistance_decay_on_our_occupied_states,
		resistance_garrison_penetration_chance,
		resistance_growth,
		resistance_growth_on_our_occupied_states,
		resistance_target,
		resistance_target_on_our_occupied_states,
		starting_compliance,
		state_production_speed_buildings_factor,
		state_resources_factor,
		supply_factor,
		truck_attrition,
		truck_attrition_factor,
	}
}
