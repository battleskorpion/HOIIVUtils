package ui.menu;

import clausewitz_coding.HOI4Fixes;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;

import static settings.LocalizerSettings.Settings.MOD_DIRECTORY;

public class MenuSettings extends JFrame {
    private JPanel menuSettingsJPanel;
    private JTextField modDirectoryTextField;

    public MenuSettings() {
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
    }
}
