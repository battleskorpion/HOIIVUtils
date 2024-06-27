package com.HOIIVUtils.clauzewitz.script;

import com.HOIIVUtils.clausewitz_parser.Node;
import com.HOIIVUtils.clausewitz_parser.NodeValue;
import org.apache.poi.ss.formula.functions.T;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public abstract class ComplexPDXScript extends PDXScript<PDXScriptList> {

    public ComplexPDXScript(String pdxIdentifier) {
        super(pdxIdentifier); 
        obj = new PDXScriptList();
    }

    public ComplexPDXScript(String... pdxIdentifiers) {
        super(pdxIdentifiers);
        obj = new PDXScriptList();  
    }

    protected abstract Collection<? extends PDXScript<?>> childScripts();

    @Override
    public void set(Node expression) throws UnexpectedIdentifierException, NodeValueTypeException {
        usingIdentifier(expression);
        NodeValue value = expression.value();

        // then load each sub-PDXScript
        if (!value.isList()) throw new NodeValueTypeException(expression, "list");
        for (PDXScript<?> pdxScript : obj) {
            pdxScript.loadPDX(value.list());
        }
    }

    @Override
    public void loadPDX(Node expression) {
        if (expression.name() == null) {
            if (expression.value().isList())
                loadPDX(expression.value().list());
            else {
                System.out.println("Error loading PDX script: " + expression);
            }
            return;
        }

        try {
            set(expression);
        } catch (UnexpectedIdentifierException | NodeValueTypeException e) {
            System.out.println("Error loading PDX script:" + e.getMessage() + "\n\t" + expression);
        }
    }
}
