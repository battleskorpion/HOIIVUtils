package com.HOIIVUtils.clauzewitz.script;

import com.HOIIVUtils.clausewitz_parser.Node;
import com.HOIIVUtils.clausewitz_parser.NodeValue;
import org.apache.poi.ss.formula.functions.T;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

public abstract class StructuredPDX extends AbstractPDX<PDXScriptList> {

    public StructuredPDX(String pdxIdentifier) {
        super(pdxIdentifier); 
        obj = new PDXScriptList();
    }

    public StructuredPDX(List<String> pdxIdentifiers) {
        super(pdxIdentifiers);
        obj = new PDXScriptList();  
    }

    protected abstract Collection<? extends AbstractPDX<?>> childScripts();

    @Override
    public void set(Node expression) throws UnexpectedIdentifierException, NodeValueTypeException {
        usingIdentifier(expression);
        NodeValue value = expression.value();

        // then load each sub-PDXScript
        if (!value.isList()) throw new NodeValueTypeException(expression, "list");
        for (AbstractPDX<?> pdxScript : obj) {
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
    public AbstractPDX<?> getPDXProperty(String identifier) {
        for (AbstractPDX<?> pdx : childScripts()) {
            if (pdx.getActiveIdentifier().equals(identifier)) {
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
    public AbstractPDX<?> getPDXProperty(List<String> identifiers) {
        for (String identifier : identifiers) {
            var pdx = getPDXProperty(identifier);
            if (pdx != null) {
                return pdx;
            }
        }
        return null;
    }

//    /**
//     * Gets the child pdx property with the current identifier matching
//     * the given string.
//     * @param identifier
//     */
//    public <T extends AbstractPDX<?>> T getPDXPropertyOfType(Class<T> type, String identifier) {
//        for (AbstractPDX<?> pdx : childScripts()) {
//            if (pdx.getActiveIdentifier().equals(identifier) && type.isInstance(pdx)) {
//                return type.cast(pdx);
//            }
//        }
//        return null;
//    }
//
//    /**
//     * Gets the child pdx property with the current identifier matching
//     * the given string.
//     * @param identifiers
//     */
//    public <T extends AbstractPDX<?>> T getPDXPropertyOfType(Class<T> type, List<String> identifiers) {
//        for (String identifier : identifiers) {
//            var pdx = getPDXPropertyOfType(type, identifier);
//            if (pdx != null) {
//                return pdx;
//            }
//        }
//        return null;
//    }
    /**
     * Gets the child pdx property with the current identifier matching
     * the given string.
     * @param identifier
     */
    @SuppressWarnings("unchecked")
    public <R> AbstractPDX<R> getPDXPropertyOfType(String identifier) {
        for (AbstractPDX<?> pdx : childScripts()) {
            if (pdx.getActiveIdentifier().equals(identifier)) {
                return (AbstractPDX<R>) pdx;
            }
        }
        return null;
    }

    /**
     * Gets the child pdx property with the current identifier matching
     * the given string.
     * @param identifiers
     */
    public <R> AbstractPDX<R> getPDXPropertyOfType(List<String> identifiers) {
        for (String identifier : identifiers) {
            AbstractPDX<R> pdx = getPDXPropertyOfType(identifier);
            if (pdx != null) {
                return pdx;
            }
        }
        return null;
    }
}
