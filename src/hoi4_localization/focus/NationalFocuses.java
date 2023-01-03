package hoi4_localization.focus;

import hoi4_localization.HOI4Fixes;
import hoi4_localization.province.CountryTag;

import java.util.ArrayList;
import java.util.HashMap;

public class NationalFocuses extends HOI4Fixes {
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

    public static ArrayList<FocusTree> unlocalizedFocuses() {
        ArrayList<FocusTree> unlocalizedFocuses = new ArrayList<>();

        for (FocusTree tree : list()) {
            if (tree.locFile() == null) {
                unlocalizedFocuses.add(tree);
            }
        }

        return unlocalizedFocuses;
    }
}
