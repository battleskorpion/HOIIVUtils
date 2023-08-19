package ui.main_menu;

import hoi4utils.HOIIVUtils;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class StatisticsWindow {

    public void open() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("StatisticsWindow.fxml"));

            Stage stage = new Stage();

            stage.setTitle("HOIIVUtils Statistics");

            stage.setScene((new Scene(root)));

            HOIIVUtils.decideScreen(stage);
            
            stage.show();
        }
        catch (Exception exception) {
            HOIIVUtils.openError(exception);
        }
    }
}
