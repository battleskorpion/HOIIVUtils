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
        if (script instanceof IntPDX pdx) {
            return pdx.pdxIdentifier() + ": " + pdx + "  (IntPDX)"
                    + (pdx.node().isDefined() ? "  index: " + pdx.getNode().get().start(): "");
        } else if (script instanceof DoublePDX pdx) {
            return pdx.pdxIdentifier() + ": " + pdx + "  (DoublePDX)"
                    + (pdx.node().isDefined() ? "  index: " + pdx.getNode().get().start(): "");
        } else if (script instanceof StringPDX pdx) {
            return pdx.pdxIdentifier() + ": \"" + pdx + "\"  (StringPDX)" 
                    + (pdx.node().isDefined() ? "  index: " + pdx.getNode().get().start(): "");
        } else if (script instanceof BooleanPDX pdx) {
            return pdx.pdxIdentifier() + ": " + pdx + "  (BooleanPDX)"
                    + (pdx.node().isDefined() ? "  index: " + pdx.getNode().get().start(): "");
        } else if (script instanceof ListPDX pdx) {
            return pdx.pdxIdentifier() + "  (ListPDX, size=" + pdx.size() + ")";
        } else if (script instanceof CollectionPDX<?> pdx) {
            return pdx.pdxIdentifier() + "  (CollectionPDX, children=" + pdx.pdxList().size() + ")"
                    + (pdx.node().isDefined() ? "  index: " + pdx.getNode().get().start(): "");
        } else if (script instanceof StructuredPDX pdx) {
            if (pdx instanceof Localizable localizablePDX) {
                var id = localizablePDX.localizableProperty(Property.NAME);
                if (id.isDefined()) {
                    return pdx.pdxIdentifier() + " <" + id.get() + ">" 
                            + "  (StructuredPDX, children=" + pdx.childScripts().size() + ")"
                            + (pdx.node().isDefined() ? "  index: " + pdx.getNode().get().start(): "");
                } 
            }
            return pdx.pdxIdentifier() + "  (StructuredPDX, children=" + pdx.childScripts().size() + ")";
        } else if (script instanceof MultiPDX<?> multiPDX) {
            return multiPDX.pdxIdentifier() + "  (MultiPDX, children=" + multiPDX.size() + ")";
        }
        // Fallback
        return script.pdxIdentifier() + "  (" + script.getClass().getSimpleName() + ")";
    }

    /**
     * Searches the given TreeView for the first node whose text contains the searchText.
     * If found, expands all parents, selects the node, and scrolls to it.
     */
    public static void searchAndSelect(TreeView<PDXScript<?>> treeView, String searchText) {
        if (searchText == null || searchText.isBlank()) return;

        // Convert to lower case for case-insensitive matching
        searchText = searchText.toLowerCase();

        TreeItem<PDXScript<?>> rootItem = treeView.getRoot();
        if (rootItem == null) return;

        // Do a DFS to find the first matching node
        TreeItem<PDXScript<?>> foundItem = findInTree(rootItem, searchText);
        if (foundItem != null) {
            // Expand all parent nodes so the found item is visible
            expandAllParents(foundItem);

            // Select it
            treeView.getSelectionModel().select(foundItem);

            // Scroll to it (optional)
            int index = treeView.getRow(foundItem);
            if (index >= 0) {
                treeView.scrollTo(index);
            }
        }
    }

    /**
     * Recursively searches the tree (DFS) for a node whose text matches searchText.
     * Returns the first match found or null if none.
     */
    private static TreeItem<PDXScript<?>> findInTree(TreeItem<PDXScript<?>> currentItem, String searchText) {
        if (currentItem == null) return null;

        // Check if this node’s text contains the search string
        PDXScript<?> script = currentItem.getValue();
        if (script != null) {
            String nodeText = generateCellText(script).toLowerCase();
            if (nodeText.contains(searchText)) {
                return currentItem;
            }
        }

        // Otherwise, search children
        for (TreeItem<PDXScript<?>> child : currentItem.getChildren()) {
            TreeItem<PDXScript<?>> match = findInTree(child, searchText);
            if (match != null) {
                return match;
            }
        }

        return null; // no match found in this branch
    }

    /**
     * Expands all parent items so that the given item is visible.
     */
    private static void expandAllParents(TreeItem<PDXScript<?>> item) {
        TreeItem<PDXScript<?>> parent = item.getParent();
        while (parent != null) {
            parent.setExpanded(true);
            parent = parent.getParent();
        }
    }
}
