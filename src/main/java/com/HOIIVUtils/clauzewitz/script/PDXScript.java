package com.HOIIVUtils.clauzewitz.script;

import com.HOIIVUtils.clausewitz_parser.Node;
import com.HOIIVUtils.clausewitz_parser.ParserException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.List;

public interface PDXScript<T> {
    void set(T obj);

    void set(Node expression) throws UnexpectedIdentifierException, NodeValueTypeException;

    @Nullable
    T get();

    void loadPDX(Node expression) throws UnexpectedIdentifierException;

    void loadPDX(List<Node> expressions);

    //void loadPDX(@NotNull File file);

    boolean isValidIdentifier(Node node);

    void setNull();

    void loadOrElse(Node exp, T value);

    String toScript();

    boolean objEquals(PDXScript<?> other);

    T getOrElse(T elseValue);

    boolean isUndefined();
}
