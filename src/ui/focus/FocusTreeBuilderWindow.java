package ui.focus;

import javax.swing.*;

public class FocusTreeBuilderWindow extends JFrame {
    private JPanel TreeBuilderJPanel;

    public FocusTreeBuilderWindow() {

        /* window */
        setContentPane(TreeBuilderJPanel);
        setSize(700, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        pack();
    }
}
