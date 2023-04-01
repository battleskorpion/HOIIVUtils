package ui.menu;

import clausewitz_coding.HOI4Fixes;
import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;

import static clausewitz_coding.HOI4Fixes.DEV_MODE;
import static settings.LocalizerSettings.Settings.MOD_DIRECTORY;
import static settings.LocalizerSettings.Settings.DARK_MODE;

public class MenuSettings extends JFrame {
    private Mainmenu parent_menu;
    private JFrame window;
    private JPanel menuSettingsJPanel;
    private JTextField modDirectoryTextField;
    private JCheckBox devModeCheckBox;
    private JCheckBox darkModeCheckBox;

    public MenuSettings(Mainmenu menu) {
        window = this;
        parent_menu = menu;

        if (HOI4Fixes.settings.get(MOD_DIRECTORY) != null) {
            modDirectoryTextField.setText(HOI4Fixes.settings.get(MOD_DIRECTORY));
        }

        // settings default/current
        darkModeCheckBox.setSelected(DARK_MODE.getMode());

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
        darkModeCheckBox.addActionListener(new ActionListener() {
            /**
             * Invoked when an action occurs.
             *
             * @param e the event to be processed
             */
            @Override
            public void actionPerformed(ActionEvent e) {
                if (DARK_MODE.getMode() != darkModeCheckBox.isSelected()) {
                    DARK_MODE.setMode(darkModeCheckBox.isSelected());
                    if (DARK_MODE.getMode()) {
                        EventQueue.invokeLater(() -> {
                            try {
                                UIManager.setLookAndFeel(new FlatDarkLaf());
                            } catch (UnsupportedLookAndFeelException ex) {
                                System.err.println("Failed to initialize look and feel");
                                throw new RuntimeException(ex);
                            }
                            SwingUtilities.updateComponentTreeUI(window);
                            SwingUtilities.updateComponentTreeUI(parent_menu);
                        });
                    } else {
                        EventQueue.invokeLater(() -> {
                            try {
                                UIManager.setLookAndFeel(new FlatLightLaf());
                            } catch (UnsupportedLookAndFeelException ex) {
                                System.err.println("Failed to initialize look and feel");
                                throw new RuntimeException(ex);
                            }
                            SwingUtilities.updateComponentTreeUI(window);
                            SwingUtilities.updateComponentTreeUI(parent_menu);
                        });
                    }
                }
            }
        });
    }
}
