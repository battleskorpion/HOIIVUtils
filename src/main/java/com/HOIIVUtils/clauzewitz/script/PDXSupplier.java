package com.HOIIVUtils.clauzewitz.script;

@FunctionalInterface
public interface PDXSupplier<T extends PDXScript<?>> {
    T apply(Object... args);
}