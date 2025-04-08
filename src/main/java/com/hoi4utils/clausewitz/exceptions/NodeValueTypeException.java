package com.hoi4utils.clausewitz.exceptions;

import com.hoi4utils.clausewitz_parser.Node;

public class NodeValueTypeException extends Exception {
    public NodeValueTypeException(Node exp,  Class<?> clazz) {
        super("[" + clazz.getName() + "] Invalid node value type: " + exp.name());
    }

    public NodeValueTypeException(Node exp, Throwable cause, Class<?> clazz) {
        super("[" + clazz.getName() + "] Invalid node value type: " + exp.name(), cause);
    }

    public NodeValueTypeException(Node expression, String expected, Class<?> clazz) {
        super("[" + clazz.getName() + "] Invalid node value type of expression: "
                + expression.name() + ", Expected: " + expected);
    }
}