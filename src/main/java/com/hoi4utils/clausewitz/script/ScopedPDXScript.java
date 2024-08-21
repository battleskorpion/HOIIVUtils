//package com.hoi4utils.clausewitz.script;
//
//import com.hoi4utils.clausewitz.code.scope.Scope;
//import com.hoi4utils.clausewitz.code.scope.ScopeType;
//
//import java.util.Collection;
//
//public interface ScopedPDXScript {
//    Scope getDefinitionScope();
//
//    default boolean isSupportedInScope(Scope scope) {
//        if (supportedScopes().contains(ScopeType.any))
//            return true;
//        return supportedScopes().contains(scope.targetScopeType());
//    }
//
//    Collection<ScopeType> supportedScopes();
//}
