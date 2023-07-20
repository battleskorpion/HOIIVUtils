package ui.focus;

import clausewitz_coding.focus.Focus;

import javax.swing.*;
import java.awt.*;
import java.util.Set;

public class FocusTreeDetailsWindow extends JFrame {
    private JLabel focusNameJLabel;
    private JPanel focusTreeDetailsJPanel;
    private JLabel focusIDJLabel;
    private JLabel focusDetailsJLabel;

    Focus focus;

    public FocusTreeDetailsWindow(Focus focus, Point p) {
        super ("Details");

        if (focus == null) {
            System.err.println("Focus null in " + this);
            return;
        }

        /* window */
        setLocation(p);
        setContentPane(focusTreeDetailsJPanel);
        setSize(700, 500);
        setUndecorated(true);
        getRootPane().setWindowDecorationStyle(JRootPane.INFORMATION_DIALOG);
        pack();

        focusNameJLabel.setText(focus.locName());
        focusIDJLabel.setText(focus.id());
        StringBuilder details = new StringBuilder();
        details.append("\n");
//        details.append("Requires one of the following");
        details.append("Completion time: ");
        details.append(focus.completionTime());
        details.append("\n");
        for (Set<Focus> prereqSet : focus.getPrerequisites()) {
            if (prereqSet.size() > 1) {
                details.append("Requires one of the following: \n");
                details.append("- ");
                for (Focus f : prereqSet) {
                    details.append(f.locName());
                    details.append("\n");
                }
            } else {
                details.append("Requires ");
                details.append(prereqSet.toArray()[0]);
                details.append("\n");
            }
        }
        details.append("\n\n");
        details.append("Effect: \n");
        focusDetailsJLabel.setText(details.toString());
    }
}
