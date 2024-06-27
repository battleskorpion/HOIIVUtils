package com.HOIIVUtils.clauzewitz.script;

import com.HOIIVUtils.clausewitz_parser.Node;

public class NodeValueTypeException extends Exception {
    public NodeValueTypeException(Node exp) {
        super("Invalid node value type: " + exp.name());
    }

    public NodeValueTypeException(Node exp, Throwable cause) {
        super("Invalid node value type: " + exp.name(), cause);
    }

    public NodeValueTypeException(Node expression, String expected) {
        super("Invalid node value type: " + expression.name() + " Expected: " + expected);
    }
}