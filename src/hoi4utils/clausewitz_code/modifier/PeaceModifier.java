package hoi4utils.clausewitz_code.modifier;

public class PeaceModifier implements Modifier {
	public PeaceModifier() {

//		super(Scope.peace);
	}

	@Override
	public ModifierCategory getCategory() {
		return ModifierCategory.peace;
	}

	public enum modifiers {
		annex_cost_factor,
		puppet_cost_factor,
	}
}
