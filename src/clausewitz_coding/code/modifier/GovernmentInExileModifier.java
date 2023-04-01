package clausewitz_coding.code.modifier;

public class GovernmentInExileModifier implements Modifier {
    public GovernmentInExileModifier() {
//        super(Scope.government_in_exile);
    }

    @Override
    public Scope getScope() {
        return Scope.government_in_exile;
    }

    public enum modifiers {
        dockyard_donations,
        exile_manpower_factor,
        industrial_factory_donations,
        legitimacy_daily,
        legitimacy_gain_factor,
        military_factory_donations,
        targeted_legitimacy_daily,
    }
}