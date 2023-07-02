package ui.focus;

import clausewitz_coding.focus.Focus;

import javax.swing.*;

public class FocusTreeDetailsWindow extends JFrame {
    private JLabel focusNameJLabel;
    private JPanel focusTreeDetailsJPanel;

    Focus focus;

    public FocusTreeDetailsWindow(Focus focus) {
        super ("Details");

        if (focus == null) {
            System.err.println("Focus null in " + this);
            return;
        }

        /* window */
        setContentPane(focusTreeDetailsJPanel);
        setSize(700, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        pack();

        focusNameJLabel.setText(focus.id());
    }
}
