package com.hoi4utils.clausewitz.script;

import com.hoi4utils.clausewitz_parser.Node;
import com.hoi4utils.clausewitz_parser.NodeValue;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

// todo uhhhhhhhhhhhhh
public class MultiReferencePDXScript<T extends AbstractPDX<?>> extends MultiPDXScript<T> {
    protected final Supplier<Collection<T>> referenceCollectionSupplier;
    protected final Function<T, String> idExtractor;
    protected final List<String> referencePDXTokenIdentifiers;
    protected final List<String> referenceNames = new ArrayList<>();

    public MultiReferencePDXScript(Supplier<Collection<T>> referenceCollectionSupplier,
                                   Function<T, String> idExtractor, String PDXIdentifier, String referenceIdentifier) {
        super(null, PDXIdentifier);
        this.referenceCollectionSupplier = referenceCollectionSupplier;
        this.idExtractor = idExtractor;
        this.referencePDXTokenIdentifiers = List.of(referenceIdentifier);
    }

    public MultiReferencePDXScript(Supplier<Collection<T>> referenceCollectionSupplier,
                                   Function<T, String> idExtractor, List<String> PDXIdentifiers, List<String> pdxReferenceIdentifier) {
        super(null, PDXIdentifiers);
        this.referenceCollectionSupplier = referenceCollectionSupplier;
        this.idExtractor = idExtractor;
        this.referencePDXTokenIdentifiers = pdxReferenceIdentifier;
    }

    @Override
    public void loadPDX(Node expression) throws UnexpectedIdentifierException {
        if (expression.value().isList()) {
            var list = expression.value().list();
            if (list == null) {
                System.out.println("PDX script had empty list: " + expression);
                return;
            }
            usingIdentifier(expression);
            for (Node node : list) {
                try {
                    add(node);
                } catch (NodeValueTypeException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        // todo supposed to be here? unsure.
        else {
            try {
                add(expression);
            } catch (UnexpectedIdentifierException | NodeValueTypeException e) {
                throw new RuntimeException(e);
            }
        }
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
        usingReferenceIdentifier(expression);
        NodeValue value = expression.value();
        referenceNames.clear();
        referenceNames.add(value.string());
    }

    @Override
    protected void add(Node expression) throws UnexpectedIdentifierException, NodeValueTypeException {
        usingReferenceIdentifier(expression);
        NodeValue value = expression.value();

        referenceNames.add(value.string());
    }

    private List<T> resolveReferences() {
        Collection<T> referenceCollection = referenceCollectionSupplier.get();
        for (T reference : referenceCollection) {
            for (String referenceName : referenceNames) {
                if (idExtractor.apply(reference).equals(referenceName)) {
                    obj.add(reference);
                }
            }
        }
        return obj;
    }

    protected void usingReferenceIdentifier(Node exp) throws UnexpectedIdentifierException {
        for (int i = 0; i < referencePDXTokenIdentifiers.size(); i++) {
            if (exp.nameEquals(referencePDXTokenIdentifiers.get(i))) {
//                activeReferenceIdentifier = i;
                return;
            }
        }
        throw new UnexpectedIdentifierException(exp);
    }

    @Override
    public @Nullable String toScript() {
        StringBuilder sb = new StringBuilder();
        var scripts = get();
        if (scripts == null) return null;
        for (var identifier : referenceNames) {
            sb.append(getPDXIdentifier()).append(" = ").append(identifier).append("\n");
        }
        return sb.toString();
    }

    public void setReferenceName(int index, String value) {
        referenceNames.set(index, value);
    }

    public String getReferenceName(int i) {
        return referenceNames.get(i);
    }

    public List<String> getReferenceCollectionNames() {
        return referenceCollectionSupplier.get().stream().map(idExtractor).toList();
    }

    public void addReferenceName(String newValue) {
        referenceNames.add(newValue);
        resolveReferences();
    }

    /**
     * Size of actively valid references (resolved PDXScript object references)
     * @return
     */
    @Override
    public int size() {
        var list = get();
        if (list == null) return 0;
        return list.size();
    }

    public int referenceSize() {
        return referenceNames.size();
    }
}