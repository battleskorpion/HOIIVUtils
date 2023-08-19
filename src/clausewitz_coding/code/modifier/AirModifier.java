package clausewitz_coding.code.modifier;

public class AirModifier implements Modifier {
	public AirModifier() {
//		super(Scope.air);
	}

	@Override
	public Scope getScope() {
		return Scope.air;
	}

	public enum modifiers {
		air_accidents,
		air_accidents_factor,
		air_ace_bonuses_factor,
		air_ace_generation_chance_factor,
		air_advisor_cost_factor,
		air_agility_factor,
		air_air_superiority_agility_factor,
		air_air_superiority_attack_factor,
		air_air_superiority_defence_factor,
		air_attack_factor,
		air_bombing_targetting,
		air_carrier_night_penalty_reduction_factor,
		air_cas_efficiency,
		air_cas_present_factor,
		air_close_air_support_agility_factor,
		air_close_air_support_attack_factor,
		air_close_air_support_defence_factor,
		air_close_air_support_org_damage_factor,
		air_defence_factor,
		air_detection,
		air_escort_efficiency,
		air_fuel_consumption_factor,
		air_home_defence_factor,
		air_intercept_efficiency,
		air_interception_agility_factor,
		air_interception_attack_factor,
		air_interception_defence_factor,
		air_interception_detect_factor,
		air_manpower_requirement_factor,
		air_maximum_speed_factor,
		air_mission_efficiency,
		air_mission_xp_gain_factor,
		air_nav_efficiency,
		air_night_penalty,
		air_paradrop_agility_factor,
		air_paradrop_attack_factor,
		air_paradrop_defence_factor,
		air_power_projection_factor,
		air_range_factor,
		air_strategic_bomber_agility_factor,
		air_strategic_bomber_attack_factor,
		air_strategic_bomber_bombing_factor,
		air_strategic_bomber_defence_factor,
		air_strategic_bomber_night_penalty,
		air_superiority_detect_factor,
		air_superiority_efficiency,
		air_training_xp_gain_factor,
		air_untrained_pilots_penalty_factor,
		air_weather_penalty,
		air_wing_xp_loss_when_killed_factor,
		army_bonus_air_superiority_factor,
		carrier_night_traffic,
		enemy_army_bonus_air_superiority_factor,
		experience_gain_air,
		experience_gain_air_factor,
		ground_attack,
		ground_attack_factor,
		mines_planting_by_air_factor,
		mines_sweeping_by_air_factor,
		modifier_enemy_port_superiority_limit,
		naval_strike_agility_factor,
		naval_strike_attack_factor,
		naval_strike_targetting_factor,
		navy_weather_penalty,
		strategic_bomb_visibility
	}
}
