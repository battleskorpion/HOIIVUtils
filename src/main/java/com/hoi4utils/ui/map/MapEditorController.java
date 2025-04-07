package com.hoi4utils.ui.map;

import com.hoi4utils.ui.HOIIVUtilsAbstractController;
import javafx.fxml.FXML;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MapEditorController extends HOIIVUtilsAbstractController {
    public static final Logger LOGGER = LogManager.getLogger(MapEditorController.class);
    
    public MapEditorController() {
        setFxmlResource("MapEditor.fxml");
        setTitle("Map Editor");
    }
    
    @FXML
    void initialize() {
        
    }
}
