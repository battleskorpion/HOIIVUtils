package hoi4_coding.code.trigger;

public enum TriggerParameter {
    BOOL,
    EVENT_TARGET,
    INT,                // in thousands?
    SCOPE_OR_VARIABLE,
    COSMETIC_TAG,
    DECISION,
    COUNTRY_FLAG,
    FLAG_VALUE,
    FOCUS,
    FOCUS_TREE,
    MISSION,
    TERRAIN,
    DATE,
    DAYS,
    PROGRESS,
    MOD,
    ACHIEVEMENT,
    MODIFIER,
    COUNTRY,        // 	occupied_country_tag?
    FLOAT,              // variable accepted?
    IDEOLOGY,           // (ideology group)
    SUB_IDEOLOGY,
    GROUP,
    COUNTRY_RULE,
    ID,
    RATIO,
    BUILDING_TYPE,
    TECHNOLOGY,
    TECH_SHARING_GROUP,
    CATEGORY,
    IDEA,
    IDEA_TRAIT,
    //ignore = { <ideas> } // todo
    SLOTS,              // Slots types are found in /Hearts of Iron IV/common/idea_tags/*.txt
    CHARACTERS,
    AUTONOMY_STATE,
    OPINION_MODIFIER,
    WARGOAL_TYPE,
    LAW_ID,
    TOOLTIP,
    EQUIPMENT_ARCHETYPE,
    EQUIPMENT,
    EQUIPMENT_VARIANT_ID,
    AGENCY_UPGRADE,
    OPERATION,


}

