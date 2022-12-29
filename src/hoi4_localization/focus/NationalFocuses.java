package hoi4_localization.focus;

import hoi4_localization.HOI4Fixes;
import hoi4_localization.province.CountryTag;

import java.util.HashMap;

public class NationalFocuses extends HOI4Fixes {
    private static HashMap<CountryTag, FocusTree> focuses;

    public static HashMap<CountryTag, FocusTree> add(CountryTag tag, FocusTree focusTree) {
        focuses.put(tag, focusTree);
        return focuses;
    }

    public static FocusTree[] list() {
        return focuses.values().toArray(new FocusTree[0]);
    }

    public static FocusTree get(CountryTag tag) { return focuses.get(tag); }
}
