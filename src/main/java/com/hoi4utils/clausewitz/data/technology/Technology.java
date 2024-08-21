package com.hoi4utils.clausewitz.data.technology;

import com.hoi4utils.clausewitz.code.ClausewitzDate;
import com.hoi4utils.clausewitz.localization.*;
import org.jetbrains.annotations.NotNull;
import scala.jdk.javaapi.CollectionConverters;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class Technology implements Localizable {
	public String id;
	public ClausewitzDate technologyDate;
	public double cost;    // research_cost
	public int year;    // start_year
	public List<Equipment> enabledEquipments;
	public List<EquipmentModule> enabledEquipmentModules;
	public List<TechCategory> categories;
	public List<TechPath> paths;
	// ai_will_do

	@Override
	public @NotNull scala.collection.mutable.Map<Property, String> getLocalizableProperties() {
		return CollectionConverters.asScala(Map.of(
			Property.NAME, id
		)); 
	}

	// todo this can be more inclusive
	@Override
	public @NotNull scala.collection.Iterable<? extends Localizable> getLocalizableGroup() {
		return CollectionConverters.asScala(List.of(this));
	}
}
