package com.HOIIVUtils.clauzewitz.data.technology;

import com.HOIIVUtils.clauzewitz.code.ClausewitzDate;
import com.HOIIVUtils.clauzewitz.localization.Localizable;

import java.util.List;

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

}
