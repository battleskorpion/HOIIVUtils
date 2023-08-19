package ui.focus_localization;

import clausewitz_coding.focus.FocusTree;
import clausewitz_coding.localization.FocusLocalizationFile;
import hoi4utils.HOIIVUtils;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

public class FocusLocalizationWindow {

    public FocusLocalizationWindow() {

    }

    public void open(){
        try {
            Parent root = FXMLLoader.load(getClass().getResource("FocusLocalizationWindow.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Focus Localization");
            stage.setScene((new Scene(root, 600, 400)));
            HOIIVUtils.decideScreen(stage);
            stage.show();
        }
        catch (Exception exception) {
            HOIIVUtils.openError(exception);
        }
    }
}
