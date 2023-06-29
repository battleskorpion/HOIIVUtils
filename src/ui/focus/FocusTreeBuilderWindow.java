package ui.focus;

import clausewitz_coding.HOI4Fixes;
import clausewitz_coding.focus.Focus;
import clausewitz_coding.focus.FocusTree;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.io.File;
import java.io.IOException;

public class FocusTreeBuilderWindow extends JFrame {
    private JPanel TreeBuilderJPanel;
    private FocusTreeViewport focusTreeBuilderWindow$FocusTreeViewport1;

    public FocusTreeBuilderWindow() {
        super ("Focus Tree");

        /* window */
        setContentPane(TreeBuilderJPanel);
        setSize(700, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        pack();

        /* draw focus tree */
        focusTreeBuilderWindow$FocusTreeViewport1.repaint();   // have to draw after window stuff
        focusTreeBuilderWindow$FocusTreeViewport1.setVisible(true);
//        focusTreeViewport.setViewPosition(new Point(-256, 0));
    }

    public void createUIComponents() {
        try {
            focusTreeBuilderWindow$FocusTreeViewport1 = new FocusTreeViewport(new FocusTree(new File(HOI4Fixes.hoi4_dir_name + HOI4Fixes.focus_folder + "//massachusetts.txt")));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public class FocusTreeViewport extends JPanel {
        //JPanel riverMapJPanel;

        private static final int X_SCALE = 80;
        private static final int Y_SCALE = 80;
        private FocusTree focusTree;
        private JScrollPane scrollPane;

        public FocusTreeViewport(FocusTree focusTree) {
            /* init */
            this.focusTree = focusTree;

            JLabel label = new JLabel("text");
            label.setPreferredSize(new Dimension(1000, 1000));
            /* scrollPane */
            scrollPane = new JScrollPane(label);
            scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
            scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
            scrollPane.setViewportBorder(new LineBorder(Color.RED));
            scrollPane.setPreferredSize(new Dimension(800, 800));
            FocusPanel focusPanel = new FocusPanel();
            scrollPane.getViewport().add(focusPanel);

            add(scrollPane, BorderLayout.CENTER);
            //setSize(400, 300);
//            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            drawFocuses();
        }

        public void drawFocuses() {
            if (focusTree == null) {
                System.err.println("Error in " + this.getClass() + ":" + " focus tree null.");
                return;
            }

//            repaint();        //todo unnecessary~~
        }

        public class FocusPanel extends JPanel {
            public FocusPanel() {
//                this.setSize(new Dimension(1000, 1000));
                this.setVisible(true);
            }

            public void paint(Graphics g) {
                Graphics2D g2D = (Graphics2D) g;

                g2D.setColor(Color.WHITE);
                System.out.println("test");
                for (Focus focus : focusTree.focuses()) {
                    drawFocus(g, focus);
//                    System.out.println(focus);
                }
            }

            public void drawFocus(Graphics g, Focus focus) {
                int x1 = X_SCALE * focus.absoluteX();
                int x2 = X_SCALE * focus.absoluteX() + X_SCALE;
                int y1 = Y_SCALE * focus.absoluteY();
                int y2 = Y_SCALE * focus.absoluteY() + Y_SCALE;

//                System.out.println("x1: " + x1 + ", " + "y1: " + y1);
                g.drawRect(x1, y1, X_SCALE, Y_SCALE);
                String name;
                if (focus.locName() == null) {
                    name = focus.id();
                } else {
                    name = focus.locName();
                }
                g.drawString(name, x1, y1);
            }

            public void removeFocus(Focus focus) {

            }
        }

    }
}
