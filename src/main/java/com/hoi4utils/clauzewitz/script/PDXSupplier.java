package main.java.com.hoi4utils.clauzewitz.script;

@FunctionalInterface
public interface PDXSupplier<T extends PDXScript<?>> {
    T apply(Object... args);
}