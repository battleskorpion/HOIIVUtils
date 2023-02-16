package hoi4_coding.focus;

import hoi4_coding.HOI4Fixes;
import hoi4_coding.country.CountryTag;
import hoi4_coding.country.CountryTags;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class FocusTrees extends HOI4Fixes {
    private static final HashMap<CountryTag, FocusTree> focuses = new HashMap<>();

    public static HashMap<CountryTag, FocusTree> add(CountryTag tag, FocusTree focusTree) {
        focuses.put(tag, focusTree);
        return focuses;
    }

    public static FocusTree[] list() {
        return focuses.values().toArray(new FocusTree[0]);
    }

    public static FocusTree get(CountryTag tag) { return focuses.get(tag); }
    public static FocusTree getdankwizardisfrench(CountryTag tag) {
        for (FocusTree tree : list()) {
            assert tree.country() != null;
            if (tree.country().equals(tag)) {
                return tree;
            }
        }

        return null;
    }

    public static ArrayList<FocusTree> unlocalizedFocusTrees() {
        ArrayList<FocusTree> focusTrees = new ArrayList<>();

        for (FocusTree tree : list()) {
            if (tree.locFile() == null) {
                focusTrees.add(tree);
            }
        }

        return focusTrees;
    }

    public static ArrayList<FocusTree> partiallyLocalizedFocusTrees() throws IOException {
        ArrayList<FocusTree> focusTrees = new ArrayList<>();

        // todo may be able to do something else in this function -
        // todo all focus trees - localized focus trees - unlocalized focus trees
        for (FocusTree tree : list()) {
            aa:
            if (tree.locFile() != null) {
                Scanner locReader = new Scanner(tree.locFile().getFile());
                ArrayList<String> focuses = tree.list();
                if (focuses == null) {
                    break aa;
                }

                ArrayList<Boolean> localized;
                while (locReader.hasNext()) {
                    String locLine = locReader.nextLine();
                    if (locLine.trim().length() >= 3) {
                        String potentialTag = locLine.trim().substring(0, 3);

                        if (CountryTags.exists(potentialTag)) {

                        }
                    }
                }
            }
        }
        return focusTrees;
    }

    public static ArrayList<FocusTree> localizedFocusTrees() throws IOException {
        ArrayList<FocusTree> focusTrees = new ArrayList<>();

        for (FocusTree tree : list()) {
            aa:
            if (tree.locFile() != null) {
                Scanner locReader = new Scanner(tree.locFile().getFile());
                ArrayList<String> focuses = tree.list();
                if (focuses == null) {
                    break aa;

                }

                ArrayList<Boolean> localized;
                while (locReader.hasNext()) {
                    String locLine = locReader.nextLine();
                    if (locLine.trim().length() >= 3) {
                        String potentialTag = locLine.trim().substring(0, 3);

                        if (CountryTags.exists(potentialTag)) {

                        }
                    }
                }
            }
        }
        return focusTrees;
    }
}
