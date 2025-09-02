package com.hoi4utils.ui.pdxscript;

import com.hoi4utils.script.CollectionPDX;
import com.hoi4utils.ui.HOIIVUtilsAbstractController;
import javafx.fxml.FXML;
import javafx.scene.layout.AnchorPane;

public class CollectionPDXNewPDXController extends HOIIVUtilsAbstractController {
    private CollectionPDX<?> pdxScript; 

    @FXML
    AnchorPane rootAnchorPane;

    public CollectionPDXNewPDXController() {
        setFxmlResource("CollectionPDXNewPDXEditor.fxml");
        setTitle("CollectionPDX new pdx Editor");
    }

    /**
     * This constructor is used internally by javafx.
     * Use {@link #CollectionPDXNewPDXController()} to create a new instance.
     * Then call {@link #open(Object...)} to set the properties.
     *
     * @param pdxScript
     */
    @SuppressWarnings("unused")
    public CollectionPDXNewPDXController(CollectionPDX<?> pdxScript) {
        this.pdxScript = pdxScript;
    }

    @FXML
    void initialize() {
        CollectionPDXSearchPane newPDXEditorPane = new CollectionPDXSearchPane(pdxScript);
        rootAnchorPane.getChildren().add(newPDXEditorPane);

        AnchorPane.setTopAnchor(newPDXEditorPane, 30.0);
        AnchorPane.setBottomAnchor(newPDXEditorPane, 0.0);
        AnchorPane.setLeftAnchor(newPDXEditorPane, 0.0);
        AnchorPane.setRightAnchor(newPDXEditorPane, 0.0);
    }

}
