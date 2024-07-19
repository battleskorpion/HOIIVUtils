package main.java.com.hoi4utils.clauzewitz.script;

import main.kotlin.com.hoi4utils.clausewitz_parser.Node;

public class UnexpectedIdentifierException extends Exception {
    public UnexpectedIdentifierException(String message) {
        super(message);
    }

    public UnexpectedIdentifierException(Node exp) {
        super("Unexpected identifier: " + exp.name());
    }
}
