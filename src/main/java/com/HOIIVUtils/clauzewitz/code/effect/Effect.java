package com.HOIIVUtils.clauzewitz.code.effect;

import com.HOIIVUtils.clausewitz_parser.NodeValue;
import com.HOIIVUtils.clauzewitz.code.scope.NotPermittedInScopeException;
import com.HOIIVUtils.clauzewitz.code.scope.Scope;
import com.HOIIVUtils.clauzewitz.code.scope.ScopeType;
import com.HOIIVUtils.clauzewitz.script.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


import java.beans.Expression;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Supplier;

/**
 * For information: <a href="https://hoi4.paradoxwikis.com/Effect">Effects
 * Wiki</a>
 */
public class Effect<T> extends DynamicPDX<T, StructuredPDX> implements ScopedPDXScript {

//    protected static final SortedMap<String, Effect<?>> effects = new TreeMap<>();
//
    public String identifier;
    private Scope defintionScope = null;
    private Scope targetScope;
    @NotNull private EnumSet<ScopeType> supportedScopes;
    @Nullable private EnumSet<ScopeType> supportedTargets;

    public Effect(String identifier, Supplier<PDXScript<T>> TSupplier, StructuredPDX structuredBlock) {
        super(TSupplier, structuredBlock);
        this.identifier = identifier;
    }

    /**
     * For simple effects that have no block version.
     * @param TSupplier
     */
    public Effect(String identifier, Supplier<PDXScript<T>> TSupplier) {
        super(TSupplier);
        this.identifier = identifier;
    }

    /**
     * For effects that only have a block definition version.
     * @param structuredBlock
     */
    public Effect(String identifier, StructuredPDX structuredBlock) {
        super(structuredBlock);
        this.identifier = identifier;
    }

    public Effect(Expression exp) {
        usingIdentifier(exp);
    }

    public String identifier() {
        return identifier;
    }

    public EnumSet<ScopeType> supportedScopes() {
        return supportedScopes;
    }

    public EnumSet<ScopeType> supportedTargets() {
		return supportedTargets;
	}

    public boolean hasSupportedTargets() {
		return (supportedTargets != null && !supportedTargets.isEmpty());
	}

	public void setTarget(Scope target) {
		this.targetScope = target;
	}

	public void setTarget(String string, Scope within) throws Exception {
		setTarget(Scope.of(string, within));
	}

    public Scope defintionScope() {
		return defintionScope;
	}

    public Scope targetScope() {
        return targetScope;
    }

    public String target() {
        if (targetScope == null) {
            return "[null target]";
        }
        return targetScope.name;
    }

    public boolean hasTarget() {
        return targetScope != null;
    }

    @Override
    public Scope getDefinitionScope() {
        return this.defintionScope;
    }

    public EnumSet<ScopeType> getSupportedScopes() {
        // Assuming there's a way to determine supported scopes
        return this.supportedScopes;
    }
//
//    @Override protected Object clone() throws CloneNotSupportedException {
//        Effect<?> c = (Effect<?>) super.clone();
//        c.parameters = new ArrayList<>();
//        return c;
//    }
}