package com.HOIIVUtils.clauzewitz.script;

import com.HOIIVUtils.clausewitz_parser.Node;
import org.apache.poi.ss.formula.functions.T;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;

public abstract class DynamicPDX<V, U extends StructuredPDX> implements PDXScript<V> {
    protected AbstractPDX<V> simplePDX;
    protected Supplier<AbstractPDX<V>> simplePDXSupplier;
    @NotNull protected final U structuredBlock;
    /**
     * may be null. the structured block does not always have a single property
     * that is equivalent to the simple value. Or, the structured block has a
     * property that is equivalent to a value and the property is null.
     */
    private List<String> structuredPDXValueIdentifiers;

    public DynamicPDX(Supplier<AbstractPDX<V>> simplePDXSupplier, @NotNull U structuredBlock) {
        this.simplePDXSupplier = simplePDXSupplier;
        this.structuredBlock = structuredBlock;
        this.structuredPDXValueIdentifiers = null;
    }

    public DynamicPDX(Supplier<AbstractPDX<V>> simplePDXSupplier, @NotNull U structuredBlock, String structuredPDXValueIdentifier) {
        this.simplePDXSupplier = simplePDXSupplier;
        this.structuredBlock = structuredBlock;
        this.structuredPDXValueIdentifiers = List.of(structuredPDXValueIdentifier);
    }

    public DynamicPDX(Supplier<AbstractPDX<V>> simplePDXSupplier, @NotNull U structuredBlock, List<String> structuredPDXValueIdentifier) {
        this.simplePDXSupplier = simplePDXSupplier;
        this.structuredBlock = structuredBlock;
        this.structuredPDXValueIdentifiers = structuredPDXValueIdentifier;
    }

    public void setSimplePDX(AbstractPDX<V> simplePDX) {
        this.simplePDX = simplePDX;
        this.structuredBlock.setNull();
    }

    public void setSimplePDX(V obj) {
        if (obj == null) {
            simplePDX = null;
            return;
        }

        supplySimplePDX();
        simplePDX.set(obj);
    }

    public boolean isBlock() {
        return simplePDX == null;
    }

    private boolean isBlock(Node expression) {
        return expression.value().isList();
    }

    @Override
    public void set(V obj) {
        if (!isBlock()) {
            setSimplePDX(obj);
        } else {
            return;
        }
    }

    @Override
    public void set(Node expression) throws UnexpectedIdentifierException, NodeValueTypeException {
        if (!isBlock(expression)) {
            supplySimplePDX();
            simplePDX.set(expression);
        } else {
            structuredBlock.set(expression);
        }
    }

    @Override
    public @Nullable V get() {
        if (!isBlock()) {
            return simplePDX.get();
        } else if (isBlock()) {
            var valueProperty = getStructuredValueProperty();
            return valueProperty == null ? null : valueProperty.get();
        } else {
            return null;
        }
    }

    @Override
    public void loadPDX(Node expression) throws UnexpectedIdentifierException {
        if (!isBlock(expression)) {
            supplySimplePDX();
            simplePDX.loadPDX(expression);
        } else {
            structuredBlock.loadPDX(expression);
        }
    }

    private void supplySimplePDX() {
        if (simplePDX == null) {
            simplePDX = simplePDXSupplier.get();
        }
    }

    @Override
    public void loadPDX(List<Node> expressions) {
        structuredBlock.loadPDX(expressions);
        simplePDX.setNull();
    }

    @Override
    public boolean isValidIdentifier(Node node) {
        // todo i hope this works as intended
        if (!isBlock()) {
            return simplePDX.isValidIdentifier(node);
        } else {
            return structuredBlock.isValidIdentifier(node);
        }
    }

    @Override
    public void setNull() {
        simplePDX = null;
        structuredBlock.setNull();
    }

    @Override
    public void loadOrElse(Node exp, V value) {
        if (!isBlock()) {
            simplePDX.loadOrElse(exp, value);
        } else {
            structuredBlock.loadOrElse(exp, null);
//            setStructuredBlockValue(value);
        }
    }

    @Override
    public String toScript() {
        if (!isBlock()) {
            return simplePDX.toScript();
        } else {
            return structuredBlock.toScript();
        }
    }

    @Override
    public boolean objEquals(PDXScript<?> other) {
        if (!isBlock()) {
            return simplePDX.objEquals(other);
        } else {
            return structuredBlock.objEquals(other);
        }
    }

    @Override
    public V getOrElse(V elseValue) {
        V get = get();
        return get == null ? elseValue : get;
    }

    @Override
    public boolean isUndefined() {
        return get() == null;
    }

    @Override
    public String toString() {
        return "DynamicPDX{" +
                "simpleValue=" + simplePDX +
                ", structuredValue=" + structuredBlock +
                '}';
    }


    /**
     * may be null. the structured block does not always have a single property
     * that is equivalent to the simple value. Or, the structured block has a
     * property that is equivalent to a value and the property is null.
     */
    private @Nullable AbstractPDX<V> getStructuredValueProperty() {
        if (isBlock() && structuredPDXValueIdentifiers != null) {
            return structuredBlock.<V>getPDXPropertyOfType(structuredPDXValueIdentifiers);
        } else {
            return null;
        }
    }
}
