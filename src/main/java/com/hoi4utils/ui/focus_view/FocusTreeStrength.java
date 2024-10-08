package com.hoi4utils.ui.focus_view;

import com.hoi4utils.clausewitz.HOIIVFile;
import com.hoi4utils.SettingsManager;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;

import static com.hoi4utils.Settings.MOD_PATH;

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
