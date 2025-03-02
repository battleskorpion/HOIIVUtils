package com.hoi4utils.ui.focus_view;

import com.hoi4utils.clausewitz.HOIIVUtils;
import com.hoi4utils.clausewitz.HOIIVFiles;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;


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
					JFileChooser j = new JFileChooser(HOIIVUtils.get("mod.path") + HOIIVFiles.Mod.focus_folder);
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
