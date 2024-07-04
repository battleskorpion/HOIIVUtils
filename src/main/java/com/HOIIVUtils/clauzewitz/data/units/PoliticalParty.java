package com.HOIIVUtils.clauzewitz.data.units;

import com.HOIIVUtils.clauzewitz.localization.Localizable;
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
