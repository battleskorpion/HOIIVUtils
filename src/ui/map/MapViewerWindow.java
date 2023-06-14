package ui.map;

import javax.swing.*;

public class MapViewerWindow extends JFrame {
    private JPanel mapViewerJPanel;

    public MapViewerWindow() {



        /* window */
        setContentPane(mapViewerJPanel);
        setSize(700, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
    }
}
