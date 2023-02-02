package hoi4_localization.code.trigger;

import hoi4_localization.code.ScopeTargetType;

import java.util.EnumSet;

/**
 *
 */
public enum CountryTrigger implements Trigger {
    // _criteria means is to specify the version of a trigger with multiple parameters

    // general
    exists {
        @Override
        public EnumSet<TriggerParameter> parameters() {
            return EnumSet.of(TriggerParameter.BOOL);
        }
    },
    focus_progress {
        @Override
        public EnumSet<TriggerParameter> parameters() {
            return EnumSet.of(TriggerParameter.FOCUS, TriggerParameter.FLOAT);
        }
    },
    has_active_mission {
        @Override
        public EnumSet<TriggerParameter> parameters() {
            return EnumSet.of(TriggerParameter.MISSION);
        }
    },
    has_collaboration {
        @Override
        public EnumSet<TriggerParameter> parameters() {
            return EnumSet.of(TriggerParameter.COUNTRY, TriggerParameter.FLOAT);
        }
    },
    has_completed_custom_achievement {
        @Override
        public EnumSet<TriggerParameter> parameters() {
            return EnumSet.of(TriggerParameter.ACHIEVEMENT);
        }
    },
    has_completed_focus {
        @Override
        public EnumSet<TriggerParameter> parameters() {
            return EnumSet.of(TriggerParameter.FOCUS);
        }
    },
    has_cosmetic_tag {
        @Override
        public EnumSet<TriggerParameter> parameters() {
            return EnumSet.of(TriggerParameter.COSMETIC_TAG);
        }
    },
    has_country_custom_difficulty_setting {
        @Override
        public EnumSet<TriggerParameter> parameters() {
            return EnumSet.of(TriggerParameter.BOOL);
        }
    },
    has_country_flag {
        @Override
        public EnumSet<TriggerParameter> parameters() {
            return EnumSet.of(TriggerParameter.COUNTRY_FLAG);
        }
    },
    has_country_flag_criteria {
        @Override
        public EnumSet<TriggerParameter> parameters() {
            return EnumSet.of(TriggerParameter.COUNTRY_FLAG, TriggerParameter.INT, TriggerParameter.DAYS,
                    TriggerParameter.DATE);
        }
    },
    has_decision {
        @Override
        public EnumSet<TriggerParameter> parameters() {
            return EnumSet.of(TriggerParameter.DECISION);
        }
    },
    has_dynamic_modifier {
        @Override
        public EnumSet<TriggerParameter> parameters() {
            return EnumSet.of(TriggerParameter.MODIFIER, TriggerParameter.COUNTRY);
        }
    },
    has_event_target {
        @Override
        public EnumSet<TriggerParameter> parameters() {
            return EnumSet.of(TriggerParameter.EVENT_TARGET);
        }
    },
    has_focus_tree {
        @Override
        public EnumSet<TriggerParameter> parameters() {
            return EnumSet.of(TriggerParameter.FOCUS_TREE);
        }
    },
    has_terrain {
        @Override
        public EnumSet<TriggerParameter> parameters() {
            return EnumSet.of(TriggerParameter.TERRAIN);
        }
    },
    is_ai {
        @Override
        public EnumSet<TriggerParameter> parameters() {
            return EnumSet.of(TriggerParameter.BOOL);
        }
    },
    is_dynamic_country {
        @Override
        public EnumSet<TriggerParameter> parameters() {
            return EnumSet.of(TriggerParameter.BOOL);
        }
    },
    num_of_supply_nodes {
        @Override
        public EnumSet<TriggerParameter> parameters() {
            return EnumSet.of(TriggerParameter.INT);
        }
    },
    original_tag {
        @Override
        public EnumSet<TriggerParameter> parameters() {
            return EnumSet.of(TriggerParameter.SCOPE_OR_VARIABLE);
        }
    },
    tag {
        @Override
        public EnumSet<TriggerParameter> parameters() {
            return EnumSet.of(TriggerParameter.SCOPE_OR_VARIABLE);
        }
    },

    // politics
    has_political_power {
        @Override
        public EnumSet<TriggerParameter> parameters() {
            return EnumSet.of(TriggerParameter.FLOAT);
        }
    },
    political_power_daily {
        @Override
        public EnumSet<TriggerParameter> parameters() {
            return EnumSet.of(TriggerParameter.FLOAT);
        }
    },
    political_power_growth {
        @Override
        public EnumSet<TriggerParameter> parameters() {
            return EnumSet.of(TriggerParameter.FLOAT);
        }
    },
    command_power {
        @Override
        public EnumSet<TriggerParameter> parameters() {
            return EnumSet.of(TriggerParameter.FLOAT);
        }
    },
    command_power_daily {
        @Override
        public EnumSet<TriggerParameter> parameters() {
            return EnumSet.of(TriggerParameter.FLOAT);
        }
    },
    has_war_support {
        @Override
        public EnumSet<TriggerParameter> parameters() {
            return EnumSet.of(TriggerParameter.FLOAT);
        }
    },
    has_stability {
        @Override
        public EnumSet<TriggerParameter> parameters() {
            return EnumSet.of(TriggerParameter.FLOAT);
        }
    },
    ideology_generic {
        @Override
        public EnumSet<TriggerParameter> parameters() {
            return EnumSet.of(TriggerParameter.FLOAT);
        }
    },
    has_government_ideology {
        // identical code name to has_government_country but accepts different parameters
        @Override
        public EnumSet<TriggerParameter> parameters() {
            return EnumSet.of(TriggerParameter.IDEOLOGY);
        }
    },
    has_government_country {
        @Override
        public EnumSet<TriggerParameter> parameters() {
            return EnumSet.of(TriggerParameter.COUNTRY);
        }
    },
    has_elections {
        @Override
        public EnumSet<TriggerParameter> parameters() {
            return EnumSet.of(TriggerParameter.BOOL);
        }
    },
    is_staging_coup {
        @Override
        public EnumSet<TriggerParameter> parameters() {
            return EnumSet.of(TriggerParameter.BOOL);
        }
    },
    is_target_of_coup {
        @Override
        public EnumSet<TriggerParameter> parameters() {
            return EnumSet.of(TriggerParameter.BOOL);
        }
    },
    has_civil_war {
        @Override
        public EnumSet<TriggerParameter> parameters() {
            return EnumSet.of(TriggerParameter.BOOL);
        }
    },
    civilwar_target {
        @Override
        public EnumSet<TriggerParameter> parameters() {
            return EnumSet.of(TriggerParameter.COUNTRY);
        }
    },
    has_manpower_for_recruit_change_to {
        @Override
        public EnumSet<TriggerParameter> parameters() {
            return EnumSet.of(TriggerParameter.FLOAT, TriggerParameter.GROUP);
        }
    },
    has_rule {
        @Override
        public EnumSet<TriggerParameter> parameters() {
            return EnumSet.of(TriggerParameter.COUNTRY_RULE);
        }
    },
    has_casualties_war_support {
        @Override
        public EnumSet<TriggerParameter> parameters() {
            return EnumSet.of(TriggerParameter.FLOAT);
        }
    },
    has_convoys_war_support {
        @Override
        public EnumSet<TriggerParameter> parameters() {
            return EnumSet.of(TriggerParameter.FLOAT);
        }
    },
    has_bombing_war_support {
        @Override
        public EnumSet<TriggerParameter> parameters() {
            return EnumSet.of(TriggerParameter.FLOAT);
        }
    },

    // Balance of power
    // TODO
    has_any_power_balance,
    has_power_balance,
    has_power_balance_modifier,
    is_power_balance_in_range,
    is_power_balance_side_active,
    power_balance_value,
    power_balance_daily_change,
    power_balance_weekly_change,

    // Buildings
    building_generic {
        @Override
        public EnumSet<TriggerParameter> parameters() {
            return EnumSet.of(TriggerParameter.INT);
        }
    },
    num_of_military_factories {
        @Override
        public EnumSet<TriggerParameter> parameters() {
            return EnumSet.of(TriggerParameter.INT);
        }
    },
    num_of_civilian_factories {
        @Override
        public EnumSet<TriggerParameter> parameters() {
            return EnumSet.of(TriggerParameter.INT);
        }
    },
    num_of_naval_factories {
        @Override
        public EnumSet<TriggerParameter> parameters() {
            return EnumSet.of(TriggerParameter.INT);
        }
    },
    num_of_available_military_factories {
        @Override
        public EnumSet<TriggerParameter> parameters() {
            return EnumSet.of(TriggerParameter.INT);
        }
    },
    num_of_available_civilian_factories {
        @Override
        public EnumSet<TriggerParameter> parameters() {
            return EnumSet.of(TriggerParameter.INT);
        }
    },
    num_of_available_naval_factories {
        @Override
        public EnumSet<TriggerParameter> parameters() {
            return EnumSet.of(TriggerParameter.INT);
        }
    },
    num_of_factories {
        @Override
        public EnumSet<TriggerParameter> parameters() {
            return EnumSet.of(TriggerParameter.INT);
        }
    },
    num_of_controlled_factories {
        @Override
        public EnumSet<TriggerParameter> parameters() {
            return EnumSet.of(TriggerParameter.INT);
        }
    },
    num_of_owned_factories {
        @Override
        public EnumSet<TriggerParameter> parameters() {
            return EnumSet.of(TriggerParameter.INT);
        }
    },
    num_of_civilian_factories_available_for_projects {
        @Override
        public EnumSet<TriggerParameter> parameters() {
            return EnumSet.of(TriggerParameter.INT);
        }
    },
    ic_ratio {
        @Override
        public EnumSet<TriggerParameter> parameters() {
            return EnumSet.of(TriggerParameter.COUNTRY, TriggerParameter.RATIO);
        }
    },
    has_damaged_buildings {
        @Override
        public EnumSet<TriggerParameter> parameters() {
            return EnumSet.of(TriggerParameter.BOOL);
        }
    },
    has_built {
        @Override
        public EnumSet<TriggerParameter> parameters() {
            return EnumSet.of(TriggerParameter.BUILDING_TYPE, TriggerParameter.INT);
        }
    },

    // Technology
    has_tech,
    is_researching_technology,
    can_research,
    original_research_slots,
    amount_research_slots,
    is_in_tech_sharing_group,
    num_tech_sharing_groups,
    has_tech_bonus,
    land_doctrine_level,
    num_researched_technologies,

    // Ideas
    has_idea,
    has_idea_with_trait,
    has_allowed_idea_with_traits,
    has_available_idea_with_traits,
    amount_taken_ideas,

    // Diplomacy
    is_major,
    is_in_faction,
    is_faction_leader,
    is_spymaster,
    is_puppet,
    is_subject,
    has_subject,
    is_exile_host,
    is_government_in_exile,
    compare_autonomy_progress_ratio,
    has_legitimacy,
    foreign_manpower,
    num_faction_members,
    num_subjects,
    is_ally_with,
    is_in_faction_with,
    has_non_aggression_pact_with,
    is_guaranteed_by,
    has_guaranteed,
    has_military_access_to,
    gives_military_access_to,
    is_neighbor_of,
    is_owner_neighbor_of,
    is_puppet_of,
    is_subject_of,
    is_embargoed_by,
    is_embargoing,
    has_autonomy_state,
    compare_autonomy_state,
    has_opinion_modifier,
    is_exiled_in,
    can_declare_war_on,
    is_hosting_exile,
    received_expeditionary_forces,
    has_relation_modifier,
    has_opinion,

    // War
    has_war,
    has_offensive_war,
    has_defensive_war,
    has_war_with_major,
    has_capitulated,
    has_border_war,
    any_claim,
    is_in_peace_conference,
    surrender_progress,
    has_added_tension_amount,
    any_war_score,
    controls_province,
    days_since_capitulated,
    has_war_with,
    has_offensive_war_with,
    has_defensive_war_with,
    has_war_together_with,
    has_border_war_with,
    has_wargoal_against,
    is_justifying_wargoal_against,
    has_annex_war_goal,
    has_border_war_between,
    has_wargoal_against_criteria,
    has_war_with_wargoal_against,

    // State-related triggers in the country scope
    controls_state,
    owns_state,
    num_of_controlled_states,
    num_occupied_states,
    has_full_control_of_state,
    core_compliance,
    garrison_manpower_need,
    has_core_occupation_modifier,
    occupation_law,

    // Military
    has_army_experience,
    has_air_experience,
    has_navy_experience,
    has_manpower,
    has_army_manpower,
    manpower_per_military_factory,
    conscription_ratio,
    current_conscription_amount,
    target_conscription_amount,
    num_divisions,
    num_of_nukes,
    casualties,
    casualties_k,       // (in thousands)
    casualties_inflicted_by,
    amount_manpower_in_deployment_queue,
    has_attache_from,
    has_attache,
    is_lend_leasing,
    has_template,
    has_template_majority_unit,
    has_template_containing_unit,
    strength_ratio,
    naval_strength_ratio,
    naval_strength_comparison,
    alliance_strength_ratio,
    alliance_naval_strength_ratio,
    enemies_strength_ratio,
    enemies_naval_strength_ratio,
    has_army_size,
    has_navy_size,
    has_deployed_air_force_size,
    divisions_in_state,
    army_manpower_in_state,
    divisions_in_border_state,
    num_divisions_in_states,
    num_battalions_in_states,
    ships_in_state_ports,
    num_planes_stationed_in_regions,
    has_volunteers_amount_from,
    convoy_threat,
    has_mined,
    has_mines,
    mine_threat,

    // Equipment
    stockpile_ratio,
    has_equipment,
    has_any_license,
    is_licensing_any_to,
    is_licensing_to,
    has_license,
    fuel_ratio,
    has_fuel,
    has_design_based_on,

    // Intelligence
    // todo

    // AI
    ai_irrationality,
    ai_liberate_desire,
    ai_has_role_division,
    ai_has_role_template,
    ai_wants_divisions,
    has_template_ai_majority_unit,

    // Character related triggers in the country scope
    can_be_country_leader,
    has_character,
    has_country_leader,
    has_country_leader_ideology,
    has_country_leader_with_trait,
    is_female,
    has_unit_leader,

    // Peace conferences
    // (triggers that can only be used within peace conferences).
    pc_is_winner,
    pc_is_on_winning_side,
    pc_is_loser,
    pc_is_untouched_loser,
    pc_is_on_same_side_as,
    pc_is_liberated,
    pc_is_liberated_by,
    pc_is_puppeted,
    pc_is_puppeted_by,
    pc_is_forced_government,
    pc_is_forced_government_by,
    pc_is_forced_government_to,
    pc_total_score,
    pc_current_score,
    ;

    Trigger trigger;

    CountryTrigger() {
//        trigger = new Trigger()
    }

    @Override
    public ScopeTargetType scopeTargetType() {
        return ScopeTargetType.COUNTRY;
    }

    @Override
    public EnumSet<TriggerParameter> parameters() {
        return null;    // no parameters by default?
    }
}
