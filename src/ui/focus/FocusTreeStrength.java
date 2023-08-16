package ui.focus;

import clausewitz_coding.HOI4Fixes;
import settings.LocalizerSettings;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;

import static settings.LocalizerSettings.Settings.MOD_DIRECTORY;

public class FocusTreeStrength extends JFrame {
    File focus_file;
    private JTextField focustreestrength;

    public FocusTreeStrength() {

        focustreestrength.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                /* focus file */
                {
                    JFileChooser j = new JFileChooser(LocalizerSettings.get(MOD_DIRECTORY) + HOI4Fixes.focus_folder);
                    j.setFileSelectionMode(JFileChooser.FILES_ONLY);
                    j.setDialogTitle("Select Focus File");
                    int opt = j.showOpenDialog(null);
                    focus_file = j.getSelectedFile();
                }
                focustreestrength.setText(focus_file.getPath());
            }
        });
    }

}
