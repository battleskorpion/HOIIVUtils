package com.hoi4utils.ui.pdxscript;

import com.hoi4utils.hoi4mod.common.national_focus.Focus;
import com.hoi4utils.script.PDXScript;
import com.hoi4utils.ui.custom_javafx.controller.HOIIVUtilsAbstractController;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;

public class PDXEditorController extends HOIIVUtilsAbstractController {
    private PDXScript<?> pdxScript;
    private Runnable onUpdate = null;

    @FXML
    AnchorPane rootSkorpPane;
    @FXML
    TextField idField;
    @FXML
    TextField iconField;

    public PDXEditorController() {
        setFxmlResource("PDXEditor.fxml");
        setTitle("PDX Editor");
    }

    /**
     * This constructor is used internally by javafx.
     * Use {@link #PDXEditorController()} to create a new instance.
     * Then call {@link #open(Object...)} to set the properties.
     *
     * @param pdxScript the PDXScript to edit
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
     * @param pdxScript the PDXScript to edit
     * @param onUpdate a Runnable that will be called when pdxScript properties are updated
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
        drawEditor();
    }

    private void drawEditor() {
        switch (pdxScript) {
            case Focus pdx:
                idField.setText(pdx.id().getOrElse("NO ID"));

                break;
            default:
        }
    }

    public void handleFocusID() {
        String input = idField.getText();
        Focus pdx = (Focus) pdxScript;
        pdx.setID(input);
        if (input.isEmpty()) {
            onPropertyUpdate();
        } else {
            reloadEditor();
        }
    }

    public void handleFocusIcon() {
        String input = iconField.getText();
        Focus pdx = (Focus) pdxScript;
//        pdx.setIcon(input);
        if (input.isEmpty()) {
            onPropertyUpdate();
        } else {
            reloadEditor();
        }
    }

    private void reloadEditor() {
        drawEditor();
    }

    private void onPropertyUpdate() {
        if (onUpdate != null) {
            onUpdate.run();
        }
    }


}
