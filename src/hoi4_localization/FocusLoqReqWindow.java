package hoi4_localization;

import hoi4_localization.focus.FocusTree;
import hoi4_localization.focus.NationalFocuses;

import javax.swing.*;
import java.util.ArrayList;

public class FocusLoqReqWindow extends JFrame {
    private JPanel FocusLoqReqJPanel;
    private JLabel focusloqreqlabel;
    private JLabel localizedFocusTreesLabel;
    private JLabel partiallyLocalizedFocusTreesLabel;
    private JLabel unlocalizedFocusTreesLabel;

    DefaultListModel localizedTreeListModel;
    private JList localizedTreeList;

    DefaultListModel partialLocalizedTreeListModel;
    private JList partialLocalizedTreeList;

    DefaultListModel unlocalizedTreeListModel;
    private JList unlocalizedTreeList;

    public FocusLoqReqWindow() {
        setContentPane(FocusLoqReqJPanel);
        setTitle("focus loq req");

        setSize(700, 500);
        addLists(NationalFocuses.unlocalizedFocusTrees(), NationalFocuses.partiallyLocalizedFocusTrees(),
                NationalFocuses.localizedFocusTrees());
        setVisible(true);
    }

    public static void main(String[] args) {
        FocusLoqReqWindow window = new FocusLoqReqWindow();
    }

    private void addLists(ArrayList<FocusTree> unlocalizedFocusTrees, ArrayList<FocusTree> partialLocalizedFocusTrees,
                          ArrayList<FocusTree> localizedFocusTrees) {
        int i = 0;
        for (FocusTree tree : unlocalizedFocusTrees) {
            unlocalizedTreeListModel.add(i, tree.country());
//            System.out.println("test");
            i++;
        }
        i = 0;
        for (FocusTree tree : partialLocalizedFocusTrees) {
            partialLocalizedTreeListModel.add(i, tree.country());
            i++;
        }
        i = 0;
        for (FocusTree tree : localizedFocusTrees) {
            localizedTreeListModel.add(i, tree.country());
            i++;
        }
        revalidate();
        repaint();
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
        localizedTreeListModel = new DefaultListModel();
        partialLocalizedTreeListModel = new DefaultListModel();
        unlocalizedTreeListModel = new DefaultListModel();
        localizedTreeList = new JList(localizedTreeListModel);
        partialLocalizedTreeList = new JList(partialLocalizedTreeListModel);
        unlocalizedTreeList = new JList(unlocalizedTreeListModel);
    }
}
