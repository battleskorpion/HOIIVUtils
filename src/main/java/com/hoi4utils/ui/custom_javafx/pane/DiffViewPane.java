package com.hoi4utils.ui.custom_javafx.pane;

import com.github.difflib.DiffUtils;
import com.github.difflib.patch.AbstractDelta;
import com.github.difflib.patch.Chunk;
import com.github.difflib.patch.Patch;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * DiffViewPane
 * A pane to display differences between two sets of strings.
 */
public class DiffViewPane extends AnchorPane {
    private final ListView<String> leftListView;
    private final ListView<String> rightListView;
    private final String leftTitle;
    private final String rightTitle;
    private Collection<String> leftData;
    private Collection<String> rightData;

    public DiffViewPane(String leftTitle, String rightTitle) {
        this.leftTitle = leftTitle;
        this.rightTitle = rightTitle;

        SplitPane splitPane = new SplitPane();
        this.getChildren().add(splitPane);
        // Anchor split pane to all sides of the DiffViewPane
        AnchorPane.setTopAnchor(splitPane, 0.0);
        AnchorPane.setBottomAnchor(splitPane, 0.0);
        AnchorPane.setLeftAnchor(splitPane, 0.0);
        AnchorPane.setRightAnchor(splitPane, 0.0);

        // Create left and right anchor panes to hold ListViews
        AnchorPane leftPane = new AnchorPane();
        AnchorPane rightPane = new AnchorPane();
        splitPane.getItems().addAll(leftPane, rightPane);

        // Initialize ListViews for displaying diffs
        leftListView = new ListView<>();
        rightListView = new ListView<>();
        leftPane.getChildren().add(leftListView);
        rightPane.getChildren().add(rightListView);

        // Anchor ListViews to their respective panes
        AnchorPane.setTopAnchor(leftListView, 0.0);
        AnchorPane.setBottomAnchor(leftListView, 0.0);
        AnchorPane.setLeftAnchor(leftListView, 0.0);
        AnchorPane.setRightAnchor(leftListView, 0.0);
        AnchorPane.setTopAnchor(rightListView, 0.0);
        AnchorPane.setBottomAnchor(rightListView, 0.0);
        AnchorPane.setLeftAnchor(rightListView, 0.0);
        AnchorPane.setRightAnchor(rightListView, 0.0);

        // list view properties
        leftListView.setEditable(false);
        rightListView.setEditable(false);

        // Bind scrolling of the left and right views
        bindDiffViewScrolling();
    }

    private void bindDiffViewScrolling() {
        // wait until skin is attached to access the scrollbar
        ChangeListener<Skin<?>> leftSkinChangeListener = new ChangeListener<>() {
            @Override
            public void changed(ObservableValue<? extends Skin<?>> observable, Skin<?> oldValue, Skin<?> newValue) {
                leftListView.skinProperty().removeListener(this);
                var leftScrollbar = getVerticalScrollBar(leftListView);
                if (leftScrollbar == null) return;
                leftScrollbar.setOpacity(0);
                var rightScrollbar = getVerticalScrollBar(rightListView);
                if (rightScrollbar == null) {
                    // wait until right scrollbar is available
                    rightListView.skinProperty().addListener((observableValue, oldSkin, newSkin) -> {
                        rightListView.skinProperty().removeListener(this);
                        var rightScrollbar2 = getVerticalScrollBar(rightListView);
                        if (rightScrollbar2 != null) {
                            leftScrollbar.valueProperty().bindBidirectional(rightScrollbar2.valueProperty());
                        }
                    });
                } else {
                    leftScrollbar.valueProperty().bindBidirectional(rightScrollbar.valueProperty());
                }
            }
        };
        leftListView.skinProperty().addListener(leftSkinChangeListener);
    }

    /**
     * Sets the data to be compared and displays the diff.
     *
     * @param originalData  the original data
     * @param revisedData the modified data
     */
    public void setData(Collection<String> originalData, Collection<String> revisedData) {
        this.leftData = originalData;
        this.rightData = revisedData;
        displayDiff();
    }

    /**
     * Display the diff between the left and right data.
     */
    private void displayDiff() {
        List<String> leftLines = (leftData != null) ? List.copyOf(leftData) : Collections.emptyList();
        List<String> rightLines = (rightData != null) ? List.copyOf(rightData) : Collections.emptyList();
        Patch<String> patch = DiffUtils.diff(leftLines, rightLines);

        ObservableList<String> leftItems = FXCollections.observableArrayList();
        ObservableList<String> rightItems = FXCollections.observableArrayList();

        int leftIndex = 0;
        int rightIndex = 0;

        for (AbstractDelta<String> delta : patch.getDeltas()) {
            Chunk<String> leftChunk = delta.getSource();
            Chunk<String> rightChunk = delta.getTarget();

            // Add unchanged lines before the current delta
            while (leftIndex < leftChunk.getPosition()) {
                leftItems.add("  " + leftLines.get(leftIndex));
                rightItems.add("  " + rightLines.get(rightIndex));
                leftIndex++;
                rightIndex++;
            }

            // Handle the different types of changes
            switch (delta.getType()) {
                case DELETE:
                    for (String line : leftChunk.getLines()) {
                        leftItems.add("- " + line);
                        rightItems.add("");
                        leftIndex++;
                    }
                    break;
                case INSERT:
                    for (String line : rightChunk.getLines()) {
                        leftItems.add("");
                        rightItems.add("+ " + line);
                        rightIndex++;
                    }
                    break;
                case CHANGE:
                    int leftIndexDiff = -leftIndex;
                    int rightIndexDiff = -rightIndex;
                    for (String line : leftChunk.getLines()) {
                        leftItems.add("~ " + line);
                        leftIndex++;
                    }
                    for (String line : rightChunk.getLines()) {
                        rightItems.add("~ " + line);
                        rightIndex++;
                    }
                    leftIndexDiff += leftIndex;
                    rightIndexDiff += rightIndex;
                    if (leftIndexDiff > rightIndexDiff) {
                        for (int i = 0; i < leftIndexDiff - rightIndexDiff; i++) {
                            rightItems.add("");
                        }
                    } else if (rightIndexDiff > leftIndexDiff) {
                        for (int i = 0; i < rightIndexDiff - leftIndexDiff; i++) {
                            leftItems.add("");
                        }
                    }
                    break;
            }
        }

        // Add remaining unchanged lines
        while (leftIndex < leftLines.size()) {
            leftItems.add("  " + leftLines.get(leftIndex));
            leftIndex++;
        }
        while (rightIndex < rightLines.size()) {
            rightItems.add("  " + rightLines.get(rightIndex));
            rightIndex++;
        }

        // Prepend titles if available
        if (leftTitle != null) {
            leftItems.add(0, leftTitle);
        }
        if (rightTitle != null) {
            rightItems.add(0, rightTitle);
        }

        // Set items in ListViews
        leftListView.setItems(leftItems);
        rightListView.setItems(rightItems);

        // Use custom cell factory to style items
        leftListView.setCellFactory(lv -> new DiffCell());
        rightListView.setCellFactory(lv -> new DiffCell());
    }

    private ScrollBar getVerticalScrollBar(Node scrollableNode) {
        for (Node node : scrollableNode.lookupAll(".scroll-bar")) {
            if (node instanceof ScrollBar scrollBar) {
                if (scrollBar.getOrientation() == Orientation.VERTICAL) {
                    return scrollBar;
                }
            }
        }
        return null;
    }

    /**
     * Custom ListCell to style lines based on their diff status.
     */
    private static class DiffCell extends ListCell<String> {
        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            /*
            i dont know why. but this has to be here. Putting it inside the if block is no good.
             */
            getStyleClass().remove("diff-insert");
            getStyleClass().remove("diff-delete");
            getStyleClass().remove("diff-change");
            if (empty || item == null) {
                setText(null);
                setStyle(null);
            } else {
                setText(item);
                if (item.startsWith("-")) {
                    //setStyle("-fx-background-color: lightcoral; -fx-font-family: monospace");
                    getStyleClass().add("diff-delete");
                    setStyle("-fx-font-family: monospace");
                } else if (item.startsWith("+")) {
                    //setStyle("-fx-background-color: lightgreen; -fx-font-family: monospace");
                    getStyleClass().add("diff-insert");
                    setStyle("-fx-font-family: monospace");
                } else if (item.startsWith("~")) {
                    getStyleClass().add("diff-change");
                    setStyle("-fx-font-family: monospace");
                } else {
                    setStyle("-fx-font-family: monospace");
                }
            }
        }
    }

}
