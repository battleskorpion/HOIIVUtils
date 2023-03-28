package clausewitz_coding.code.modifier;

public class DefensiveModifier implements Modifier {
    public DefensiveModifier() {
//        super(Scope.defensive);
    }

    @Override
    public Scope getScope() {
        return Scope.defensive;
    }

    public enum modifiers {
        armor_factor,
        army_armor_defence_factor,
        army_artillery_defence_factor,
        army_breakthrough_against_major_factor,
        army_breakthrough_against_minor_factor,
        army_core_defence_factor,
        army_defence_against_major_factor,
        army_defence_against_minor_factor,
        army_defence_factor,
        army_infantry_defence_factor,
        army_morale,
        army_morale_factor,
        army_org,
        army_org_factor,
        attrition,
        attrition_for_controller,
        cavalry_defence_factor,
        convoy_escort_efficiency,
        convoy_retreat_speed,
        defence,
        dig_in_speed,
        dig_in_speed_factor,
        dont_lose_dig_in_on_attack,
        encryption,
        encryption_factor,
        guarantee_tension,
        local_intel_to_enemies,
        max_dig_in,
        max_dig_in_factor,
        mechanized_defence_factor,
        motorized_defence_factor,
        naval_retreat_chance,
        naval_retreat_chance_after_initial_combat,
        naval_retreat_speed,
        naval_retreat_speed_after_initial_combat,
        navy_capital_ship_defence_factor,
        navy_screen_defence_factor,
        navy_submarine_defence_factor,
        navy_submarine_detection_factor,
        puppet_cost_factor,
        recon_factor,
        recon_factor_while_entrenched,
        send_volunteers_tension,
        special_forces_defence_factor,
        static_anti_air_damage_factor,
        static_anti_air_hit_chance_factor,
        sub_retreat_speed,
        supply_consumption_factor,
        terrain_penalty_reduction,
    }
}
