package ui.menu;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MenuWindow extends Application {
    private MenuWindow menu;
//    private JButton fixFocusLocalizationButton;
//    private JButton findFocusesWithoutLocalizationButton;
//    private JButton ideaLocalizationButton;
//    private JButton viewBuildingsButton;
//    private JPanel mainmenuJPanel;
//    private JButton settingsButton;
//    private JButton generateProvinceColorsButton;
//    private JButton statisticsButton;
//    private JButton focusTreeBuilderButton;
//    private JButton customTooltipLocalizationButton;
//    private JButton focusTreeStrengthButton;
//    private JButton riverGenerationButton;
//    private JButton GFXButton;

    public MenuWindow() {
        menu = this;
    }

    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("Mainmenu.fxml"));
        stage.setTitle("Hello World");
        stage.setScene((new Scene(root, 600, 400)));
        stage.show();
    }
}
