package com.HOIIVUtils.ui.javafx;

import com.github.difflib.DiffUtils;
import com.github.difflib.patch.AbstractDelta;
import com.github.difflib.patch.Chunk;
import com.github.difflib.patch.Patch;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.poi.ss.formula.functions.Delta;
import org.fxmisc.richtext.InlineCssTextArea;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

/**
 * DiffViewPane
 * <p>
 */
public class DiffViewPane extends AnchorPane {
    private final InlineCssTextArea leftTextArea;
    private final InlineCssTextArea rightTextArea;
    private final String leftTitle;
    private final String rightTitle;
    SplitPane splitPane;
    Collection<String> leftData;
    Collection<String> rightData;

    public DiffViewPane(String leftTitle, String rightTitle) {
        super();
        this.leftTitle = leftTitle;
        this.rightTitle = rightTitle;
        splitPane = new SplitPane();
        this.getChildren().add(splitPane);
        // anchor split pane
        AnchorPane.setTopAnchor(splitPane, 0.0);
        AnchorPane.setBottomAnchor(splitPane, 0.0);
        AnchorPane.setLeftAnchor(splitPane, 0.0);
        AnchorPane.setRightAnchor(splitPane, 0.0);

        // split pane's anchor panes
        AnchorPane leftPane = new AnchorPane();
        AnchorPane rightPane = new AnchorPane();
        splitPane.getItems().addAll(leftPane, rightPane);

        // anchor pane text areas
        leftTextArea = new InlineCssTextArea();
        rightTextArea = new InlineCssTextArea();
        leftPane.getChildren().add(leftTextArea);
        rightPane.getChildren().add(rightTextArea);
        // make the text areas use the .text-area css style
        leftTextArea.getStyleClass().add("text-area");
        rightTextArea.getStyleClass().add("text-area");

        // anchor text areas
        AnchorPane.setTopAnchor(leftTextArea, 0.0);
        AnchorPane.setBottomAnchor(leftTextArea, 0.0);
        AnchorPane.setLeftAnchor(leftTextArea, 0.0);
        AnchorPane.setRightAnchor(leftTextArea, 0.0);
        AnchorPane.setTopAnchor(rightTextArea, 0.0);
        AnchorPane.setBottomAnchor(rightTextArea, 0.0);
        AnchorPane.setLeftAnchor(rightTextArea, 0.0);
        AnchorPane.setRightAnchor(rightTextArea, 0.0);
        // text area properties
        leftTextArea.setEditable(false);
        rightTextArea.setEditable(false);
        leftTextArea.setWrapText(false);
        rightTextArea.setWrapText(false);
        // merge left-right scrolling
        leftTextArea.estimatedScrollYProperty().bindBidirectional(rightTextArea.estimatedScrollYProperty());
    }

    public void setData(Collection<String> leftData, Collection<String> rightData) {
        this.leftData = leftData;
        this.rightData = rightData;
        displayDiff();
    }

    private void displayDiff() {
        List<String> leftLines = (leftData != null) ? List.copyOf(leftData) : Collections.emptyList();
        List<String> rightLines = (rightData != null) ? List.copyOf(rightData) : Collections.emptyList();
        Patch<String> patch = DiffUtils.diff(leftLines, rightLines);

        StyleSpansBuilder<String> leftSpansBuilder = new StyleSpansBuilder<>();
        StyleSpansBuilder<String> rightSpansBuilder = new StyleSpansBuilder<>();

        int leftIndex = 0;
        int rightIndex = 0;
        for (var delta : patch.getDeltas()) {
            Chunk<String> leftChunk = delta.getSource();
            Chunk<String> rightChunk = delta.getTarget();

            // Add unchanged lines before the current delta
            while (leftIndex < leftChunk.getPosition()) {
                leftSpansBuilder.add("-fx-font-family: monospace", leftLines.get(leftIndex).length() + 1);
                rightSpansBuilder.add("-fx-font-family: monospace", rightLines.get(rightIndex).length() + 1);
                leftIndex++;
                rightIndex++;
            }

            switch (delta.getType()) {
                case DELETE:
                    for (String line : leftChunk.getLines()) {
                        leftSpansBuilder.add("-fx-font-family: monospace", line.length() + 1);
                        rightSpansBuilder.add("-fx-font-family: monospace", 0);
                        leftIndex++;
                    }
                    break;
                case INSERT:
                    for (String line : rightChunk.getLines()) {
                        leftSpansBuilder.add("-fx-font-family: monospace", 0);
                        rightSpansBuilder.add("-fx-font-family: monospace", line.length() + 1);
                        rightIndex++;
                    }
                    break;
                case CHANGE:
                    for (String line : leftChunk.getLines()) {
                        leftSpansBuilder.add("-fx-font-family: monospace", line.length() + 1);
                        leftIndex++;
                    }
                    for (String line : rightChunk.getLines()) {
                        rightSpansBuilder.add("-fx-font-family: monospace", line.length() + 1);
                        rightIndex++;
                    }
                    break;
            }
        }

        // Add remaining unchanged lines
        while (leftIndex < leftLines.size()) {
            leftSpansBuilder.add("-fx-font-family: monospace", leftLines.get(leftIndex).length() + 1);
            leftIndex++;
        }
        while (rightIndex < rightLines.size()) {
            rightSpansBuilder.add("-fx-font-family: monospace", rightLines.get(rightIndex).length() + 1);
            rightIndex++;
        }

        // Append text (title first though so its not lost)
        if (leftTitle != null) {
            leftTextArea.replaceText(leftTitle + "\n");
            leftTextArea.setStyle(0, "-fx-font-size: 16px;");
        }
        if (rightTitle != null) {
            rightTextArea.replaceText(rightTitle + "\n");
            rightTextArea.setStyle(0, "-fx-font-size: 16px;");
        }
        int leftTitleLength = leftTextArea.getLength();
        int rightTitleLength = rightTextArea.getLength();
        leftTextArea.appendText(String.join("\n", leftLines) + '\n');
        rightTextArea.appendText(String.join("\n", rightLines) + '\n');

        leftTextArea.setStyleSpans(leftTitleLength - 1, leftSpansBuilder.create());
        rightTextArea.setStyleSpans(rightTitleLength - 1, rightSpansBuilder.create());
    }

}