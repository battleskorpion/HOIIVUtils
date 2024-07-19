package main.java.com.hoi4utils.clauzewitz.script;

import main.kotlin.com.hoi4utils.clausewitz_parser.Node;
import main.java.com.hoi4utils.clausewitz_parser.NodeValue;

import java.util.*;

public abstract class StructuredPDX extends AbstractPDX<PDXScriptList> {

    public StructuredPDX(String pdxIdentifier) {
        super(pdxIdentifier);
        obj = new PDXScriptList();
    }

    public StructuredPDX(List<String> pdxIdentifiers) {
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


    /**
     * Gets the child pdx property with the current identifier matching
     * the given string.
     * @param identifier
     */
    public PDXScript<?> getPDXProperty(String identifier) {
        for (PDXScript<?> pdx : childScripts()) {
            if (pdx.getPDXIdentifier().equals(identifier)) {
                return pdx;
            }
        }
        return null;
    }

    /**
     * Gets the child pdx property with the current identifier matching
     * the given string.
     * @param identifiers
     */
    public PDXScript<?> getPDXProperty(List<String> identifiers) {
        for (String identifier : identifiers) {
            var pdx = getPDXProperty(identifier);
            if (pdx != null) {
                return pdx;
            }
        }
        return null;
    }

    /**
     * Gets the child pdx property with the current identifier matching
     * the given string.
     * @param identifier
     */
    @SuppressWarnings("unchecked")
    public <R> PDXScript<R> getPDXPropertyOfType(String identifier) {
        for (PDXScript<?> pdx : childScripts()) {
            if (pdx.getPDXIdentifier().equals(identifier)) {
                return (PDXScript<R>) pdx;
            }
        }
        return null;
    }

    /**
     * Gets the child pdx property with the current identifier matching
     * the given string.
     * @param identifiers
     */
    public <R> PDXScript<R> getPDXPropertyOfType(List<String> identifiers) {
        for (String identifier : identifiers) {
            PDXScript<R> pdx = getPDXPropertyOfType(identifier);
            if (pdx != null) {
                return pdx;
            }
        }
        return null;
    }

    public Collection<? extends PDXScript<?>> pdxProperties() {
        var scripts = childScripts();
        if (scripts == null) return null;
        return Collections.unmodifiableCollection(scripts);
    }
}
