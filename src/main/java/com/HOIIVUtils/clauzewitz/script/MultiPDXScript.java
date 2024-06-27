package com.HOIIVUtils.clauzewitz.script;

import com.HOIIVUtils.clausewitz_parser.Node;
import com.HOIIVUtils.clausewitz_parser.NodeValue;
import com.HOIIVUtils.clauzewitz.data.focus.Focus;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * PDX Script that can have multiple instantiation.
 * Example: multiple icon definitions in a focus.
 * The super PDXScript object will be a list of T objects.
 * @param <T>
 */
public class MultiPDXScript<T> extends PDXScript<List<T>> implements Iterable<T> {

    public MultiPDXScript(String PDXIdentifier) {
        super(PDXIdentifier);
        obj = new ArrayList<>();
    }

    public MultiPDXScript(String... PDXIdentifiers) {
        super(PDXIdentifiers);
        obj = new ArrayList<>();
    }

    @Override
    public void loadPDX(Node expression) {
        try {
            add(expression);
        } catch (UnexpectedIdentifierException | NodeValueTypeException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void loadPDX(List<Node> expressions) {
        if (expressions == null) return;
        expressions.stream().filter(this::isValidIdentifier)
                .forEach(this::loadPDX);
    }

    @Override
    public List<T> get() {
        return super.get();
    }

    protected void add(Node expression) throws UnexpectedIdentifierException, NodeValueTypeException {
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
                obj.add((T) value.valueObject());
            } catch (ClassCastException e) {
                throw new NodeValueTypeException(expression, e);
            }
        }
    }

    public void clear() {
        obj.clear();
    }

    public boolean isEmpty() {
        return get().isEmpty();
    }

    @NotNull
    @Override
    public Iterator<T> iterator() {
        return get().iterator();
    }

    @Override
    public void forEach(Consumer<? super T> action) {
        get().forEach(action);
    }

    @Override
    public Spliterator<T> spliterator() {
        return get().spliterator();
    }

    public int size() {
        return get().size();
    }


    public Stream<T> stream() {
        return get().stream();
    }
}


