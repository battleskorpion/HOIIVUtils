package com.hoi4utils.clausewitz.data.units;

import com.hoi4utils.clausewitz.localization.Localizable;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class PoliticalParty implements Localizable {
    String id;

    @Override
    public @NotNull Map<Property, String> getLocalizableProperties() {
        return null;
    }

    @Override
    public @NotNull Collection<? extends Localizable> getLocalizableGroup() {
        return List.of(this); // todo make this the list of all parties probably.
    }
}
