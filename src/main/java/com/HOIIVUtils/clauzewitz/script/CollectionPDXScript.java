package com.HOIIVUtils.clauzewitz.script;

import com.HOIIVUtils.clausewitz_parser.Node;
import com.HOIIVUtils.clausewitz_parser.NodeValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

abstract public class CollectionPDXScript<T extends PDXScript<?>> extends AbstractPDX<List<T>> implements Iterable<T> {

    public CollectionPDXScript(@NotNull String... pdxIdentifiers) {
        super(pdxIdentifiers);
        obj = new ArrayList<>();
    }

    public CollectionPDXScript(@NotNull List<String> pdxIdentifiers) {
        super(pdxIdentifiers);
        obj = new ArrayList<>();
    }

    @Override
    public void loadPDX(Node expression) throws UnexpectedIdentifierException {
        try {
            add(expression);
        } catch (NodeValueTypeException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void loadPDX(List<Node> expressions) {
        if (expressions == null) return;
        expressions.stream().filter(this::isValidIdentifier)
                .forEach(expression -> {
                    try {
                        loadPDX(expression);
                    } catch (UnexpectedIdentifierException e) {
                        System.err.println(e.getMessage());
                        //throw new RuntimeException(e);
                    }
                });
    }

    @Override
    public boolean objEquals(PDXScript<?> other) {
        return false; // todo? well.
    }

    @Override
    public List<T> get() {
        return super.get();
    }


    protected void add(Node expression) throws UnexpectedIdentifierException, NodeValueTypeException {
        //usingIdentifier(expression);  // could be any identifier based on T
        NodeValue value = expression.value();

        // if this PDXScript is an encapsulation of PDXScripts (such as Focus)
        // then load each sub-PDXScript
        if (obj instanceof ArrayList<T> childScriptList) {
            T childScript = newChildScript(expression);
            childScript.loadPDX(expression);
            childScriptList.add(childScript);
        } else {
            try {
                obj.add((T) value.valueObject());
            } catch (ClassCastException e) {
                throw new NodeValueTypeException(expression, e);
            }
        }
    }

    abstract protected T newChildScript(Node expression);

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

    @Override
    public boolean isUndefined() {
        return obj.isEmpty();
    }

    @Override
    public @Nullable String toScript() {
        StringBuilder sb = new StringBuilder();
        var scripts = get();
        if (scripts == null) return null;
        for (T pdxScript : scripts) {
            var str = pdxScript.toScript();
            if (str == null) continue;
            sb.append(str);
        }
        return sb.toString();
    }
}


