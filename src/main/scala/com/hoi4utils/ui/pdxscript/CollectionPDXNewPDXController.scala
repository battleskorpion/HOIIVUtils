package com.hoi4utils.ui.pdxscript

import com.hoi4utils.script.CollectionPDX
import com.hoi4utils.ui.HOIIVUtilsAbstractController
import javafx.fxml.FXML
import javafx.scene.layout.AnchorPane

class CollectionPDXNewPDXController(pdxScript: CollectionPDX[_]) extends HOIIVUtilsAbstractController {
  setFxmlResource("CollectionPDXNewPDXEditor.fxml")
  setTitle("CollectionPDX new pdx Editor")
  @FXML val rootAnchorPane: AnchorPane = null


  def initialize(): Unit = {
    val newPDXEditorPane = new CollectionPDXNewPDXPane(pdxScript)
    rootAnchorPane.getChildren.add(newPDXEditorPane)
    AnchorPane.setTopAnchor(newPDXEditorPane, 30.0)
    AnchorPane.setBottomAnchor(newPDXEditorPane, 0.0)
    AnchorPane.setLeftAnchor(newPDXEditorPane, 0.0)
    AnchorPane.setRightAnchor(newPDXEditorPane, 0.0)
  }
}