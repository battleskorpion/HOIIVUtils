package com.HOIIVUtils.ui.javafx;

import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
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
    SplitPane splitPane;
    Collection<String> leftData;
    Collection<String> rightData;

    public DiffViewPane(String leftTitle, String rightTitle) {
        super();
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
        // set data (append text hereafter)
        if (leftTitle != null) {
            leftTextArea.appendText(leftTitle + "\n");
            //leftTextArea.setStyle(0, "-fx-font-size: 16px; -fx-fill: blue;");
        }
        if (rightTitle != null) {
            rightTextArea.appendText(rightTitle + "\n");
            //rightTextArea.setStyle(0, "-fx-font-size: 16px; -fx-fill: blue;");
        }

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
    }


}