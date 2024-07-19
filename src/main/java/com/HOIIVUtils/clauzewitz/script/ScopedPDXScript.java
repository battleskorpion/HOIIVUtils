package com.HOIIVUtils.clauzewitz.script;

import com.HOIIVUtils.clausewitz_parser.Node;
import com.HOIIVUtils.clausewitz_parser.NodeStreamable;
import com.HOIIVUtils.clauzewitz.code.scope.Scope;
import com.HOIIVUtils.clauzewitz.code.scope.ScopeType;

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
