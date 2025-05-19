package com.hoi4utils.ui.pdxscript

import com.hoi4utils.HOIIVUtils
import com.hoi4utils.hoi4.country.CountryTag
import com.hoi4utils.hoi4.focus.FocusTree
import com.hoi4utils.ui.HOIIVUtilsAbstractController
import com.typesafe.scalalogging.LazyLogging
import javafx.fxml.FXML
import javafx.scene.control.{Button, ComboBox, TextField}
import javafx.scene.layout.AnchorPane

import scala.jdk.CollectionConverters._
import javax.swing.JOptionPane
import java.io.File
import java.util.function.Consumer

class NewFocusTreeController(private var onCreateCallback: Consumer[FocusTree]) extends HOIIVUtilsAbstractController with LazyLogging {
  setFxmlResource("NewFocusTree.fxml")
  setTitle("New Focus Tree")
  @FXML val rootAnchorPane: AnchorPane = null
  @FXML val idTextField: TextField = null
  @FXML val countryTagComboBox: ComboBox[CountryTag] = null
  @FXML val cancelButton: Button = null
  @FXML val createFocusTreeButton: Button = null
  setOnCreateConsumerAction(onCreateCallback)

  @FXML def initialize(): Unit = {
    import scala.jdk.CollectionConverters._
    val javaCollection = CountryTag.iterator.toSeq.asJavaCollection
    countryTagComboBox.getItems.addAll(javaCollection)
  }

  @FXML def onCreate(): Unit = {
    val id = idTextField.getText
    val countryTag = countryTagComboBox.getValue
    if (id.isEmpty || countryTag == null) {
      JOptionPane.showMessageDialog(null, "Please fill out all fields", "Error", JOptionPane.ERROR_MESSAGE)
      return
    }
    val focusFile = new File(HOIIVUtils.get("mod.path") + "/common/national_focus/" + id + "_" + "temp_HOIIVUtils" + ".txt")
    val focusTree = new FocusTree
    focusTree.setID(id)
    focusTree.setCountryTag(countryTag)
    focusTree.setFile(focusFile)
    if (onCreateCallback != null) onCreateCallback.accept(focusTree)
    closeWindow(createFocusTreeButton)
  }

  @FXML private[pdxscript] def onCancel(): Unit = {
    closeWindow(cancelButton)
  }

  def setOnCreateConsumerAction(onCreate: Consumer[FocusTree]): Unit = {
    this.onCreateCallback = onCreate
    System.out.println(this.onCreateCallback == null)
  }
}