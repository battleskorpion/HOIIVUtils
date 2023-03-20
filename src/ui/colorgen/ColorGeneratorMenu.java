package ui.colorgen;

import mapgen.colorgen.ColorGenerator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class ColorGeneratorMenu extends JFrame {
    private JPanel colorGeneratorJPanel;
    private JSlider redMinSlider;
    private JSlider redMaxSlider;
    private JSlider greenMinSlider;
    private JSlider greenMaxSlider;
    private JSlider blueMinSlider;
    private JSlider blueMaxSlider;
    private JButton generateButton;
    private JTextField numColorsTextField;
    private JLabel minGreenAmtLabel;
    private JLabel maxGreenAmtLabel;
    private JLabel minRedAmtLabel;
    private JLabel maxRedAmtLabel;
    private JLabel minBlueAmtLabel;
    private JLabel maxBlueAmtLabel;

    public ColorGeneratorMenu() {
        super("Color Generator");

        /* vars */
        JSlider[] sliders = {redMinSlider, redMaxSlider, greenMinSlider, greenMaxSlider, blueMinSlider, blueMaxSlider};
        JLabel[] sliderAmtLabels = {minRedAmtLabel, maxRedAmtLabel, minGreenAmtLabel, maxGreenAmtLabel, minBlueAmtLabel, maxBlueAmtLabel};

        /* color generator */
        ColorGenerator colorGenerator = new ColorGenerator();

        setContentPane(colorGeneratorJPanel);
        setSize(700, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        pack();

        /* action listeners */
        generateButton.addActionListener(new ActionListener() {
            /**
             * Invoked when an action occurs.
             *
             * @param e the event to be processed
             */
            @Override
            public void actionPerformed(ActionEvent e) {
                colorGenerator.generateColors(getNumColorsGenerate());
            }
        });
        for (int i = 0; i < sliders.length; i++) {
            int finalI = i;
            sliders[i].addFocusListener(new FocusAdapter() {
                /**
                 * Invoked when a component loses the keyboard focus.
                 *
                 * @param e
                 */
                @Override
                public void focusLost(FocusEvent e) {
                    super.focusLost(e);

                    int value = sliders[finalI].getValue();
                    sliderAmtLabels[finalI].setText(Integer.toString(value));
                }
            });
        }

    }

    private int getNumColorsGenerate() {
        int numColors;
        try {
            numColors = Integer.parseInt(numColorsTextField.getText());
        }
        catch (Exception exc) {
            numColors = 0;
            System.err.println(exc.getMessage());
            System.err.println("\t" + "in " + this);
        }
        return numColors;
    }

}
