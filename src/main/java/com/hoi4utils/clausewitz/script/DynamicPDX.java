package com.hoi4utils.clausewitz.script;

import com.hoi4utils.clausewitz_parser.Node;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;

public abstract class DynamicPDX<V, U extends StructuredPDX> implements PDXScript<V> {
    protected PDXScript<V> simplePDX;
    protected Supplier<PDXScript<V>> simplePDXSupplier;
    @Nullable protected final U structuredBlock;
    /**
     * may be null. the structured block does not always have a single property
     * that is equivalent to the simple value. Or, the structured block has a
     * property that is equivalent to a value and the property is null.
     */
    private List<String> structuredPDXValueIdentifiers;

    public DynamicPDX(@NotNull Supplier<PDXScript<V>> simplePDXSupplier, @NotNull U structuredBlock) {
        this.simplePDXSupplier = simplePDXSupplier;
        this.structuredBlock = structuredBlock;
        this.structuredPDXValueIdentifiers = null;
    }

    public DynamicPDX(@NotNull Supplier<PDXScript<V>> simplePDXSupplier, @NotNull U structuredBlock, String structuredPDXValueIdentifier) {
        this.simplePDXSupplier = simplePDXSupplier;
        this.structuredBlock = structuredBlock;
        this.structuredPDXValueIdentifiers = List.of(structuredPDXValueIdentifier);
    }

    public DynamicPDX(@NotNull Supplier<PDXScript<V>> simplePDXSupplier, @NotNull U structuredBlock, List<String> structuredPDXValueIdentifier) {
        this.simplePDXSupplier = simplePDXSupplier;
        this.structuredBlock = structuredBlock;
        this.structuredPDXValueIdentifiers = structuredPDXValueIdentifier;
    }

    public DynamicPDX(@NotNull U structuredBlock) {
        this.simplePDXSupplier = null;
        this.structuredBlock = structuredBlock;
        this.structuredPDXValueIdentifiers = null;
    }

    public DynamicPDX(@NotNull U structuredBlock, String structuredPDXValueIdentifier) {
        this.simplePDXSupplier = null;
        this.structuredBlock = structuredBlock;
        this.structuredPDXValueIdentifiers = List.of(structuredPDXValueIdentifier);
    }

    public DynamicPDX(@NotNull U structuredBlock, List<String> structuredPDXValueIdentifier) {
        this.simplePDXSupplier = null;
        this.structuredBlock = structuredBlock;
        this.structuredPDXValueIdentifiers = structuredPDXValueIdentifier;
    }

    public DynamicPDX(@NotNull Supplier<PDXScript<V>> simplePDXSupplier) {
        this.simplePDXSupplier = simplePDXSupplier;
        this.structuredBlock = null;
        this.structuredPDXValueIdentifiers = null;
    }

    public DynamicPDX(@NotNull Supplier<PDXScript<V>> simplePDXSupplier, String structuredPDXValueIdentifier) {
        this.simplePDXSupplier = simplePDXSupplier;
        this.structuredBlock = null;
        this.structuredPDXValueIdentifiers = List.of(structuredPDXValueIdentifier);
    }

    public DynamicPDX(@NotNull Supplier<PDXScript<V>> simplePDXSupplier, List<String> structuredPDXValueIdentifier) {
        this.simplePDXSupplier = simplePDXSupplier;
        this.structuredBlock = null;
        this.structuredPDXValueIdentifiers = structuredPDXValueIdentifier;
    }

    public void setSimplePDX(PDXScript<V> simplePDX) {
        this.simplePDX = simplePDX;
        this.setNull(structuredBlock);
    }

    private void setNull(AbstractPDX<?> pdx) {
        if (pdx != null) {
            pdx.setNull();
        }
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
        } else if (blockAllowed()) {
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
        } else if (blockAllowed()) {
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
        if (blockAllowed())
            structuredBlock.loadPDX(expressions);
        simplePDX.setNull();
    }

    @Override
    public boolean isValidIdentifier(Node node) {
        // todo i hope this works as intended
        if (!isBlock()) {
            return simplePDX.isValidIdentifier(node);
        } else if (blockAllowed()) {
            return structuredBlock.isValidIdentifier(node);
        } else {
            return false;
        }
    }

    private boolean blockAllowed() {
        return structuredBlock != null;
    }

    @Override
    public void setNull() {
        simplePDX = null;
        setNull(structuredBlock);
    }

    @Override
    public void loadOrElse(Node exp, V value) {
        if (!isBlock()) {
            simplePDX.loadOrElse(exp, value);
        } else if (blockAllowed()) {
            structuredBlock.loadOrElse(exp, null);
//            setStructuredBlockValue(value);
        }
    }

    @Override
    public String toScript() {
        if (!isBlock()) {
            return simplePDX.toScript();
        } else if (blockAllowed()) {
            return structuredBlock.toScript();
        } else {
            return "[null DynamicPDX]";
        }
    }

    @Override
    public boolean objEquals(PDXScript<?> other) {
        if (!isBlock()) {
            return simplePDX.objEquals(other);
        } else if (blockAllowed()) {
            return structuredBlock.objEquals(other);
        } else {
            return false;
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
    private @Nullable PDXScript<V> getStructuredValueProperty() {
        if (isBlock() && blockAllowed() && structuredPDXValueIdentifiers != null) {
            return structuredBlock.<V>getPDXPropertyOfType(structuredPDXValueIdentifiers);
        } else {
            return null;
        }
    }

    @Override
    public String getPDXIdentifier() {
        if (!isBlock()) {
            return simplePDX.getPDXIdentifier();
        } else if (blockAllowed()) {
            return structuredBlock.getPDXIdentifier();
        } else {
            return null;
        }
    }

    public PDXScript<?> getPDXScript() {
        if (!isBlock()) {
            return simplePDX;
        } else {
            return structuredBlock;
        }
    }
}
