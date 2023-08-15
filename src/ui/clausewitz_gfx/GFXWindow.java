package ui.clausewitz_gfx;

import clausewitz_coding.HOI4Fixes;
import clausewitz_coding.focus.Focus;
import clausewitz_coding.focus.FocusTree;
import clausewitz_coding.focus.FocusTrees;
import clausewitz_coding.gfx.Interface;
import clausewitz_coding.gfx.SpriteType;
import ddsreader.DDSReader;

import javax.swing.*;
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
    private JButton toggleInterfaceFilesButton;
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

        // data
        refreshGFXTable();

        /* window */
        setContentPane(GFXWindowJPanel);
        setSize(700, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        pack();

        /* action listeners */
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
        toggleInterfaceFilesButton.addActionListener(new ActionListener() {
            /**
             * Invoked when an action occurs.
             *
             * @param e the event to be processed
             */
            @Override
            public void actionPerformed(ActionEvent e) {
                InterfaceFileListWindow interfaceFileListWindow = new InterfaceFileListWindow();
                interfaceFileListWindow.setVisible(true);
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

    /**
     * from <a href="https://stackoverflow.com/questions/14793396/rendering-bufferedimage-in-jtable-cell">...</a>
     */
    private class BufferedImageTableCellRenderer extends DefaultTableCellRenderer {
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
