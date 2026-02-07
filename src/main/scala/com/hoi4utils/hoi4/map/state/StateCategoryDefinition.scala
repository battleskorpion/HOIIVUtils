package com.hoi4utils.hoi4.map.state

import com.hoi4utils.parser.{Node, SeqNode}
import com.hoi4utils.script.{IntPDX, PDXScript, Referable, StructuredPDX}

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

class StateCategoryDefinition(pdxIdentifier: String) extends StructuredPDX(pdxIdentifier) with Referable[String] {
  final var local_building_slots = new IntPDX("local_building_slots")
  //final var color = new ColorPDX("color") // todo :D

  def this(node: SeqNode) = {
    this(node.name)
    var file = None
    // TODO TODO 
//    loadPDX(node, file)   
  }

  override protected def childScripts: mutable.Seq[? <: PDXScript[?, ?]] = {
    ListBuffer(local_building_slots)
  }

  def name: String = this.pdxIdentifier

  override def referableID: Option[String] = Some(name)

  //	/**
  //	 * localizable: category name
  //	 * @param categoryName
  //	 * @param buildingSlots
  //	 */
  //	public record StateCategoryType(String categoryName, int buildingSlots) implements Localizable, Comparable<StateCategoryType> {
  //		static ArrayList<StateCategoryType> stateCategoryTypes = new ArrayList<>();
  //
  //		public StateCategoryType {
  //			stateCategoryTypes.add(this);
  //		}
  //
  //		private static void defaultStateCategories() {
  //			new StateCategoryType("city", 6);
  //			new StateCategoryType("enclave", 0);
  //			new StateCategoryType("large_city", 8);
  //			new StateCategoryType("large_town", 5);
  //			new StateCategoryType("megalopolis", 12);
  //			new StateCategoryType("metropolis", 10);
  //			new StateCategoryType("pastoral", 1);
  //			new StateCategoryType("rural", 2);
  //			new StateCategoryType("small_island", 1);
  //			new StateCategoryType("tiny_island", 0);
  //			new StateCategoryType("town", 4);
  //			new StateCategoryType("wasteland", 0);
  //		}
  //
  //		@Override
  //		public int compareTo(@NotNull StateCategory.StateCategoryType o) {
  //			int compare = Integer.compare(buildingSlots, o.buildingSlots);
  //			if (compare == 0) {
  //				compare = categoryName.compareTo(o.categoryName);
  //			}
  //			return compare;
  //		}
  //
  //		@Override
  //		public @NotNull scala.collection.mutable.Map<Property, String> getLocalizableProperties() {
  //			return CollectionConverters.asScala(Map.of());
  //		}
  //
  //		@Override
  //		public @NotNull scala.collection.Iterable<? extends Localizable> getLocalizableGroup() {
  //			return CollectionConverters.asScala(stateCategoryTypes);
  //		}
  //	}
  //	public static void loadStateCategories() {
  //		File dir = new File(HOIIVFiles.Mod.common_folder + "\\state_category");
  //
  //		if (dir.exists() && dir.isDirectory() && dir.listFiles().length > 0) {
  //			state_category_folder = dir;
  //
  //			/* custom state categories */
  //			for (File file : state_category_folder.listFiles()) {
  //				Parser parser = new Parser(file);
  //				Node rootExp = null;
  //				try {
  //					rootExp = parser.parse();
  //				} catch (ParserException e) {
  //					throw new RuntimeException(e);
  //				}
  //
  //				if (rootExp.contains(file.getName())) {
  //					Node categoryExp = rootExp.find(file.getName()).getOrElse(null);
  //					// ! todo Implement the string
  //					//String category_name = categoryExp.getText();
  //					if (categoryExp.contains("local_building_slots")) {
  //					//	int numBuildingSlots = categoryExp.get("local_building_slots").getValue();
  //					} else {
  //					//	int numBuildingSlots = 0;
  //						logger.error("Error - StateCategory.java: number of building slots was not found, " +
  //								"defaulted to 0");
  //					}
  //				}
  //			}
  //
  //		} else {
  //			state_category_folder = null;
  //
  //			/* default state categories */
  //			StateCategoryType.defaultStateCategories();
  //		}
  //	}
}
