package ui.menu;

import clausewitz_coding.HOI4Fixes;

import javax.swing.*;

public class DevStatistics extends JFrame {
    private JPanel StatisticsJPanel;
    private JTextArea textArea1;

    public DevStatistics() {
        setContentPane(StatisticsJPanel);
        setSize(700, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();

        textArea1.append("mod dir: " + HOI4Fixes.hoi4_dir_name + "\n");
        textArea1.append("state folder: " + HOI4Fixes.hoi4_dir_name + HOI4Fixes.states_folder + "\n");
    }
}
