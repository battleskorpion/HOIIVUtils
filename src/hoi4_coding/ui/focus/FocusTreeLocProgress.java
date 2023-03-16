package hoi4_coding.ui.focus;

import hoi4_coding.focus.FocusTree;

import javax.swing.*;

public class FocusTreeLocProgress extends JFrame {
    private JPanel FocusTreeLocProgressJPanel;
    private JProgressBar progressBarLoc;
    private JLabel focusTreeName;
    private JLabel focusTreeLabel;

    public FocusTreeLocProgress(FocusTree focusTree) {
        focusTreeName.setText(focusTree.toString());
        //progressBarLoc.setIndeterminate();
        progressBarLoc.setMaximum(4);

        setContentPane(FocusTreeLocProgressJPanel);
        setSize(700, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
    }

    public void incrementProgressBar() {
        progressBarLoc.setValue(progressBarLoc.getValue() + 1);
    }
}
