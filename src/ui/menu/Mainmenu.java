package ui.menu;

import clausewitz_coding.HOI4Fixes;
import clausewitz_coding.focus.FixFocus;
import clausewitz_coding.focus.localization.FocusLocReqFinder;
import clausewitz_coding.state.State;
import clausewitz_coding.idea.FixIdea;
import ui.buildings.BuildingsByCountryWindow;
import ui.colorgen.ColorGeneratorMenu;
import ui.focus_localization.FocusLoqReqWindow;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import static settings.LocalizerSettings.Settings.MOD_DIRECTORY;

public class Mainmenu extends JFrame {
    private Mainmenu menu;
    private JButton fixFocusLocalizationButton;
    private JButton findFocusesWithoutLocalizationButton;
    private JButton findIdeaLocalizationButton;
    private JButton viewBuildingsButton;
    private JPanel mainmenuJPanel;
    private JButton settingsButton;
    private JButton generateProvinceColorsButton;
    private JButton statisticsButton;

    public Mainmenu() {
        menu = this;

        /* statistics button */
        toggleStatisticsButton();

        setContentPane(mainmenuJPanel);
        setSize(700, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();

        /* action listeners */
        fixFocusLocalizationButton.addActionListener(new ActionListener() {
            /**
             * Invoked when an action occurs.
             *
             * @param e the event to be processed
             */
            @Override
            public void actionPerformed(ActionEvent e) {
                File focus_file;
                File loc_file;

                /* focus file */
                {
                    JFileChooser j = new JFileChooser(HOI4Fixes.settings.get(MOD_DIRECTORY) + HOI4Fixes.focus_folder);
                    j.setFileSelectionMode(JFileChooser.FILES_ONLY);
                    j.setDialogTitle("Select Focus File");
                    int opt = j.showOpenDialog(null);
                    focus_file = j.getSelectedFile();
                }

                /* loc file */
                {
                    JFileChooser j = new JFileChooser(HOI4Fixes.settings.get(MOD_DIRECTORY) + HOI4Fixes.localization_eng_folder);
                    j.setFileSelectionMode(JFileChooser.FILES_ONLY);
                    j.setDialogTitle("Select Localization File");
                    int opt = j.showOpenDialog(null);
                    loc_file = j.getSelectedFile();
                }

                try {
                    FixFocus.addFocusLoc(focus_file, loc_file);
                } catch (IOException exc) {
                    throw new RuntimeException(exc);
                }
            }
        });

        findFocusesWithoutLocalizationButton.addActionListener(new ActionListener() {
            /**
             * Invoked when an action occurs.
             *
             * @param e the event to be processed
             */
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    FocusLocReqFinder.findLocReqFocuses(new File(HOI4Fixes.settings.get(MOD_DIRECTORY)));
                } catch (IOException exc) {
                    exc.printStackTrace();
                    throw new RuntimeException(exc);
                }

                // create window here displaying focus trees missing loc file,
                // and focus trees partially localized
                FocusLoqReqWindow window = new FocusLoqReqWindow();
                window.setVisible(true);
            }
        });
        findIdeaLocalizationButton.addActionListener(new ActionListener() {
            /**
             * Invoked when an action occurs.
             *
             * @param e the event to be processed
             */
            @Override
            public void actionPerformed(ActionEvent e) {
                File idea_file;
                File loc_file;

                /* focus file */
                {
                    JFileChooser j = new JFileChooser(new File(HOI4Fixes.settings.get(MOD_DIRECTORY)));
                    j.setFileSelectionMode(JFileChooser.FILES_ONLY);
                    j.setDialogTitle("Select Idea File");
                    Integer opt = j.showOpenDialog(null);
                    idea_file = j.getSelectedFile();
                }

                /* loc file */
                {
                    JFileChooser j = new JFileChooser(HOI4Fixes.settings.get(MOD_DIRECTORY) + HOI4Fixes.localization_eng_folder);
                    j.setFileSelectionMode(JFileChooser.FILES_ONLY);
                    j.setDialogTitle("Select Localization File");
                    Integer opt = j.showOpenDialog(null);
                    loc_file = j.getSelectedFile();
                }

                try {
                    FixIdea.addIdeaLoc(idea_file, loc_file);
                } catch (IOException exc) {
                    exc.printStackTrace();
                    throw new RuntimeException(exc);
                }
            }
        });
        viewBuildingsButton.addActionListener(new ActionListener() {
            /**
             * Invoked when an action occurs.
             *
             * @param e the event to be processed
             */
            @Override
            public void actionPerformed(ActionEvent e) {
                State.readStates();

                BuildingsByCountryWindow window = new BuildingsByCountryWindow();
                window.setVisible(true);
            }
        });
        settingsButton.addActionListener(new ActionListener() {
            /**
             * Invoked when an action occurs.
             *
             * @param e the event to be processed
             */
            @Override
            public void actionPerformed(ActionEvent e) {
                MenuSettings menuSettings = new MenuSettings(menu);

                menuSettings.setVisible(true);
            }
        });

        generateProvinceColorsButton.addActionListener(new ActionListener() {
            /**
             * Invoked when an action occurs.
             *
             * @param e the event to be processed
             */
            @Override
            public void actionPerformed(ActionEvent e) {
                ColorGeneratorMenu colorGenMenu = new ColorGeneratorMenu();
                colorGenMenu.setVisible(true);
            }
        });
        statisticsButton.addActionListener(new ActionListener() {
            /**
             * Invoked when an action occurs.
             *
             * @param e the event to be processed
             */
            @Override
            public void actionPerformed(ActionEvent e) {
                DevStatistics devStatistics = new DevStatistics();
                devStatistics.setVisible(true);
            }
        });
    }

    void toggleStatisticsButton() {
        if (!HOI4Fixes.DEV_MODE) {
            statisticsButton.setVisible(false);
        } else {
            statisticsButton.setVisible(true);
        }
    }
}
