package com.hoi4utils.ui.pdxscript;

import com.hoi4utils.clausewitz.localization.Localizable;
import com.hoi4utils.clausewitz.localization.Property;
import com.hoi4utils.clausewitz.script.*;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.util.Callback;

/**
 * Utility class that creates a TreeView to display any PDXScript hierarchy.
 */
public class PDXTreeViewFactory {

    /**
     * Builds a TreeView from a root PDXScript.
     *
     * @param rootScript The root PDXScript (e.g., a StructuredPDX or CollectionPDX).
     * @return A fully populated TreeView<PDXScript<?>>.
     */
    public static TreeView<PDXScript<?>> createPDXTreeView(PDXScript<?> rootScript) {
        // Recursively build the TreeItem hierarchy
        TreeItem<PDXScript<?>> rootItem = buildTreeItem(rootScript);

        // Create the TreeView with the root item
        TreeView<PDXScript<?>> treeView = new TreeView<>(rootItem);
        treeView.setShowRoot(true);
        treeView.setMaxWidth(Double.MAX_VALUE);
        treeView.setMaxHeight(Double.MAX_VALUE);

        // Customize how each cell is displayed
        treeView.setCellFactory(new Callback<>() {
            @Override
            public TreeCell<PDXScript<?>> call(TreeView<PDXScript<?>> param) {
                return new TreeCell<>() {
                    @Override
                    protected void updateItem(PDXScript<?> item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                            setGraphic(null);
                        } else {
                            setText(generateCellText(item));
                        }
                    }
                };
            }
        });

        // (Optional) expand the root by default
        rootItem.setExpanded(true);
        return treeView;
    }

    /**
     * Recursively creates a TreeItem for the given script and its children.
     */
    private static TreeItem<PDXScript<?>> buildTreeItem(PDXScript<?> script) {
        TreeItem<PDXScript<?>> item = new TreeItem<>(script);

        // Depending on the concrete type, add children
        if (script instanceof CollectionPDX<?> pdx) {
            // "CollectionPDX" might hold a list of children
            pdx.foreach(childPDX -> {
                item.getChildren().add(buildTreeItem((PDXScript<?>) childPDX));
                return null;
            });
        } else if (script instanceof StructuredPDX pdx) {
            // "StructuredPDX" also has child scripts
            pdx.childScripts().foreach(childPDX -> {
                item.getChildren().add(buildTreeItem(childPDX));
                return null;
            });
        } else if (script instanceof ListPDX<?> pdx) {
            // "ListPDX" has items
            pdx.foreach(childPDX -> {
                item.getChildren().add(buildTreeItem((PDXScript<?>) childPDX));
                return null;
            });
        }
        else if (script instanceof MultiPDX<?> multiPDX) {
            // Exactly how you iterate depends on your API.
            // If `MultiPDX` also has a `foreach` or `getValues()` method, do something like:
            multiPDX.foreach(childPDX -> {
                item.getChildren().add(buildTreeItem((PDXScript<?>) childPDX));
                return null;
            });
        }
        // ... handle other PDX types as needed ...
        // e.g., ReferencePDX, etc.
        // Usually those either contain child scripts or a single value.

        return item;
    }

    /**
     * Produces a display string depending on the type of script.
     */
    private static String generateCellText(PDXScript<?> script) {
        // Customize how each subclass is shown in the tree cell
        if (script instanceof IntPDX intPDX) {
            return intPDX.pdxIdentifier() + ": " + intPDX + "  (IntPDX)";
        } else if (script instanceof DoublePDX doublePDX) {
            return doublePDX.pdxIdentifier() + ": " + doublePDX + "  (DoublePDX)";
        } else if (script instanceof StringPDX stringPDX) {
            return stringPDX.pdxIdentifier() + ": \"" + stringPDX + "\"  (StringPDX)";
        } else if (script instanceof BooleanPDX boolPDX) {
            return boolPDX.pdxIdentifier() + ": " + boolPDX + "  (BooleanPDX)";
        } else if (script instanceof ListPDX listPDX) {
            return listPDX.pdxIdentifier() + "  (ListPDX, size=" + listPDX.size() + ")";
        } else if (script instanceof CollectionPDX<?> pdx) {
            return pdx.pdxIdentifier() + "  (CollectionPDX, children=" + pdx.pdxList().size() + ")";
        } else if (script instanceof StructuredPDX pdx) {
            if (pdx instanceof Localizable localizablePDX) {
                var id = localizablePDX.localizableProperty(Property.NAME);
                if (id.isDefined()) {
                    return pdx.pdxIdentifier() + " <" + id.get() + ">" 
                            + "  (StructuredPDX, children=" + pdx.childScripts().size() + ")";
                } 
            }
            return pdx.pdxIdentifier() + "  (StructuredPDX, children=" + pdx.childScripts().size() + ")";
        } else if (script instanceof MultiPDX<?> multiPDX) {
            return multiPDX.pdxIdentifier() + "  (MultiPDX, children=" + multiPDX.size() + ")";
        }
        // Fallback
        return script.pdxIdentifier() + "  (" + script.getClass().getSimpleName() + ")";
    }
}
