package ui.focus;

import hoi4utils.HOIIVUtils;
import settings.HOIIVUtilsProperties;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;

import static settings.HOIIVUtilsProperties.Settings.MOD_PATH;

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
                    JFileChooser j = new JFileChooser(HOIIVUtilsProperties.get(MOD_PATH) + HOIIVUtils.focus_folder);
                    j.setFileSelectionMode(JFileChooser.FILES_ONLY);
                    j.setDialogTitle("Select Focus File");
//                    int opt = j.showOpenDialog(null);
                    focus_file = j.getSelectedFile();
                }
                focustreestrength.setText(focus_file.getPath());
            }
        });
    }

}
