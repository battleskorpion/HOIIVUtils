package ui.clausewitz_gfx;

import clausewitz_coding.HOI4Fixes;
import clausewitz_coding.focus.Focus;
import clausewitz_coding.focus.FocusTree;
import clausewitz_coding.focus.FocusTrees;
import clausewitz_coding.gfx.Interface;
import clausewitz_coding.gfx.SpriteType;
import ddsreader.DDSReader;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;

public class GFXWindow extends JFrame {
    private JTable GFXTable;
    private JPanel GFXWindowJPanel;
    private JButton countUsesButton;
    private DefaultTableModel GFXTableModel;

    private HashMap<SpriteType, Integer> instancesHashMap;

    public GFXWindow() {
        super("Interface/GFX Details");

        // table model
        GFXTableModel = new DefaultTableModel() {
            /*
            gfx image
            name
            texturefile
            instances (num uses)
            uses details
             */
            @Override
            public int getRowCount() {
                return Interface.numGFX();
            }

            @Override
            public int getColumnCount() {
                return 5;
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public Class getColumnClass(int column) {
                switch (column) {
                    case 0:
                        return BufferedImage.class;
                    case 3:
                        return Integer.class;
                    default:
                        return String.class;
                }
            }
        };
        String[] columns = {"GFX", "Name", "Texturefile", "Instances", "Details"};
        GFXTableModel.setColumnIdentifiers(columns);
        GFXTable.setModel(GFXTableModel);

        // cell rendering
        GFXTable.setRowHeight(100);
        GFXTable.setDefaultRenderer(BufferedImage.class, new BufferedImageTableCellRenderer());

        // row sorter
        GFXTable.setAutoCreateRowSorter(true);

        /* action listeners */

        // data
        refreshGFXTable();

        /* window init */
        setContentPane(GFXWindowJPanel);
        setSize(700, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        pack();

        countUsesButton.addActionListener(new ActionListener() {
            /**
             * Invoked when an action occurs.
             *
             * @param e the event to be processed
             */
            @Override
            public void actionPerformed(ActionEvent e) {
                instancesHashMap = new HashMap<>();

                for (FocusTree focusTree : FocusTrees.list()) {
                    for (Focus focus : focusTree.focuses()) {
                        String icon = focus.icon();

                        if (icon != null) {
                            SpriteType gfx = Interface.getSpriteType(icon);

                            // add or increment instances of gfx
                            instancesHashMap.compute(gfx, (k, v) -> (v == null) ? 1 : v + 1);
                            //instancesHashMap.put(gfx, 100);
                        }
                    }
                }

                refreshGFXTable();
            }
        });
    }

    private void refreshGFXTable() {
        // table data update
        SpriteType[] GFXList = Interface.listGFX();

        GFXTableModel.getDataVector().removeAllElements();
        GFXTableModel.setRowCount(GFXList.length);
        GFXTableModel.setColumnCount(5);
        GFXTableModel.fireTableDataChanged();

        for (int i = 0; i < GFXList.length; i++) {
            BufferedImage ddsImage = getDDSImage(GFXList[i].getGFX());
            //SpriteTypeGFXPanel gfxPanel = new SpriteTypeGFXPanel(ddsImage);

            SpriteType GFX = GFXList[i];
            int instances;
            if (instancesHashMap == null) {
                instances = 0;
            } else {
                instances = instancesHashMap.getOrDefault(GFX, 0);
            }
            String details = "";

            // data
            GFXTableModel.setValueAt(ddsImage, i, 0);
            GFXTableModel.setValueAt(GFX.getName(), i, 1);
            GFXTableModel.setValueAt(GFX.getTexturefile(), i,2);
            GFXTableModel.setValueAt(instances, i,3);
            GFXTableModel.setValueAt(details, i, 4);
        }
    }

    private BufferedImage getDDSImage(String filename) {
        BufferedImage ddsImage;

        if (filename == null) {
            System.err.println("GFX filename null in " + this);
            return null;
        }

        /* dds binary data buffer */
        /* https://github.com/npedotnet/DDSReader */
        try {
            FileInputStream fis;
            File file = new File(filename);
            if (!file.exists()) {
                System.err.println("GFX file " + file + " does not exist");
                fis = new FileInputStream(HOI4Fixes.hoi4_dir_name + "\\gfx\\interface\\goals\\focus_ally_cuba.dds");
            } else {
                fis = new FileInputStream(file.getPath());
            }
            byte[] buffer = new byte[fis.available()];
            fis.read(buffer);
            fis.close();
            int[] ddspixels = DDSReader.read(buffer, DDSReader.ARGB, 0);
            int ddswidth = DDSReader.getWidth(buffer);
            int ddsheight = DDSReader.getHeight(buffer);

            if (ddswidth < 0 || ddsheight < 0) {
                System.out.println("DDS read error of " + filename);
                return null;
            }
            if (ddswidth > 1000000 || ddsheight > 1000000) {
                System.out.println("DDS read error of " + filename + ", dimensions too large.");
                return null;
            }
            ddsImage = new BufferedImage(ddswidth, ddsheight, BufferedImage.TYPE_INT_ARGB);
            ddsImage.setRGB(0, 0, ddswidth, ddsheight, ddspixels, 0, ddswidth);
        } catch (IOException | NegativeArraySizeException exc) {
            exc.printStackTrace();
            return null;
        } catch (NullPointerException exc) {
            System.err.println("DDS Read error of " + filename + ", pixels array size may be 0.");
            return null;
        }

        return ddsImage;
    }

//    public class SpriteTypeGFXPanel extends JPanel implements Scrollable {
//        JScrollPane scrollPane;
//        BufferedImage gfxImage;
//
//        public SpriteTypeGFXPanel(BufferedImage gfxImage) {
//            super();
//
//            /* scrollPane */
//            scrollPane = new JScrollPane(this);
//            scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
//            scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
//            scrollPane.setPreferredSize(new Dimension(100, 100));
//            scrollPane.setViewportBorder(new LineBorder(Color.RED));
//
//            this.gfxImage = gfxImage;
//        }
//
//        @Override
//        public Dimension getPreferredSize() {
//            int width = 100;
//            return new Dimension(width, 100);
//        }
//
//        public Dimension getMinimumSize() {
//            return new Dimension(10, 10);
//        }
//
//        /**
//         * draws each focus as well as related graphics
//         * @param g the <code>Graphics</code> object to protect
//         */
//        @Override
//        protected void paintComponent(Graphics g) {
//            super.paintComponent(g);
//
//            if (gfxImage == null) {
//                System.err.println("GFX image null in " + this);
//                return;
//            }
//
//            int width = getWidth();
//            int height = getHeight();
//            Graphics2D g2d = (Graphics2D) g.create();
//            g2d.setColor(Color.DARK_GRAY);
//            g2d.drawRect(0, 0, width, height);
//
//            /* dds */
//            g2d.drawRect(0, 0, width, height);
//            g2d.drawImage(gfxImage, 0, 0, null);
//
//            g2d.dispose();
//        }
//
//        /**
//         * Returns the preferred size of the viewport for a view component.
//         * For example, the preferred size of a <code>JList</code> component
//         * is the size required to accommodate all of the cells in its list.
//         * However, the value of <code>preferredScrollableViewportSize</code>
//         * is the size required for <code>JList.getVisibleRowCount</code> rows.
//         * A component without any properties that would affect the viewport
//         * size should just return <code>getPreferredSize</code> here.
//         *
//         * @return the preferredSize of a <code>JViewport</code> whose view
//         * is this <code>Scrollable</code>
//         * @see JViewport#getPreferredSize
//         */
//        @Override
//        public Dimension getPreferredScrollableViewportSize() {
//            return null;
//        }
//
//        /**
//         * Components that display logical rows or columns should compute
//         * the scroll increment that will completely expose one new row
//         * or column, depending on the value of orientation.  Ideally,
//         * components should handle a partially exposed row or column by
//         * returning the distance required to completely expose the item.
//         * <p>
//         * Scrolling containers, like JScrollPane, will use this method
//         * each time the user requests a unit scroll.
//         *
//         * @param visibleRect The view area visible within the viewport
//         * @param orientation Either SwingConstants.VERTICAL or SwingConstants.HORIZONTAL.
//         * @param direction   Less than zero to scroll up/left, greater than zero for down/right.
//         * @return The "unit" increment for scrolling in the specified direction.
//         * This value should always be positive.
//         * @see JScrollBar#setUnitIncrement
//         */
//        @Override
//        public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
//            return 0;
//        }
//
//        /**
//         * Components that display logical rows or columns should compute
//         * the scroll increment that will completely expose one block
//         * of rows or columns, depending on the value of orientation.
//         * <p>
//         * Scrolling containers, like JScrollPane, will use this method
//         * each time the user requests a block scroll.
//         *
//         * @param visibleRect The view area visible within the viewport
//         * @param orientation Either SwingConstants.VERTICAL or SwingConstants.HORIZONTAL.
//         * @param direction   Less than zero to scroll up/left, greater than zero for down/right.
//         * @return The "block" increment for scrolling in the specified direction.
//         * This value should always be positive.
//         * @see JScrollBar#setBlockIncrement
//         */
//        @Override
//        public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
//            return 0;
//        }
//
//        /**
//         * Return true if a viewport should always force the width of this
//         * <code>Scrollable</code> to match the width of the viewport.
//         * For example a normal
//         * text view that supported line wrapping would return true here, since it
//         * would be undesirable for wrapped lines to disappear beyond the right
//         * edge of the viewport.  Note that returning true for a Scrollable
//         * whose ancestor is a JScrollPane effectively disables horizontal
//         * scrolling.
//         * <p>
//         * Scrolling containers, like JViewport, will use this method each
//         * time they are validated.
//         *
//         * @return True if a viewport should force the Scrollables width to match its own.
//         */
//        @Override
//        public boolean getScrollableTracksViewportWidth() {
//            return false;
//        }
//
//        /**
//         * Return true if a viewport should always force the height of this
//         * Scrollable to match the height of the viewport.  For example a
//         * columnar text view that flowed text in left to right columns
//         * could effectively disable vertical scrolling by returning
//         * true here.
//         * <p>
//         * Scrolling containers, like JViewport, will use this method each
//         * time they are validated.
//         *
//         * @return True if a viewport should force the Scrollables height to match its own.
//         */
//        @Override
//        public boolean getScrollableTracksViewportHeight() {
//            return false;
//        }
//    }

    /**
     * from <a href="https://stackoverflow.com/questions/14793396/rendering-bufferedimage-in-jtable-cell">...</a>
     */
    public class BufferedImageTableCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (value instanceof BufferedImage) {
                setIcon(new ImageIcon((BufferedImage)value));
                setText(null);
            } else {
                setText("Bad image");
            }
            return this;
        }
    }
}
