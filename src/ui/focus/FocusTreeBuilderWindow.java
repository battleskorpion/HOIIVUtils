package ui.focus;

import clausewitz_coding.HOI4Fixes;
import clausewitz_coding.focus.FocusTree;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;

public class FocusTreeBuilderWindow extends JFrame {
    private JPanel TreeBuilderJPanel;
    private FocusTreeViewport focusTreeViewport;

    public FocusTreeBuilderWindow() {

        /* window */
        setContentPane(TreeBuilderJPanel);
        setSize(700, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        pack();

        /* draw focus tree */
        focusTreeViewport.repaint();   // have to draw after window stuff
        focusTreeViewport.setVisible(true);
        focusTreeViewport.setViewPosition(new Point(-256, 0));
    }

    public void createUIComponents() throws IOException {
        focusTreeViewport = new FocusTreeViewport(new FocusTree(new File(HOI4Fixes.hoi4_dir_name + HOI4Fixes.focus_folder + "//massachusetts.txt")));

        //focusTreeViewport.setViewSize(new Dimension(1000, 1000));
    }
}
