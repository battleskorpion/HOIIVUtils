package com.hoi4utils.ui.pdxscript;

import com.hoi4utils.clausewitz.script.PDXScript;
import com.hoi4utils.ui.HOIIVUtilsAbstractController;
import javafx.fxml.FXML;
import javafx.scene.layout.AnchorPane;

public class PDXEditorController extends HOIIVUtilsAbstractController {
    private PDXScript<?> pdxScript;
    private Runnable onUpdate = null;

    @FXML
    AnchorPane rootSkorpPane;

    public PDXEditorController() {
        setFxmlResource("PDXEditor.fxml");
        setTitle("PDX Editor");
    }

    /**
     * This constructor is used internally by javafx.
     * Use {@link #PDXEditorController()} to create a new instance.
     * Then call {@link #open(Object...)} to set the properties.
     *
     * @param pdxScript
     */
    @SuppressWarnings("unused")
    public PDXEditorController(PDXScript<?> pdxScript) {
        this.pdxScript = pdxScript;
    }

    /**
     * This constructor is used internally by javafx.
     * Use {@link #PDXEditorController()} to create a new instance.
     * Then call {@link #open(Object...)} to set the properties.
     *
     * @param pdxScript
     */
    @SuppressWarnings("unused")
    public PDXEditorController(PDXScript<?> pdxScript, Runnable onUpdate) {
        this.pdxScript = pdxScript;
        this.onUpdate = onUpdate;
    }

    @FXML
    void initialize() {
        PDXEditorPane editorPane;

        if (onUpdate == null) {
            editorPane = new PDXEditorPane(pdxScript);
        } else {
            editorPane = new PDXEditorPane(pdxScript, onUpdate);
        }
        rootSkorpPane.getChildren().add(editorPane);

        AnchorPane.setTopAnchor(editorPane, 30.0);
        AnchorPane.setBottomAnchor(editorPane, 0.0);
        AnchorPane.setLeftAnchor(editorPane, 0.0);
        AnchorPane.setRightAnchor(editorPane, 0.0);

    }
}
