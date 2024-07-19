package com.HOIIVUtils.ui.focus_view;

import com.HOIIVUtils.clauzewitz.HOIIVFile;
import com.HOIIVUtils.SettingsManager;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;

import static com.HOIIVUtils.Settings.MOD_PATH;

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
					JFileChooser j = new JFileChooser(SettingsManager.get(MOD_PATH) + HOIIVFile.mod_focus_folder);
					j.setFileSelectionMode(JFileChooser.FILES_ONLY);
					j.setDialogTitle("Select Focus File");
//					int opt = j.showOpenDialog(null);
					focus_file = j.getSelectedFile();
				}
				focustreestrength.setText(focus_file.getPath());
			}
		});
	}

}
