package hoi4_localization.code.trigger;

import com.sun.source.tree.Scope;
import hoi4_localization.code.ScopeTargetType;

import java.util.EnumSet;

public interface Trigger {
    /**
     * Return scope target type, different triggers can only be used within certain scopes.
     */
    ScopeTargetType scopeTargetType();

    EnumSet<TriggerParameter> parameters();


}
