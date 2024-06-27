package com.HOIIVUtils.clauzewitz.script;

import com.HOIIVUtils.clausewitz_parser.Node;
import com.HOIIVUtils.clausewitz_parser.NodeValue;

import java.util.List;

public class DoublePDX extends PDXScript<Double>{
    public DoublePDX(String pdxIdentifiers) {
        super(pdxIdentifiers);
    }

    public DoublePDX(String... PDXIdentifiers) {
        super(PDXIdentifiers);
    }

    public DoublePDX(List<String> pdxIdentifiers) {
        super(pdxIdentifiers);
    }

    @Override
    public void set(Node expression) throws UnexpectedIdentifierException, NodeValueTypeException {
        usingIdentifier(expression);
        NodeValue value = expression.value();

        if (value.valueObject() instanceof Number num) {
            obj = num.doubleValue();
        } else {
            throw new NodeValueTypeException(expression, "Number (as a Double)");
        }
    }
}
