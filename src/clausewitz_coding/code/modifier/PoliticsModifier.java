package clausewitz_coding.code.modifier;

public class PoliticsModifier implements Modifier {
	public PoliticsModifier() {
//		super(Scope.politics);
	}

	@Override
	public Scope getScope() {
		return Scope.politics;
	}

	public enum modifiers {
		drift_defence_factor,
		guarantee_cost,
		master_ideology_drift,
	}
}
