package ui.main_menu;

import hoi4utils.HOIIVUtils;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MenuWindow {
    Stage primaryStage;

    public void open(){
        try {
            if (primaryStage != null) {
                primaryStage.show();
            }
            else {
                Parent root = FXMLLoader.load(getClass().getResource("MenuWindow.fxml"));
                
                Stage primaryStage = new Stage();

                primaryStage.setTitle("HOIIVUtils Menu");

                primaryStage.setScene((new Scene(root)));

                HOIIVUtils.decideScreen(primaryStage);
                
                primaryStage.show();
            }
        }
        catch (Exception exception) {
            HOIIVUtils.openError(exception);
        }
    }
}
