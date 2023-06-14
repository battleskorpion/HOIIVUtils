package ui.map;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class RiverGenWindow extends JFrame {
    private JPanel riverGenJPanel;
    private JPanel RivermapGenButtonsJPanel;
    private JPanel RivermapGenMapJPanel;
    private JRadioButton randomGenerationRadioButton;
    private JRadioButton useSourceRadioButton;
    private JCheckBox keepExistingRiversCheckBox;
    private JPanel RivermapGenTypeOptionButtonsJPanel;
    private JTextField genSourceTextPanel;
    private JPanel genRiverColorDisplayJPanel;
    private JPanel genUseSourceOptionsJPanel;
    private RiverMapViewport riverGenWindow$RiverMapViewport1;

    public RiverGenWindow() {
        super("River Generation");

        /* visibility */
        genUseSourceOptionsJPanel.setVisible(false);

        setContentPane(riverGenJPanel);
        setSize(700, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        pack();

        /* action listeners */
        randomGenerationRadioButton.addActionListener(new ActionListener() {
            /**
             * Invoked when an action occurs.
             *
             * @param e the event to be processed
             */
            @Override
            public void actionPerformed(ActionEvent e) {
                if (randomGenerationRadioButton.isSelected() && useSourceRadioButton.isSelected()) {
                    disableUseSource();
                }
                enableRandomGeneration();
            }
        });
        useSourceRadioButton.addActionListener(new ActionListener() {
            /**
             * Invoked when an action occurs.
             *
             * @param e the event to be processed
             */
            @Override
            public void actionPerformed(ActionEvent e) {
                if (randomGenerationRadioButton.isSelected() && useSourceRadioButton.isSelected()) {
                    disableRandomGeneration();
                }
                enableUseSource();
            }
        });
    }

    private void enableUseSource() {
        genUseSourceOptionsJPanel.setVisible(true);
        repaint();      // repaint after toggling source text panel
    }

    private void enableRandomGeneration() {
    }

    private void disableRandomGeneration() {
        randomGenerationRadioButton.setSelected(false);
    }

    private void disableUseSource() {
        useSourceRadioButton.setSelected(false);
        genUseSourceOptionsJPanel.setVisible(false);
        repaint();      // repaint after toggling source text panel
    }

    public class RiverMapViewport extends JPanel {
        //JPanel riverMapJPanel;

        public RiverMapViewport() {
            JLabel label = new JLabel("label");
            label.setPreferredSize(new Dimension(1000, 1000));
            JScrollPane scrollPane = new JScrollPane(label);
            //
            scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
            scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

            scrollPane.setViewportBorder(new LineBorder(Color.RED));
//            scrollPane.getViewport().add(//, null);
            add(scrollPane, BorderLayout.CENTER);
            setSize(400, 300);
//            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        }
    }

}
