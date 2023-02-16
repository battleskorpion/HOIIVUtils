package hoi4_coding.ui.focus_localization;

import hoi4_coding.country.CountryTag;
import hoi4_coding.focus.FocusTree;
import hoi4_coding.focus.FocusTrees;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
    private ArrayList<JList<CountryTag>> treeLists;
    private JButton localizeButton;

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

        treeLists = new ArrayList<>();
        treeLists.add(localizedTreeList);
        treeLists.add(partialLocalizedTreeList);
        treeLists.add(unlocalizedTreeList);

        // action listeners
        localizeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                localizeSelectedFocuses();
            }
        });

        setContentPane(FocusLoqReqJPanel);
        setSize(700, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
    }

    public static void main(String[] args) {
        JFrame window = new FocusLoqReqWindow();
        window.setVisible(true);
    }

    private void createUIComponents() {
        // NOTE: place custom component creation code here
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

    private void localizeSelectedFocuses() {
//        JList<CountryTag> selectedList = null;
        if (localizedTreeList.getSelectedIndex() >= 0) {
            JOptionPane.showMessageDialog(this, "Tree already localized.");
        }
        if (partialLocalizedTreeList.getSelectedIndex() >= 0) {

        }
        if (unlocalizedTreeList.getSelectedIndex() >= 0) {
            ArrayList<FocusTree> focusTrees = new ArrayList<>();
            unlocalizedTreeList.getSelectedValuesList().forEach(tag -> {
                focusTrees.add(FocusTrees.get(tag));
            });
            UnlocalizedFocusWindow unlocalizedFocusWindow = new UnlocalizedFocusWindow(focusTrees);
            unlocalizedFocusWindow.setVisible(true);
        }
    }
}
