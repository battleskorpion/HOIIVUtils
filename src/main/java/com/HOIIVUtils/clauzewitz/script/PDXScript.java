package com.HOIIVUtils.clauzewitz.script;

import com.HOIIVUtils.clausewitz_parser.Node;
import com.HOIIVUtils.clausewitz_parser.NodeValue;
import com.HOIIVUtils.clausewitz_parser.Parser;
import com.HOIIVUtils.clausewitz_parser.ParserException;
import com.HOIIVUtils.clauzewitz.data.focus.Focus;
import com.HOIIVUtils.clauzewitz.data.focus.Icon;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 * Any object that can be converted to a PDX script block, such as a focus, national focus tree,
 * or event.
 * <p>
 */
public class PDXScript<T> {
    protected T obj;
    private final List<String> PDXIdentifier;
    int activeIdentifier = 0;

    public PDXScript(String PDXIdentifier) {
        this.PDXIdentifier = List.of(PDXIdentifier);
    }

    public PDXScript(String... PDXIdentifiers) {
        this.PDXIdentifier = List.of(PDXIdentifiers);
    }

    protected void usingIdentifier(Node exp) throws UnexpectedIdentifierException {
        for (int i = 0; i < PDXIdentifier.size(); i++) {
            if (exp.nameEquals(PDXIdentifier.get(i))) {
                activeIdentifier = i;
                return;
            }
        }
        throw new UnexpectedIdentifierException(exp);
    }

    public void set(T obj) {
        this.obj = obj;
    }

    @SuppressWarnings("unchecked")
    public void set(Node expression) throws UnexpectedIdentifierException, NodeValueTypeException {
        usingIdentifier(expression);
        NodeValue value = expression.value();

        // if this PDXScript is an encapsulation of PDXScripts (such as Focus)
        // then load each sub-PDXScript
        if (obj instanceof PDXScriptList childScriptList) {
            if (!value.isList()) throw new NodeValueTypeException(expression, "list");
            for (PDXScript<?> pdxScript : childScriptList) {
                pdxScript.loadPDX(value.list());
            }
        } else {
            try {
                obj = (T) value.valueObject();
            } catch (ClassCastException e) {
                throw new NodeValueTypeException(expression, e);
            }
        }
    }

    public T get() {
        return obj;
    }

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

    public void loadPDX(List<Node> expressions) {
        if (expressions == null) return;
        expressions.stream().filter(this::isValidIdentifier)
                .findFirst()
                .ifPresentOrElse(this::loadPDX, this::setNull);
    }

    protected void loadPDX(@NotNull File file) {
        if (!file.exists()) {
            System.err.println("Focus tree file does not exist: " + file);
            return;
        }

        /* parser */
        var pdxParser = new Parser(file);
        Node rootNode;
        try {
            rootNode = pdxParser.parse();
        } catch (ParserException e) {
            System.err.println("Error parsing focus tree file: " + file);
            return;
        }
        loadPDX(rootNode);
    }

    protected boolean isValidIdentifier(Node node) {
        for (String identifier : PDXIdentifier) {
            if (node.name().equals(identifier)) {
                return true;
            }
        }
        return false;
    }

    public void setNull() {
        obj = null;
    }

    public void loadOrElse(Node exp, T value) {
        loadPDX(exp);
        if (obj == null) {
            obj = value;
        }
    }

    protected void setChildScripts(PDXScriptList pdxScripts) {
        if (obj instanceof PDXScriptList childScriptList) {
            childScriptList.addAll(pdxScripts);
        }
    }

    protected void setChildScripts(PDXScript<?>... pdxScripts) {
        if (obj instanceof PDXScriptList childScriptList) {
            childScriptList.addAll(Arrays.asList(pdxScripts));
        }
    }

    public String toScript() {
        return PDXIdentifier.get(activeIdentifier) + " = " + obj.toString();
    }

    public boolean objEquals(PDXScript<?> other) {
        if (obj == null) {
            return false;
        }
        if (other.obj == null) {
            return false;
        }
        return obj.equals(other.obj);
    }

    public T getOrElse(T elseValue) {
        return obj == null ? elseValue : obj;
    }

    @Override
    public String toString() {
        if (obj == null) {
            return super.toString();
        }
        return obj.toString();
    }
}