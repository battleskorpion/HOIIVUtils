package ui.focus;

import clausewitz_coding.focus.Focus;
import clausewitz_coding.focus.FocusTree;

import javax.swing.*;
import java.awt.*;

public class FocusTreeViewport extends JViewport {
    protected FocusTree focusTree;
//    private JViewport viewport;
    private static final int xScale = 100;      // focus x,y -> ui x,y
    private static final int yScale = 100;
    private static final int nameOffset = 20;
    // todo - new focus tree
//    public focusTreePanel() {
//        this(new FocusTree());
//    }

    public FocusTreeViewport(FocusTree focusTree) {
        this.focusTree = focusTree;

//        this.setLayout(new ViewportLayout());

        /* view focus tree */
//        viewport = new JViewport();
        FocusTreeView view = new FocusTreeView();
        setView(view);
        //setViewPosition(new Point(0, 0));
        //setViewSize(new Dimension(1000, 1000));
//        this.add(viewport);
        System.out.println("viewport location: " + getLocation() + ", size: " + getSize());
        setBackground(Color.red);
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(Color.black);
        g.drawRect(10, 10, 100, 100);
    }
//    public void draw() {
//        Graphics g = this.getGraphics();    //viewport.getGraphics()
//
//        if (g == null) {
//            System.out.println("Note: Graphics g null, " + this);
//            return;
//        }
//
//        /* graphics settings */
//        g.setColor(Color.BLACK);
//
//        g.drawRect(-1000, -1000, 1000, 1000);
//        /* draw focuses */
//        for (Focus focus : focusTree.focuses()) {
//            int x = focus.x() * xScale;
//            int y = focus.y() * yScale;
//
//            String name = focus.locName();
//            if (name != null) {
//                g.drawString(name, x, y + nameOffset);
//            } else {
//                g.drawString(focus.id() ,x, y + nameOffset);
//            }
//        }
//    }
}
