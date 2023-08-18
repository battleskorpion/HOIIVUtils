package ui.menu;

import clausewitz_coding.HOI4Fixes;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MenuWindow {

    private MenuWindow menuWindow;

    public MenuWindow() {
        menuWindow = this;
    }

    public void open() throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("MenuWindow.fxml"));
        Stage stage = new Stage();
        stage.setTitle("Hello World");
        stage.setScene((new Scene(root, 600, 400)));

        HOI4Fixes.decideScreen(stage);
        stage.show();
    }
}
