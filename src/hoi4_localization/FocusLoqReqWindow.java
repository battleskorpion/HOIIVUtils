package hoi4_localization;

import hoi4_localization.country.CountryTag;
import hoi4_localization.focus.FocusTree;
import hoi4_localization.focus.FocusTrees;

import javax.swing.*;
import java.io.IOException;
import java.util.ArrayList;

public class FocusLoqReqWindow extends JFrame {
    private JPanel FocusLoqReqJPanel;
    private JLabel focusloqreqlabel;
    private JLabel localizedFocusTreesLabel;
    private JLabel partiallyLocalizedFocusTreesLabel;
    private JLabel unlocalizedFocusTreesLabel;

    private DefaultListModel<CountryTag> localizedTreeListModel;
    private JList<CountryTag> localizedTreeList;

    private DefaultListModel<CountryTag> partialLocalizedTreeListModel;
    private JList<CountryTag>  partialLocalizedTreeList;

    private DefaultListModel<CountryTag> unlocalizedTreeListModel;
    private JList<CountryTag>  unlocalizedTreeList;

    public FocusLoqReqWindow() {
        super("Focus Localization");        // JFrame

        // texts etc.
        localizedTreeListModel = new DefaultListModel<>();
        partialLocalizedTreeListModel = new DefaultListModel<>();
        unlocalizedTreeListModel = new DefaultListModel<>();
        localizedTreeList.setModel(localizedTreeListModel);
        partialLocalizedTreeList.setModel(partialLocalizedTreeListModel);
        unlocalizedTreeList.setModel(unlocalizedTreeListModel);
        try {
            refreshFocusTreeLists(FocusTrees.unlocalizedFocusTrees(), FocusTrees.partiallyLocalizedFocusTrees(),
                    FocusTrees.localizedFocusTrees());
        } catch (IOException exc) {
            exc.printStackTrace();
            System.exit(-1);
        }

        setContentPane(FocusLoqReqJPanel);
        setSize(700, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
    }

    public static void main(String[] args) {
        JFrame window = new FocusLoqReqWindow();
        window.setVisible(true);
    }

    public void refreshFocusTreeLists(ArrayList<FocusTree> unlocalizedFocusTrees, ArrayList<FocusTree> partialLocalizedFocusTrees,
                                       ArrayList<FocusTree> localizedFocusTrees) {
        unlocalizedTreeListModel.removeAllElements();
        partialLocalizedTreeListModel.removeAllElements();
        localizedTreeListModel.removeAllElements();

        for (FocusTree tree : unlocalizedFocusTrees) {
            unlocalizedTreeListModel.addElement(tree.country());
        }
        for (FocusTree tree : partialLocalizedFocusTrees) {
            partialLocalizedTreeListModel.addElement(tree.country());
        }
        for (FocusTree tree : localizedFocusTrees) {
            localizedTreeListModel.addElement(tree.country());
        }
    }

    private void createUIComponents() {
        // NOTE: place custom component creation code here
    }
}
