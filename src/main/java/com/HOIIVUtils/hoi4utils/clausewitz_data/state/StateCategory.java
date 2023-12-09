package com.HOIIVUtils.hoi4utils.clausewitz_data.state;

import com.HOIIVUtils.hoi4utils.HOIIVFile;
import com.HOIIVUtils.hoi4utils.clausewitz_data.Localizable;
import com.HOIIVUtils.hoi4utils.clausewitz_parser_deprecated.Expression;
import com.HOIIVUtils.hoi4utils.clausewitz_parser_deprecated.Parser;

import java.io.File;
import java.util.ArrayList;
/*
 * StateCategory File
 * //todo refactor stuff here
 */
public class StateCategory {
	private static File state_category_folder = null;		 // set by

	public StateCategory() {

	}

	/**
	 * localizable: category name
	 * @param categoryName
	 * @param buildingSlots
	 */
	public record StateCategoryType(String categoryName, int buildingSlots) implements Localizable {
		static ArrayList<StateCategoryType> stateCategoryTypes = new ArrayList<>();

		public StateCategoryType {
			stateCategoryTypes.add(this);
		}

		private static void defaultStateCategories() {
			new StateCategoryType("city", 6);
			new StateCategoryType("enclave", 0);
			new StateCategoryType("large_city", 8);
			new StateCategoryType("large_town", 5);
			new StateCategoryType("megalopolis", 12);
			new StateCategoryType("metropolis", 10);
			new StateCategoryType("pastoral", 1);
			new StateCategoryType("rural", 2);
			new StateCategoryType("small_island", 1);
			new StateCategoryType("tiny_island", 0);
			new StateCategoryType("town", 4);
			new StateCategoryType("wasteland", 0);
		}
	}

	public static void loadStateCategories() {
		File dir = new File(HOIIVFile.common_folder + "\\state_category");

		if (dir.exists() && dir.isDirectory() && dir.listFiles().length > 0) {
			state_category_folder = dir;

			/* custom state categories */
			for (File file : state_category_folder.listFiles()) {
				Parser parser = new Parser(file);
				Expression exp = parser.expression();

				if (exp.get(file.getName()) != null ) {
					Expression categoryExp = exp.get(file.getName());
					// ! todo Implement the string
					//String category_name = categoryExp.getText();
					if (categoryExp.get("local_building_slots") != null) {
					//	int numBuildingSlots = categoryExp.get("local_building_slots").getValue();
					} else {
					//	int numBuildingSlots = 0;
						System.err.println("Error - StateCategory.java: number of building slots was not found, " +
								"defaulted to 0");
					}
				}
			}

		} else {
			state_category_folder = null;

			/* default state categories */
			StateCategoryType.defaultStateCategories();
		}
	}

}