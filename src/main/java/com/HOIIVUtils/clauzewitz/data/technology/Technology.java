package com.HOIIVUtils.clauzewitz.data.technology;

import com.HOIIVUtils.clauzewitz.code.ClausewitzDate;
import com.HOIIVUtils.clauzewitz.localization.Localizable;
import org.jetbrains.annotations.NotNull;

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
	public @NotNull Map<Property, String> getLocalizableProperties() {
		return Map.of(
			Property.NAME, id
		);
	}

	// todo this can be more inclusive
	@Override
	public @NotNull Collection<? extends Localizable> getLocalizableGroup() {
		return List.of(this);
	}
}
