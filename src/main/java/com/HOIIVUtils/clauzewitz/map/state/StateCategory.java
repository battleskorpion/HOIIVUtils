package com.HOIIVUtils.clauzewitz.map.state;

import com.HOIIVUtils.clausewitz_parser.ParserException;
import com.HOIIVUtils.clauzewitz.HOIIVFile;
import com.HOIIVUtils.clauzewitz.localization.Localizable;
import com.HOIIVUtils.clausewitz_parser.Node;
import com.HOIIVUtils.clausewitz_parser.Parser;
import org.jetbrains.annotations.NotNull;

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
	public record StateCategoryType(String categoryName, int buildingSlots) implements Localizable, Comparable<StateCategoryType> {
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

		@Override
		public int compareTo(@NotNull StateCategory.StateCategoryType o) {
			int compare = Integer.compare(buildingSlots, o.buildingSlots);
			if (compare == 0) {
				compare = categoryName.compareTo(o.categoryName);
			}
			return compare;
		}
	}

	public static void loadStateCategories() {
		File dir = new File(HOIIVFile.mod_common_folder + "\\state_category");

		if (dir.exists() && dir.isDirectory() && dir.listFiles().length > 0) {
			state_category_folder = dir;

			/* custom state categories */
			for (File file : state_category_folder.listFiles()) {
				Parser parser = new Parser(file);
				Node rootExp = null;
				try {
					rootExp = parser.parse();
				} catch (ParserException e) {
					throw new RuntimeException(e);
				}

				if (rootExp.contains(file.getName())) {
					Node categoryExp = rootExp.findFirst(file.getName());
					// ! todo Implement the string
					//String category_name = categoryExp.getText();
					if (categoryExp.contains("local_building_slots")) {
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