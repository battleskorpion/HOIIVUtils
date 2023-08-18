package ui.menu;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import settings.HOIIVUtilsProperties;
import clausewitz_coding.HOI4Fixes;

import java.io.File;

public class SettingsWindow extends Application {
    Stage primaryStage;
    boolean firstTimeSetup;
    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("SettingsWindow.fxml"));
        primaryStage.setTitle("Settings");
        primaryStage.setScene((new Scene(root, 600, 400)));
        primaryStage.show();
        this.primaryStage = primaryStage;

        /* settings */
        String user_docs_path = System.getProperty("user.home") + File.separator + "Documents";
        String hoi4UtilsPropertiesPath = user_docs_path + File.separator + "HOIIVUtils";
        if (new File(hoi4UtilsPropertiesPath).exists()) {
            /* standard setup */
            firstTimeSetup = false;
            HOI4Fixes.settings = new HOIIVUtilsProperties();
        } else {
            /* first-time setup */
            firstTimeSetup = true;
        }
    }

    public void launchSettingsWindow(String... var0) {
        super.launch(var0);
    }

    public void open() {primaryStage.show(); }
}