package hoi4utils.clausewitz_code.modifier;

public class GovernmentInExileModifier implements Modifier {
	public GovernmentInExileModifier() {
//		super(Scope.government_in_exile);
	}

	@Override
	public ModifierCategory getCategory() {
		return ModifierCategory.government_in_exile;
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
