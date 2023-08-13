package ui.clausewitz_gfx;

import javax.swing.*;

public class InterfaceFileListWindow extends JFrame {
    private JTable interfaceFileListJPanel;
    private JPanel interfaceFIleListJPanel;

    public InterfaceFileListWindow() {
        super("Interface file list");

        /* window */
        setContentPane(TreeBuilderJPanel);
        setSize(700, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        pack();


    }
}
