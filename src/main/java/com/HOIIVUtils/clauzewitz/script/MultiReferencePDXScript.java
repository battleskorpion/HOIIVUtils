package com.HOIIVUtils.clauzewitz.script;

import com.HOIIVUtils.clausewitz_parser.Node;
import com.HOIIVUtils.clausewitz_parser.NodeValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class MultiReferencePDXScript<T extends PDXScript<?>> extends MultiPDXScript<T> {
    protected final Supplier<Collection<T>> referenceCollectionSupplier;
    protected final Function<T, String> idExtractor;
    protected final List<String> referenceIdentifiers;

    public MultiReferencePDXScript(Supplier<Collection<T>> referenceCollectionSupplier,
                                   Function<T, String> idExtractor, String PDXIdentifier) {
        super(PDXIdentifier);
        this.referenceCollectionSupplier = referenceCollectionSupplier;
        this.idExtractor = idExtractor;
        referenceIdentifiers = new ArrayList<>();
    }

    public MultiReferencePDXScript(Supplier<Collection<T>> referenceCollectionSupplier,
                                   Function<T, String> idExtractor, String... PDXIdentifiers) {
        super(PDXIdentifiers);
        this.referenceCollectionSupplier = referenceCollectionSupplier;
        this.idExtractor = idExtractor;
        referenceIdentifiers = new ArrayList<>();
    }

    @Override
    public List<T> get() {
        if (obj != null && !obj.isEmpty()) {
            return obj;
        }
        return resolveReferences();
    }

    @Override
    public void set(Node expression) throws UnexpectedIdentifierException, NodeValueTypeException {
        usingIdentifier(expression);
        NodeValue value = expression.value();
        referenceIdentifiers.clear();
        referenceIdentifiers.add(value.string());
    }

    @Override
    protected void add(Node expression) throws UnexpectedIdentifierException, NodeValueTypeException {
        usingIdentifier(expression);
        NodeValue value = expression.value();

        referenceIdentifiers.add(value.string());
    }

    private List<T> resolveReferences() {
        Collection<T> referenceCollection = referenceCollectionSupplier.get();
        for (T reference : referenceCollection) {
            for (String referenceIdentifier : referenceIdentifiers) {
                if (idExtractor.apply(reference).equals(referenceIdentifier)) {
                    obj.add(reference);
                }
            }
        }
        return obj;
    }
}