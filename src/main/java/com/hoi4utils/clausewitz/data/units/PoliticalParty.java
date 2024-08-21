package com.hoi4utils.clausewitz.data.units;

import com.hoi4utils.clausewitz.localization.*;
import org.jetbrains.annotations.NotNull;
import scala.jdk.javaapi.CollectionConverters;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class PoliticalParty implements Localizable {
    String id;

    @Override
    public @NotNull scala.collection.mutable.Map<Property, String> getLocalizableProperties() {
        return CollectionConverters.asScala(Map.of());
    }

    @Override
    public @NotNull scala.collection.Iterable<? extends Localizable> getLocalizableGroup() {
        return CollectionConverters.asScala(List.of(this)); // todo make this the list of all parties probably.
    }
}
