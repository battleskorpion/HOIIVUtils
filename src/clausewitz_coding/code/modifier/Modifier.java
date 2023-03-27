package clausewitz_coding.code.modifier;

import java.sql.Array;
import java.util.ArrayList;
import java.util.List;

public abstract class Modifier {
    public enum Scope {
        aggressive,
        ai,
        air,
        army,
        autonomy,
        country,
        defensive,
        government_in_exile,
        intelligence_agency,
        military_advancements,
        naval,
        peace,
        politics,
        state,
        unit_leader,
        war_production,
    }

    private String modifierID;
    private List<Scope> scope;      // can have multiple categories?

    public Modifier(Scope scope) {
        this.scope = new ArrayList<>();
        this.scope.add(scope);
    }

    public abstract Modifier getModifier();
}
