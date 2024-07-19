package main.java.com.hoi4utils.clauzewitz.script;

import main.kotlin.com.hoi4utils.clausewitz_parser.Node;
import main.java.com.hoi4utils.clausewitz_parser.NodeValue;

import java.util.List;

public class DoublePDX extends AbstractPDX<Double> {
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

    @Override
    public boolean objEquals(PDXScript<?> other) {
        if (other instanceof DoublePDX pdx) {
            return obj.equals(pdx.get());
        }
        return false;
    }
}
