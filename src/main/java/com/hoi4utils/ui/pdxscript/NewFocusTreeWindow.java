package com.hoi4utils.ui.pdxscript;

import com.hoi4utils.clausewitz.HOIIVUtils;
import com.hoi4utils.clausewitz.data.country.CountryTag;
import com.hoi4utils.clausewitz.data.country.CountryTag$;
import com.hoi4utils.clausewitz.data.focus.FocusTree;
import com.hoi4utils.ui.HOIIVUtilsWindow;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import scala.jdk.javaapi.CollectionConverters;

import javax.swing.*;
import java.io.File;
import java.util.function.Consumer;

public class NewFocusTreeWindow extends HOIIVUtilsWindow {

    @FXML
    AnchorPane rootAnchorPane;
    @FXML
    TextField idTextField;
    @FXML
    ComboBox<CountryTag> countryTagComboBox;
    @FXML
    Button cancelButton;
    @FXML
    Button createFocusTreeButton;

    private Consumer<FocusTree> onCreate = null;

    public NewFocusTreeWindow() {
        setFxmlResource("NewFocusTreeWindow.fxml");
        setTitle("New Focus Tree");
    }

    /**
     * This constructor is used internally by javafx.
     * Use {@link #NewFocusTreeWindow()} to create a new instance.
     * Then call {@link #open(Object...)} to set the properties.
     *
     * @param onCreate
     */
    @SuppressWarnings("unused")
    public NewFocusTreeWindow(Consumer<FocusTree> onCreate) {
        this();
        setOnCreateConsumerAction(onCreate);
    }

    @FXML
    void initialize() {
        countryTagComboBox.getItems().addAll(CollectionConverters.asJava(CountryTag$.MODULE$.toList()));
    }

    @FXML
    void onCreate() {
        String id = idTextField.getText();
        CountryTag countryTag = countryTagComboBox.getValue();
        if (id.isEmpty() || countryTag == null) {
            JOptionPane.showMessageDialog(null, "Please fill out all fields", "Error",
                    JOptionPane.ERROR_MESSAGE);
        }

        File focusFile = new File(HOIIVUtils.get("mod.path") + "/common/national_focus/" + id + "_" + "temp_HOIIVUtils"+ ".txt");
        FocusTree focusTree = new FocusTree();
        focusTree.setID(id);
        focusTree.setCountryTag(countryTag);
        focusTree.setFile(focusFile);
        if (onCreate != null) {
            onCreate.accept(focusTree);
        }
        closeWindow(createFocusTreeButton);
    }

    @FXML
    void onCancel() {
        closeWindow(cancelButton);
    }

    public void setOnCreateConsumerAction(Consumer<FocusTree> onCreate) {
       this.onCreate = onCreate;
       System.out.println(this.onCreate == null);
    }
}
