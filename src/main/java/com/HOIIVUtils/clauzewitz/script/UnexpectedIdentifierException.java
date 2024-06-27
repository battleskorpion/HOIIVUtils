package com.HOIIVUtils.clauzewitz.script;

import com.HOIIVUtils.clausewitz_parser.Node;

public class UnexpectedIdentifierException extends Exception {
    public UnexpectedIdentifierException(String message) {
        super(message);
    }

    public UnexpectedIdentifierException(Node exp) {
        super("Unexpected identifier: " + exp.name());
    }
}
