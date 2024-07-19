package com.HOIIVUtils.clauzewitz.script;

import com.HOIIVUtils.clausewitz_parser.Node;
import com.HOIIVUtils.clausewitz_parser.NodeValue;
import com.HOIIVUtils.clausewitz_parser.Parser;
import com.HOIIVUtils.clausewitz_parser.ParserException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.List;

/**
 * Any object that can be converted to a PDX script block, such as a focus, national focus tree,
 * or event.
 * <p>
 */
public abstract class AbstractPDX<T> implements PDXScript<T> {
    protected T obj;
    protected final List<String> pdxIdentifiers;
    int activeIdentifier = 0;

    public AbstractPDX(String pdxIdentifiers) {
        this.pdxIdentifiers = List.of(pdxIdentifiers);
    }

    public AbstractPDX(String... PDXIdentifiers) {
        this.pdxIdentifiers = List.of(PDXIdentifiers);
    }

    public AbstractPDX(List<String> pdxIdentifiers) {
        this.pdxIdentifiers = pdxIdentifiers;
    }

    protected void usingIdentifier(Node exp) throws UnexpectedIdentifierException {
        for (int i = 0; i < pdxIdentifiers.size(); i++) {
            if (exp.nameEquals(pdxIdentifiers.get(i))) {
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

        try {
            obj = (T) value.valueObject();
        } catch (ClassCastException e) {
            throw new NodeValueTypeException(expression, e);
        }
    }

    public @Nullable T get() {
        return obj;
    }

    public void loadPDX(Node expression) throws UnexpectedIdentifierException {
        if (expression.name() == null) {
            System.out.println("Error loading PDX script: " + expression);
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
                .ifPresentOrElse(expression -> {
                    try {
                        loadPDX(expression);
                    } catch (UnexpectedIdentifierException e) {
                        throw new RuntimeException(e);
                    }
                }, this::setNull);
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
        try {
            loadPDX(rootNode);
        } catch (UnexpectedIdentifierException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isValidIdentifier(Node node) {
        for (String identifier : pdxIdentifiers) {
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
        try {
            loadPDX(exp);
        } catch (UnexpectedIdentifierException e) {
            throw new RuntimeException(e);
        }
        if (obj == null) {
            obj = value;
        }
    }

    public @Nullable String toScript() {
        if (obj == null) return null;
        return pdxIdentifiers.get(activeIdentifier) + " = " + obj + "\n";
    }

    public boolean objEquals(AbstractPDX<?> other) {
        if (obj == null) {
            return false;
        }
        if (other.obj == null) {
            return false;
        }
        return obj.equals(other.obj);
    }

    public T getOrElse(T elseValue) {
        return isUndefined() ? elseValue : obj;
    }

    @Override
    public String toString() {
        if (obj == null) {
            return super.toString();
        }
        return obj.toString();
    }

    public boolean isUndefined() {
        return obj == null;
    }

    public String getPDXIdentifier() {
        return pdxIdentifiers.get(activeIdentifier);
    }
}