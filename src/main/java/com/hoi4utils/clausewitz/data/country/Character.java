package com.hoi4utils.clausewitz.data.country;

import com.hoi4utils.clausewitz.localization.*;
import scala.collection.Iterable;
import scala.jdk.javaapi.CollectionConverters;

import java.util.List;
import java.util.Map;


public class Character implements Localizable {
    String id;

    @Override
    public scala.collection.mutable.Map<Property, String> getLocalizableProperties() {
        return CollectionConverters.asScala(Map.of(Property.NAME, id));
    }

    @Override
    public Iterable<? extends Localizable> getLocalizableGroup() {
        return CollectionConverters.asScala(List.of(this)); // todo improve
    }
}
