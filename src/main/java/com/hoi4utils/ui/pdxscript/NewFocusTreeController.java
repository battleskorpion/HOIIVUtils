package com.hoi4utils.ui.pdxscript;

import com.hoi4utils.hoi4.common.country_tags.CountryTag;
import com.hoi4utils.hoi4.common.country_tags.CountryTag$;
import com.hoi4utils.hoi4.common.national_focus.FocusTree;
import com.hoi4utils.main.HOIIVUtils;
import com.hoi4utils.main.HOIIVUtilsConfig;
import com.hoi4utils.ui.javafx.application.HOIIVUtilsAbstractController;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import scala.jdk.javaapi.CollectionConverters;

import javax.swing.*;
import java.io.File;
import java.util.function.Consumer;

@Deprecated
public class NewFocusTreeController extends HOIIVUtilsAbstractController {

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

    public NewFocusTreeController() {
        setFxmlFile("NewFocusTree.fxml");
        setTitle("New Focus Tree");
    }

    /**
     * This constructor is used internally by javafx.
     * Use {@link #NewFocusTreeController()} to create a new instance.
     * Then call {@link #open(Object...)} to set the properties.
     *
     * @param onCreate
     */
    @SuppressWarnings("unused")
    public NewFocusTreeController(Consumer<FocusTree> onCreate) {
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

        File focusFile = new File(HOIIVUtilsConfig.get("mod.path") + "/common/national_focus/" + id + "_" + "temp_HOIIVUtils"+ ".txt");
		logger.error("sorry, must update to scala!!!!!"); // TODO
		//        FocusTree focusTreeFile = new FocusTree(null);
//        focusTreeFile.setID(id);
//        focusTreeFile.setCountryTag(countryTag);
//        focusTreeFile.setFile(focusFile);
//        if (onCreate != null) {
//            onCreate.accept(focusTreeFile);
//        }
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
