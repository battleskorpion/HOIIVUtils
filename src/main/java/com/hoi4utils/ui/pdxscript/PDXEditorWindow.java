package com.hoi4utils.ui.pdxscript;

import com.hoi4utils.clausewitz.script.PDXScript;
import com.hoi4utils.ui.HOIIVUtilsWindow;
import com.hoi4utils.ui.javafx.PDXEditorPane;
import javafx.fxml.FXML;
import javafx.scene.layout.AnchorPane;

public class PDXEditorWindow extends HOIIVUtilsWindow {
    @FXML
    AnchorPane rootAnchorPane;// = new AnchorPane();
    @FXML
    private PDXEditorPane editorPane;
    private PDXScript<?> pdxScript;

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
    public PDXEditorWindow(PDXScript<?> pdxScript) {
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
