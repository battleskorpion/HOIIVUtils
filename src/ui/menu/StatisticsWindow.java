package ui.menu;

import clausewitz_coding.HOI4Fixes;
import clausewitz_coding.focus.Focus;
import clausewitz_coding.focus.FocusTree;
import clausewitz_coding.focus.FocusTrees;
import clausewitz_parser.Expression;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.util.ArrayList;

public class StatisticsWindow extends JFrame implements HOI4FixesWindow {
    private JPanel StatisticsJPanel;
    private JTextArea textArea1;
    private JTabbedPane tabbedPane1;
    private JTree focusTreesJTree;

    public StatisticsWindow() {

        textArea1.append("mod dir: " + HOI4Fixes.hoi4_dir_name + "\n");
        textArea1.append("state folder: " + HOI4Fixes.hoi4_dir_name + HOI4Fixes.states_folder + "\n");

        /* window */
        setContentPane(StatisticsJPanel);
        setSize(700, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
    }

    public void repaint() {
        buildJTree();
    }

    private DefaultMutableTreeNode buildJTree() {
        ArrayList<DefaultMutableTreeNode> focusTreesArray = new ArrayList<>();
        DefaultMutableTreeNode focusTreesNode = new DefaultMutableTreeNode("Focus Trees");

        for (FocusTree tree : FocusTrees.list()) {
            ArrayList<DefaultMutableTreeNode> focusesArray = new ArrayList<>();
            DefaultMutableTreeNode focusTreeNode = new DefaultMutableTreeNode(tree.country());

            for (Focus focus : tree.focuses()) {
                ArrayList<DefaultMutableTreeNode> focusExpArray = new ArrayList<>();
                DefaultMutableTreeNode focusNode = new DefaultMutableTreeNode(focus.id());

                for (Expression exp : focus.getFocusExpression().getAll()) {
                    DefaultMutableTreeNode focusExpNode = new DefaultMutableTreeNode(exp.getText());
                    focusExpArray.add(focusExpNode);
                }
                focusesArray.add(focusNode);
            }
            focusTreesArray.add(focusTreeNode);
        }
        return focusTreesNode;
    }

    private void createUIComponents() {
        focusTreesJTree = new JTree(buildJTree());
    }
}
