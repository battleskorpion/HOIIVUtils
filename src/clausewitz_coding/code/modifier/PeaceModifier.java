package clausewitz_coding.code.modifier;

public class PeaceModifier implements Modifier {
    public PeaceModifier() {

//        super(Scope.peace);
    }

    @Override
    public Scope getScope() {
        return Scope.peace;
    }

    public enum modifiers {
        annex_cost_factor,
        puppet_cost_factor,
    }
}
