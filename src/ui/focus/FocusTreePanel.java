package ui.focus;

import clausewitz_coding.focus.Focus;
import clausewitz_coding.focus.FocusTree;

import javax.swing.*;
import java.awt.*;

public class FocusTreePanel extends JPanel {
    protected FocusTree focusTree;
    private static final int xScale = 100;      // focus x,y -> ui x,y
    private static final int yScale = 100;
    // todo - new focus tree
//    public focusTreePanel() {
//        this(new FocusTree());
//    }

    public FocusTreePanel(FocusTree focusTree) {
        this.focusTree = focusTree;

        /* view focus tree */
        this.setLayout(new ViewportLayout());

        for (Focus focus : focusTree.focuses()) {
            int x = focus.x() * xScale;
            int y = focus.y() * yScale;

            JTextField focusNameField = new JTextField(focus.locName());
            //add
        }
    }
}
