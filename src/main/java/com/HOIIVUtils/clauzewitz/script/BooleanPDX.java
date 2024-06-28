package com.HOIIVUtils.clauzewitz.script;

import com.HOIIVUtils.clauzewitz.BoolType;
import com.HOIIVUtils.clausewitz_parser.Node;
import com.HOIIVUtils.clausewitz_parser.NodeValue;

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
    public boolean objEquals(PDXScript<?> other) {
        if (other instanceof BooleanPDX pdx) {
            return obj.equals(pdx.get());
        }
        return false;
    }
}
