
package com.hoi4utils.clausewitz.data.units;

import com.hoi4utils.clausewitz.BoolType;
import com.hoi4utils.clausewitz.HOIIVUtils;
import com.hoi4utils.clausewitz_parser.Node;
import com.hoi4utils.clausewitz_parser.Parser;
import com.hoi4utils.clausewitz_parser.ParserException;

import scala.jdk.javaapi.CollectionConverters;

import javax.swing.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/*
 * Unit File
 */
public record SubUnit(
		String identifier,
		String abbreviation,
		String sprite,
		String mapIconCategory,
		Integer priority,
		Integer aiPriority,
		Boolean active,
		String group,
		Integer combatWidth,
		Integer manpower,
		Integer maxOrganization,
		Integer defaultMorale,
		Integer maxStrength,
		Integer trainingTime,
		Double weight,
		Double supplyConsumption
) {
	public static List<SubUnit> read(File dir) {
		if (!dir.isDirectory()) {
			throw new IllegalArgumentException(dir + " is not a directory");
		}
		// TODO: account for null, causes Unit window not to load 
		if (dir.listFiles().length == 0) {
			throw new IllegalArgumentException(dir + " is empty");
		}

		List<SubUnit> subUnits = new ArrayList<>();
		for (File file : dir.listFiles()) {
			if (file.isDirectory()) continue;
			Parser parser = new Parser(file);
			Node rootNode;
			try {
				rootNode = parser.parse();
			} catch (ParserException e) {
				JOptionPane.showMessageDialog(null, "[SubUnit] Error parsing " + file.getName() + ": " + e.getMessage());
				continue;
			}

			var list = CollectionConverters.asJava(rootNode.find("sub_units").toList());
			if (list.isEmpty()) {
				System.out.println("No sub_units found in " + file.getName());
				continue;
			}
			if (HOIIVUtils.getBoolean("dev_mode.enabled")) {
				System.out.println("File: " + file.getName() + ", subunits: " + list.size());
			}

			try {
				subUnits.addAll(createSubUnits(list));
			} catch (NullPointerException e) {
				throw new RuntimeException(e);
			}
			
		}
		return subUnits;
	}
	
	/**
	 * Creates a list of SubUnit objects from a list of Node objects.
	 *
	 * @param list	a list of Node objects representing subunits
	 * @return      a list of SubUnit objects
	 */
	private static List<SubUnit> createSubUnits(List<Node> list) throws NullPointerException {
		List<SubUnit> subUnits = new ArrayList<>();
		for (Node subUnitNode : list) {
			SubUnit subUnit = new SubUnit(
				subUnitNode.name(),
				subUnitNode.contains("abbreviation") ? subUnitNode.getValue("abbreviation").string() : null,
				subUnitNode.contains("sprite") ? subUnitNode.getValue("sprite").string() : null,
				subUnitNode.contains("map_icon_category") ? subUnitNode.getValue("map_icon_category").string() : null,
				subUnitNode.contains("priority") ? subUnitNode.getValue("priority").integer() : null,
				subUnitNode.contains("ai_priority") ? subUnitNode.getValue("ai_priority").integer() : null,
				subUnitNode.contains("active") ? subUnitNode.getValue("active").bool(BoolType.YES_NO) : null,
				//subUnit.type
				subUnitNode.contains("group") ? subUnitNode.getValue("group").string() : null,
				//subUnit.categories
				subUnitNode.contains("combat_width") ? subUnitNode.getValue("combat_width").integer() : null,
				//subUnit.need
				subUnitNode.contains("manpower") ? subUnitNode.getValue("manpower").integer() : null,
				subUnitNode.contains("max_organization") ? subUnitNode.getValue("max_organization").integer() : null,
				subUnitNode.contains("default_morale") ? subUnitNode.getValue("default_morale").integer() : null,
				subUnitNode.contains("max_strength") ? subUnitNode.getValue("max_strength").integer() : null,
				subUnitNode.contains("training_time") ? subUnitNode.getValue("training_time").integer() : null,
				subUnitNode.contains("weight") ? subUnitNode.getValue("weight").rational() : null,
				subUnitNode.contains("supply_consumption") ? subUnitNode.getValue("supply_consumption").rational() : null
			);
	
			subUnits.add(subUnit);
		}
		return subUnits;
	}
	
	public static List<Function<SubUnit, ?>> getDataFunctions() {
		List<Function<SubUnit, ?>> dataFunctions = new ArrayList<>(16);

		dataFunctions.add(SubUnit::identifier);
		dataFunctions.add(SubUnit::abbreviation);
		dataFunctions.add(SubUnit::sprite);
		dataFunctions.add(SubUnit::mapIconCategory);
		dataFunctions.add(SubUnit::priority);
		dataFunctions.add(SubUnit::aiPriority);
		dataFunctions.add(SubUnit::active);
		dataFunctions.add(SubUnit::group);
		dataFunctions.add(SubUnit::combatWidth);
		dataFunctions.add(SubUnit::manpower);
		dataFunctions.add(SubUnit::maxOrganization);
		dataFunctions.add(SubUnit::defaultMorale);
		dataFunctions.add(SubUnit::maxStrength);
		dataFunctions.add(SubUnit::trainingTime);
		dataFunctions.add(SubUnit::weight);
		dataFunctions.add(SubUnit::supplyConsumption);
		return dataFunctions;
	}

	public static List<String> getDataLabels() {
		List<String> dataFunctionLabels = new ArrayList<>(16);

		dataFunctionLabels.add("Subunit");
		dataFunctionLabels.add("Abbreviation");
		dataFunctionLabels.add("Sprite");
		dataFunctionLabels.add("Map Icon Category");
		dataFunctionLabels.add("Priority");
		dataFunctionLabels.add("AI Priority");
		dataFunctionLabels.add("Active");
		dataFunctionLabels.add("Group");
		dataFunctionLabels.add("Combat Width");
		dataFunctionLabels.add("Manpower");
		dataFunctionLabels.add("Max Organization");
		dataFunctionLabels.add("Default Morale");
		dataFunctionLabels.add("Max Strength");
		dataFunctionLabels.add("Training Time");
		dataFunctionLabels.add("Weight");
		dataFunctionLabels.add("Supply Consumption");
        return dataFunctionLabels;
    }
}
