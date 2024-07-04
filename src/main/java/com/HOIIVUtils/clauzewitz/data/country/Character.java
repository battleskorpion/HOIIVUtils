package com.HOIIVUtils.clauzewitz.data.country;

import com.HOIIVUtils.clauzewitz.localization.Localizable;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class Character implements Localizable {
    String id;

    @Override
    public @NotNull Map<Property, String> getLocalizableProperties() {
        return Map.of(Property.NAME, id);
    }

    @Override
    public @NotNull Collection<? extends Localizable> getLocalizableGroup() {
        return List.of(this); // todo improve
    }
}
