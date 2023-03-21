package ui.menu;

import clausewitz_coding.HOI4Fixes;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;

import static clausewitz_coding.HOI4Fixes.DEV_MODE;
import static settings.LocalizerSettings.Settings.MOD_DIRECTORY;

public class MenuSettings extends JFrame {
    private Mainmenu parent_menu;
    private JPanel menuSettingsJPanel;
    private JTextField modDirectoryTextField;
    private JCheckBox devModeCheckBox;

    public MenuSettings(Mainmenu menu) {
        parent_menu = menu;

        if (HOI4Fixes.settings.get(MOD_DIRECTORY) != null) {
            modDirectoryTextField.setText(HOI4Fixes.settings.get(MOD_DIRECTORY));
        }

        setContentPane(menuSettingsJPanel);
        setSize(700, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        pack();

        /* action listeners */
        modDirectoryTextField.addMouseListener(new MouseAdapter() {
            /**
             * {@inheritDoc}
             *
             * @param e
             */
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);

                JFileChooser j = new JFileChooser();
                j.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                j.setDialogTitle("Choose Mod Directory");

                int opt = j.showOpenDialog(null);
                if (opt == JFileChooser.APPROVE_OPTION) {
                    HOI4Fixes.hoi4_dir_name = j.getSelectedFile().getPath();
                } else {
                    return;
                }

                /* directory acquired, now save settings */
                try {
                    HOI4Fixes.settings.saveSettings(MOD_DIRECTORY, HOI4Fixes.hoi4_dir_name);
                } catch (IOException exc) {
                    throw new RuntimeException(exc);
                }

                modDirectoryTextField.setText(HOI4Fixes.settings.get(MOD_DIRECTORY));
            }
        });
        devModeCheckBox.addActionListener(new ActionListener() {
            /**
             * Invoked when an action occurs.
             *
             * @param e the event to be processed
             */
            @Override
            public void actionPerformed(ActionEvent e) {
                if (devModeCheckBox.isSelected()) {
                    DEV_MODE = true;
                } else {
                    DEV_MODE = false;
                }

                parent_menu.toggleStatisticsButton();
            }
        });
    }
}
