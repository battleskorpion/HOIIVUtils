package ui.menu;

import java.io.File;

import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;
import settings.HOIIVUtilsProperties;
import settings.HOIIVUtilsProperties.*;
import clausewitz_coding.HOI4Fixes;

public class SettingsWindow extends Application {
    Stage primaryStage;
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
            HOI4Fixes.firstTimeSetup = false;
            HOI4Fixes.settings = new HOIIVUtilsProperties();

            HOI4Fixes.decideScreen(primaryStage);
        } else {
            /* first-time setup */
            HOI4Fixes.firstTimeSetup = true;
        }
    }

    public void launchSettingsWindow(String... var0) {
        super.launch(var0);
    }

    public void open() {primaryStage.show(); }
}