package com.HOIIVUtils.clauzewitz.script;

import com.HOIIVUtils.clausewitz_parser.Node;
import com.HOIIVUtils.clausewitz_parser.NodeValue;

import java.util.List;

public class IntegerPDX extends AbstractPDX<Integer> {
    public IntegerPDX(String pdxIdentifiers) {
        super(pdxIdentifiers);
    }

    public IntegerPDX(String... PDXIdentifiers) {
        super(PDXIdentifiers);
    }

    public IntegerPDX(List<String> pdxIdentifiers) {
        super(pdxIdentifiers);
    }

    @Override
    public void set(Node expression) throws UnexpectedIdentifierException, NodeValueTypeException {
        usingIdentifier(expression);
        NodeValue value = expression.value();

        if (value.valueObject() instanceof Number num) {
            obj = num.intValue();
        } else {
            throw new NodeValueTypeException(expression, "Number (as an Integer)");
        }
    }

    @Override
    public boolean objEquals(PDXScript<?> other) {
        if (other instanceof IntegerPDX pdx) {
            return obj.equals(pdx.get());
        }
        return false;
    }
}
