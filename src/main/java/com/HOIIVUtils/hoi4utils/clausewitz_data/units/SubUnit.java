
package com.HOIIVUtils.hoi4utils.clausewitz_data.units;

import com.HOIIVUtils.clausewitz_parser.Node;
import com.HOIIVUtils.clausewitz_parser.Parser;
import com.HOIIVUtils.clausewitz_parser.ParserException;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/*
 * Unit File
 */
public record SubUnit(
		String abbreviation,
		String sprite,
		String mapIconCategory,
		int priority,
		int aiPriority,
		boolean active,
		String group,
		int combatWidth,
		int manpower,
		int maxOrganization,
		double defaultMorale,
		double maxStrength,
		int trainingTime,
		double weight,
		double supplyConsumption
) {
	public static List<SubUnit> read(File dir) {
		if (!dir.isDirectory()) {
			throw new IllegalArgumentException("this is not a directory");
		}
		if (dir.listFiles().length == 0) {
			throw new IllegalArgumentException("This is empty");
		}

		List<SubUnit> subUnits = new ArrayList<>();
		for (File f : dir.listFiles()) {
			Parser parser = new Parser(f);
			Node rootNode;
			try {
				rootNode = parser.parse();
			} catch (ParserException e) {
				throw new RuntimeException(e);
			}

			var l = rootNode.filterName("sub_units");
			// loop through each sub unit definition in this file
			for (Node subUnitNode : l.toList()) {
				SubUnit subUnit = new SubUnit(
						subUnitNode.getValue("abbreviation").string(),
						subUnitNode.getValue("sprite").string(),
						subUnitNode.getValue("map_icon_category").string(),
						subUnitNode.getValue("priority").integer(),
						subUnitNode.getValue("ai_priority").integer(),
						subUnitNode.getValue("active").bool(Node.BoolType.YES_NO),
						//subUnit.type = subUnitNode.getValue("type").string(),
						subUnitNode.getValue("group").string(),
						//subUnit.categories
						subUnitNode.getValue("combat_width").integer(),
						//subUnit.need
						subUnitNode.getValue("manpower").integer(),
						subUnitNode.getValue("max_organization").integer(),
						subUnitNode.getValue("default_morale").rational(),
						subUnitNode.getValue("max_strength").rational(),
						subUnitNode.getValue("training_time").integer(),
						subUnitNode.getValue("weight").rational(),
						subUnitNode.getValue("supply_consumption").rational()
				);

				subUnits.add(subUnit);
			}
		}
		return subUnits;
	}
}
