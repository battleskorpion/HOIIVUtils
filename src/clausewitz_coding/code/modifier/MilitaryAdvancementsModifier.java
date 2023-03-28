package clausewitz_coding.code.modifier;

public class MilitaryAdvancementsModifier implements Modifier {
    public MilitaryAdvancementsModifier() {
//        super(Scope.military_advancements);
    }

    @Override
    public Scope getScope() {
        return Scope.military_advancements;
    }

    public enum modifiers {
        experience_gain_army,
        experience_gain_army_factor,
        experience_gain_army_unit,
        experience_gain_army_unit_factor,
        experience_gain_factor,
        experience_gain_navy,
        experience_gain_navy_factor,
        experience_gain_navy_unit,
        experience_gain_navy_unit_factor,
        research_speed_factor,
    }
}
