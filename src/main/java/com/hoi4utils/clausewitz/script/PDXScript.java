//package com.hoi4utils.clausewitz.script;
//
//import com.hoi4utils.clausewitz_parser.Node;
//import org.jetbrains.annotations.Nullable;
//
//import java.util.List;
//
//public interface PDXScript<T> {
//    void set(T obj);
//
//    void set(Node expression) throws UnexpectedIdentifierException, NodeValueTypeException;
//
//    @Nullable
//    T get();
//
//    void loadPDX(Node expression) throws UnexpectedIdentifierException;
//
//    void loadPDX(List<Node> expressions);
//
//    //void loadPDX(@NotNull File file);
//
//    boolean isValidIdentifier(Node node);
//
//    void setNull();
//
//    void loadOrElse(Node exp, T value);
//
//    String toScript();
//
//    boolean objEquals(PDXScript<?> other);
//
//    T getOrElse(T elseValue);
//
//    boolean isUndefined();
//
//    String getPDXIdentifier();
//
//}
