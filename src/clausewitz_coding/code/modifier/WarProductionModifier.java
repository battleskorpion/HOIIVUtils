package clausewitz_coding.code.modifier;

public class WarProductionModifier implements Modifier {
	public WarProductionModifier() {
//		super(Scope.war_production);
	}

	@Override
	public Scope getScope() {
		return Scope.war_production;
	}

	public enum modifiers {
		civilian_factory_use,
		conscription,
		conscription_factor,
		consumer_goods_factor,
		conversion_cost_civ_to_mil_factor,
		conversion_cost_mil_to_civ_factor,
		equipment_conversion_speed,
		exiled_government_weekly_manpower,
		faction_trade_opinion_factor,
		global_building_slots,
		global_building_slots_factor,
		industrial_capacity_dockyard,
		industrial_capacity_factory,
		industry_air_damage_factor,
		industry_free_repair_factor,
		industry_repair_factor,
		land_reinforce_rate,
		line_change_production_efficiency_factor,
		local_building_slots,
		local_building_slots_factor,
		local_factories,
		local_manpower,
		local_non_core_manpower,
		local_resources,
		local_resources_factor,
		local_supplies,
		local_supplies_for_controller,
		min_export,
		minimum_training_level,
		mobilization_speed,
		monthly_population,
		non_core_manpower,
		production_factory_efficiency_gain_factor,
		production_factory_max_efficiency_factor,
		production_factory_start_efficiency_factor,
		production_lack_of_resource_penalty_factor,
		production_oil_factor,
		production_speed_buildings_factor,
		special_forces_training_time_factor,
		state_production_speed_buildings_factor,
		state_resources_factor,
		tech_air_damage_factor,
		trade_opinion_factor,
		training_time_army,
		training_time_army_factor,
		weekly_manpower,
	}
}
