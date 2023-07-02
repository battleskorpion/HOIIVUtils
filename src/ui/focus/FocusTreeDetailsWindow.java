package ui.focus;

import javax.swing.*;

public class FocusTreeDetailsWindow extends JFrame {
    private JLabel countryNameJLabel;
    private JPanel focusTreeDetailsJPanel;

    public FocusTreeDetailsWindow() {
        super ("Details");

        /* window */
        setContentPane(focusTreeDetailsJPanel);
        setSize(700, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        pack();

        
    }
}
