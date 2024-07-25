package com.hoi4utils.clausewitz.script;

import com.hoi4utils.clausewitz.BoolType;
import com.hoi4utils.clausewitz_parser.Node;
import com.hoi4utils.clausewitz_parser.NodeValue;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class BooleanPDX extends AbstractPDX<Boolean> {
    private final BoolType boolType;
    /**
     * this is separate to obj because
     * if we want to clear the value of obj, we would still return this value by default.
     */
    private final boolean defaultValue;

    public BooleanPDX(String pdxIdentifiers, boolean defaultValue, BoolType boolType) {
        super(pdxIdentifiers);
        this.boolType = boolType;
        this.defaultValue = defaultValue;
    }

    public BooleanPDX(List<String> pdxIdentifiers, boolean defaultValue, BoolType boolType) {
        super(pdxIdentifiers);
        this.boolType = boolType;
        this.defaultValue = defaultValue;
    }

    @Override
    public void set(Node expression) throws UnexpectedIdentifierException, NodeValueTypeException {
        usingIdentifier(expression);
        NodeValue value = expression.value();

        if (value.valueObject() instanceof String str) {
            obj = value.bool(boolType);
        } else {
            throw new NodeValueTypeException(expression, "String parsable as Bool matching enum + " + boolType.toString());
        }
    }

    @Override
    public @NotNull Boolean get() {
        var val = super.get();
        if (val == null) {
            return defaultValue;
        }
        return val;
    }

    @Override
    public boolean objEquals(PDXScript<?> other) {
        if (other instanceof BooleanPDX pdx) {
            return obj.equals(pdx.get());
        }
        return false;
    }

    public boolean invert() {
        set(!get());
        return get();
    }
}
