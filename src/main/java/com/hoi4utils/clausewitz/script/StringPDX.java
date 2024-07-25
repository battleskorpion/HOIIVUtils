//package com.hoi4utils.clausewitz.script;
//
//import com.hoi4utils.clausewitz_parser.Node;
//import com.hoi4utils.clausewitz_parser.NodeValue;
//
//import java.util.List;
//
//public class StringPDX extends AbstractPDX<String> {
//    public StringPDX(String pdxIdentifiers) {
//        super(pdxIdentifiers);
//    }
//
//    public StringPDX(String... PDXIdentifiers) {
//        super(PDXIdentifiers);
//    }
//
//    public StringPDX(List<String> pdxIdentifiers) {
//        super(pdxIdentifiers);
//    }
//
//    @Override
//    public void set(Node expression) throws UnexpectedIdentifierException, NodeValueTypeException {
//        usingIdentifier(expression);
//        NodeValue value = expression.value();
//
//        if (value.valueObject() instanceof String str) {
//            obj = str;
////        } else {
////            throw new NodeValueTypeException(expression, "Number (as an Integer)");
////        }
//        } else {
//            obj = value.asString();
//        }
//    }
//
//    @Override
//    public boolean nodeEquals(PDXScript<?> other) {
//        if (other instanceof StringPDX) {
//            return obj.equals(((StringPDX) other).obj);
//        }
//        return false;
//    }
//
//    public boolean objEquals(String s) {
//        return obj.equals(s);
//    }
//}
