package com.HOIIVUtils.hoi4utils.clausewitz_data.units;

import com.HOIIVUtils.hoi4utils.clausewitz_parser_deprecated.Expression;
import com.HOIIVUtils.hoi4utils.clausewitz_parser_deprecated.Parser;

import java.io.File;
import java.util.ArrayList;
/*
 * Unit File
 */
public class Unit {
	public static ArrayList<Unit> units;
	int maxOrganization;
	int maxStrength;
	double defaultMorale;

	int manpower;
	double maximumSpeed;
	int trainingTime;
	double weight;
	double supplyConsumption;
	int suppression;
	// todo terrain modifiers

	public Unit (int manpower) {
		this.manpower = manpower;
	}

	public int getManpower() {
		return manpower;
	}

	public void setManpower(int manpower) {
		this.manpower = manpower;
	}

	public static void readUnits(File dir) {
		if (!dir.isDirectory()) {
			throw new IllegalArgumentException("this is not a directory you foul bastard");
		}
		if (dir.listFiles().length == 0) {
			throw new IllegalArgumentException("This is empty");
		}

		for (File f : dir.listFiles()) {
			Parser parser = new Parser(f);
			Expression exp = parser.find("sub_units");
			Expression[] unitsExp = exp.getSubexpressions();

			for (Expression unitExp : unitsExp) {
				units = new ArrayList<>();
				int manpower = unitExp.get("manpower").getValue();
				Unit unit = new Unit(manpower);
				units.add(unit);
				unit.defaultMorale = unitExp.get("default_morale").getDoubleValue();
				unit.maximumSpeed = unitExp.get("maximum_speed").getDoubleValue();
				unit.trainingTime = unitExp.get("training_time").getValue();
				unit.maxOrganization = unitExp.get("max_organisation").getValue();
				unit.maxStrength = unitExp.get("max_strength").getValue();
				unit.supplyConsumption = unitExp.get("supply_consumption").getDoubleValue();
				unit.suppression = unitExp.get("suppression").getValue();
				unit.weight = unitExp.get("weight").getDoubleValue();

			}
		}
	}
}
