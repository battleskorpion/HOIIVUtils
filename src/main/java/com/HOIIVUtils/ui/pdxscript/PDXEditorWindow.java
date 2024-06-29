package com.HOIIVUtils.ui.pdxscript;

import com.HOIIVUtils.clauzewitz.script.AbstractPDX;
import com.HOIIVUtils.clauzewitz.script.PDXScript;
import com.HOIIVUtils.ui.HOIIVUtilsWindow;
import com.HOIIVUtils.ui.javafx.PDXEditorPane;
import javafx.fxml.FXML;
import javafx.scene.layout.AnchorPane;

public class PDXEditorWindow extends HOIIVUtilsWindow {
    @FXML
    AnchorPane rootAnchorPane;// = new AnchorPane();
    @FXML
    private PDXEditorPane editorPane;
    private AbstractPDX<?> pdxScript;

    public PDXEditorWindow() {
        setFxmlResource("PDXEditorWindow.fxml");
        setTitle("PDX Editor");
    }

    /**
     * This constructor is used internally by javafx.
     * Use {@link #PDXEditorWindow()} to create a new instance.
     * Then call {@link #open(Object...)} to set the properties.
     *
     * @param pdxScript
     */
    @SuppressWarnings("unused")
    public PDXEditorWindow(AbstractPDX<?> pdxScript) {
        this.pdxScript = pdxScript;
    }

    @FXML
    void initialize() {
        editorPane = new PDXEditorPane(pdxScript);
        rootAnchorPane.getChildren().add(editorPane);
        AnchorPane.setTopAnchor(editorPane, 30.0);
        AnchorPane.setBottomAnchor(editorPane, 0.0);
        AnchorPane.setLeftAnchor(editorPane, 0.0);
        AnchorPane.setRightAnchor(editorPane, 0.0);
    }
}
