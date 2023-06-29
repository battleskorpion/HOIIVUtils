package ui.focus;

import clausewitz_coding.HOI4Fixes;
import clausewitz_coding.focus.Focus;
import clausewitz_coding.focus.FocusTree;
import ddsreader.DDSReader;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
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

        private static final int X_SCALE = 120;
        private static final int Y_SCALE = 120;
        private FocusTree focusTree;
        private JScrollPane scrollPane;

        public FocusTreeViewport(FocusTree focusTree) {
            /* init */
            this.focusTree = focusTree;

//            JLabel label = new JLabel("text");
//            label.setPreferredSize(new Dimension(2000, 2000));
//            /* scrollPane */
//            scrollPane = new JScrollPane(label);
//            scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
//            scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
//            scrollPane.setViewportBorder(new LineBorder(Color.RED));
//            scrollPane.setPreferredSize(new Dimension(800, 800));
//            FocusPanel focusPanel = new FocusPanel();
//            scrollPane.getViewport().add(focusPanel, null);
//
//            add(scrollPane, BorderLayout.CENTER);

            FocusPanel focusPanel = new FocusPanel();
            focusPanel.setPreferredSize(new Dimension(2000, 2000));

            /* scrollPane */
            scrollPane = new JScrollPane(focusPanel);
            scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
            scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
            scrollPane.setViewportBorder(new LineBorder(Color.RED));
            scrollPane.setPreferredSize(new Dimension(800, 800));

            add(scrollPane, BorderLayout.CENTER);

            drawFocuses();
        }

        public void drawFocuses() {
            if (focusTree == null) {
                System.err.println("Error in " + this.getClass() + ":" + " focus tree null.");
                return;
            }

//            repaint();        //todo unnecessary~~
        }

        public class FocusPanel extends JPanel implements Scrollable {

            public FocusPanel() {
//                this.setSize(new Dimension(1000, 1000));
                this.setVisible(true);
            }

//            public void paint(Graphics g) {
//                Graphics2D g2D = (Graphics2D) g;
//
//                g2D.setColor(Color.WHITE);
//                System.out.println("test");
//                for (Focus focus : focusTree.focuses()) {
//                    drawFocus(g, focus);
////                    System.out.println(focus);
//                }
//            }

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

            @Override
            public Dimension getPreferredSize() {
                int width = 100 * X_SCALE;
                return new Dimension(width, 50 * Y_SCALE);
            }

            @Override
            public Dimension getMinimumSize() {
                return new Dimension(10 * X_SCALE, 10 * Y_SCALE);
            }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                if (focusTree != null) {
                    int width = getWidth();
                    int height = getHeight();
                    Graphics2D g2d = (Graphics2D) g.create();
                    g2d.setColor(Color.DARK_GRAY);
                    g2d.drawRect(0, 0, width, height);
                    int xPos = 0;
                    for (Focus focus : focusTree.focuses()) {
                        g2d.setColor(Color.WHITE);
                        int x1 = X_SCALE * focus.absoluteX();
                        int y1 = Y_SCALE * focus.absoluteY();

                        g.drawRect(x1, y1, X_SCALE, Y_SCALE);
//                            BufferedImage image = ImageIO.read(new File(focus.icon()));
                        g.drawImage(focus.getDDSImage(), x1, y1, null);

                        String name;
                        if (focus.locName() == null) {
                            name = focus.id();
                        } else {
                            name = focus.locName();
                        }
                        g.drawString(name, x1, y1);

                        xPos += X_SCALE;
                    }
                    g2d.dispose();
                }
            }

            /**
             * Returns the preferred size of the viewport for a view component.
             * For example, the preferred size of a <code>JList</code> component
             * is the size required to accommodate all of the cells in its list.
             * However, the value of <code>preferredScrollableViewportSize</code>
             * is the size required for <code>JList.getVisibleRowCount</code> rows.
             * A component without any properties that would affect the viewport
             * size should just return <code>getPreferredSize</code> here.
             *
             * @return the preferredSize of a <code>JViewport</code> whose view
             * is this <code>Scrollable</code>
             * @see JViewport#getPreferredSize
             */
            @Override
            public Dimension getPreferredScrollableViewportSize() {
                return new Dimension(800, 800);
            }

            /**
             * Components that display logical rows or columns should compute
             * the scroll increment that will completely expose one new row
             * or column, depending on the value of orientation.  Ideally,
             * components should handle a partially exposed row or column by
             * returning the distance required to completely expose the item.
             * <p>
             * Scrolling containers, like JScrollPane, will use this method
             * each time the user requests a unit scroll.
             *
             * @param visibleRect The view area visible within the viewport
             * @param orientation Either SwingConstants.VERTICAL or SwingConstants.HORIZONTAL.
             * @param direction   Less than zero to scroll up/left, greater than zero for down/right.
             * @return The "unit" increment for scrolling in the specified direction.
             * This value should always be positive.
             * @see JScrollBar#setUnitIncrement
             */
            @Override
            public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
                return 1;
            }

            /**
             * Components that display logical rows or columns should compute
             * the scroll increment that will completely expose one block
             * of rows or columns, depending on the value of orientation.
             * <p>
             * Scrolling containers, like JScrollPane, will use this method
             * each time the user requests a block scroll.
             *
             * @param visibleRect The view area visible within the viewport
             * @param orientation Either SwingConstants.VERTICAL or SwingConstants.HORIZONTAL.
             * @param direction   Less than zero to scroll up/left, greater than zero for down/right.
             * @return The "block" increment for scrolling in the specified direction.
             * This value should always be positive.
             * @see JScrollBar#setBlockIncrement
             */
            @Override
            public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
                return 1;
            }

            /**
             * Return true if a viewport should always force the width of this
             * <code>Scrollable</code> to match the width of the viewport.
             * For example a normal
             * text view that supported line wrapping would return true here, since it
             * would be undesirable for wrapped lines to disappear beyond the right
             * edge of the viewport.  Note that returning true for a Scrollable
             * whose ancestor is a JScrollPane effectively disables horizontal
             * scrolling.
             * <p>
             * Scrolling containers, like JViewport, will use this method each
             * time they are validated.
             *
             * @return True if a viewport should force the Scrollables width to match its own.
             */
            @Override
            public boolean getScrollableTracksViewportWidth() {
                return getPreferredSize().width
                        <= getParent().getSize().width;
            }

            /**
             * Return true if a viewport should always force the height of this
             * Scrollable to match the height of the viewport.  For example a
             * columnar text view that flowed text in left to right columns
             * could effectively disable vertical scrolling by returning
             * true here.
             * <p>
             * Scrolling containers, like JViewport, will use this method each
             * time they are validated.
             *
             * @return True if a viewport should force the Scrollables height to match its own.
             */
            @Override
            public boolean getScrollableTracksViewportHeight() {
                return getPreferredSize().height
                        <= getParent().getSize().height;
            }
        }

    }
}
