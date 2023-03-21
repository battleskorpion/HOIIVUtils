package clausewitz_coding.state;

import clausewitz_coding.HOI4Fixes;
import hoi4Parser.Expression;
import hoi4Parser.Parser;

import java.io.File;
import java.util.ArrayList;

public class StateCategory {
    private static File state_category_folder = null;         // set by

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
        File dir = new File(HOI4Fixes.hoi4_dir_name + "\\common\\state_category");

        if (dir.exists() && dir.isDirectory() && dir.listFiles().length > 0) {
            state_category_folder = dir;

            /* custom state categories */
            for (File file : state_category_folder.listFiles()) {
                Parser parser = new Parser(file);
                Expression exp = parser.expressions();

                if (exp.get(file.getName()) != null ) {
                    Expression categoryExp = exp.get(file.getName());
                    String name = categoryExp.getName();
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