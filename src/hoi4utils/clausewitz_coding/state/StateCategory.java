package hoi4utils.clausewitz_coding.state;

import hoi4utils.HOIIVUtils;
import hoi4utils.clausewitz_parser.Expression;
import hoi4utils.clausewitz_parser.Parser;
import java.io.File;
import java.util.ArrayList;
/*
 * StateCategory File
 */
public class StateCategory {
	private static File state_category_folder = null;		 // set by

	public StateCategory() {

	}

	public record StateCategoryType(String categoryName, int buildingSlots) {
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
		File dir = new File(HOIIVUtils.common_folder + "\\state_category");

		if (dir.exists() && dir.isDirectory() && dir.listFiles().length > 0) {
			state_category_folder = dir;

			/* custom state categories */
			for (File file : state_category_folder.listFiles()) {
				Parser parser = new Parser(file);
				Expression exp = parser.expression();

				if (exp.get(file.getName()) != null ) {
					Expression categoryExp = exp.get(file.getName());
					String category_name = categoryExp.getText(); // ! todo Implement the string
					if (categoryExp.get("local_building_slots") != null) {
						int numBuildingSlots = categoryExp.get("local_building_slots").getValue();
					} else {
						int numBuildingSlots = 0;
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