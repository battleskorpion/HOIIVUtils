package ui.menu;

import clausewitz_coding.HOI4Fixes;
import clausewitz_coding.focus.FixFocus;
import clausewitz_coding.focus.localization.FocusLocReqFinder;
import clausewitz_coding.state.State;
import clausewitz_coding.idea.FixIdea;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import ui.buildings.BuildingsByCountryWindow;
import ui.clausewitz_gfx.GFXWindow;
import ui.colorgen.ColorGeneratorMenu;
import ui.custom_tooltop.CustomTooltipWindow;
import ui.focus.FocusTreeWindow;
import ui.focus.FocusTreeStrength;
import ui.focus_localization.FocusLoqReqWindow;
import ui.map.RiverGenWindow;

import javax.swing.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;

import static settings.LocalizerSettings.Settings.MOD_DIRECTORY;

public class Mainmenu extends Application {
    private Mainmenu menu;
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

    public Mainmenu() {
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
