package main.java.com.hoi4utils.clauzewitz.script;

import main.java.com.hoi4utils.clauzewitz.code.scope.Scope;
import main.java.com.hoi4utils.clauzewitz.code.scope.ScopeType;

import java.util.Collection;

public interface ScopedPDXScript {
    Scope getDefinitionScope();

    default boolean isSupportedInScope(Scope scope) {
        if (supportedScopes().contains(ScopeType.any))
            return true;
        return supportedScopes().contains(scope.targetScopeType());
    }

    Collection<ScopeType> supportedScopes();
}
