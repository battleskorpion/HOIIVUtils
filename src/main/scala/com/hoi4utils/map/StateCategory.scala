package com.hoi4utils.map

import com.hoi4utils.HOIIVFiles
import com.hoi4utils.exceptions.UnexpectedIdentifierException
import com.hoi4utils.parser.{Node, ParserException}
import com.hoi4utils.script.*
import org.apache.logging.log4j.{LogManager, Logger}

import java.io.File
import scala.collection.mutable
import scala.collection.mutable.ListBuffer


/*
 * StateCategory File
 * //todo refactor stuff here
 */

object StateCategory {

  def list: List[StateCategory] = {
    StateCategories.list.map(sc_def => new StateCategory(sc_def))
  }

  def apply(): PDXSupplier[StateCategory] = {
    new PDXSupplier[StateCategory] {
      override def simplePDXSupplier(): Option[Node => Option[StateCategory]] = {
        Some((expr: Node) => {
          Some(new StateCategory(expr))
        })
      }

      override def blockPDXSupplier(): Option[Node => Option[StateCategory]] = {
        Some((expr: Node) => {
          Some(new StateCategory(expr))
        })
      }
    }
  }

}

/**
 * Represents a valid state category.
 *
 * @param id
 */
class StateCategory(id: String) extends ReferencePDX[StateCategoryDef](() => StateCategories.list, s => Some(s.name), id) {
  /* init */
  require(isValidID(id), s"Invalid state category identifier: $id")

  def this(node: Node) = {
    this(node.name)
    loadPDX(node)
  }

  def this(stateCategoryDef: StateCategoryDef) = {
    this(stateCategoryDef.name)
  }

  def sameStateCategory(stateCategory: StateCategory): Boolean = this.isValidID(stateCategory.identifier)

  def sameStateCategory(identifier: String): Boolean = this.isValidID(identifier)

  def identifier: String = this.pdxIdentifier
}

object StateCategories {
  val logger: Logger = LogManager.getLogger(classOf[this.type])

  private val _stateCategoryFiles: ListBuffer[StateCategoryFile] = ListBuffer()

  def read(): Unit = {
    clear()

    var stateCategoryDirectory: Option[File] = None
    if (!HOIIVFiles.Mod.state_category_dir.exists || !HOIIVFiles.Mod.state_category_dir.isDirectory) {
      if (HOIIVFiles.HOI4.state_category_dir.exists && HOIIVFiles.HOI4.state_category_dir.isDirectory) {
        stateCategoryDirectory = Some(HOIIVFiles.HOI4.state_category_dir)
      }
    } else {
      stateCategoryDirectory = Some(HOIIVFiles.Mod.state_category_dir)
    }

    stateCategoryDirectory match {
      case Some(dir) =>
        logger.info(s"Reading resources from ${dir.getAbsolutePath}")
        //_resourcesPDX = Some(new Resources(dir))
        stateCategoryDirectory.filter(_.getName.endsWith(".txt")).foreach { f =>
          _stateCategoryFiles += new StateCategoryFile(f)
        }
      case None =>
        logger.error(s"In ${this.getClass.getSimpleName} - ${HOIIVFiles.HOI4.resources_file} is not a directory, " +
          s"or it does not exist (No resources file found).")
    }
  }

  def pdxSupplier(): PDXSupplier[StateCategoryDef] = {
    new PDXSupplier[StateCategoryDef] {
      override def simplePDXSupplier(): Option[Node => Option[StateCategoryDef]] = {
        Some((expr: Node) => {
          Some(new StateCategoryDef(expr))
        })
      }

      override def blockPDXSupplier(): Option[Node => Option[StateCategoryDef]] = {
        Some((expr: Node) => {
          Some(new StateCategoryDef(expr))
        })
      }
    }
  }

  def list: List[StateCategoryDef] = {
    _stateCategoryFiles.flatMap(_.toList).toList
  }

  def clear(): Unit = {
    _stateCategoryFiles.clear()
  }
}

class StateCategoryFile extends CollectionPDX[StateCategoryDef](StateCategories.pdxSupplier(), "state_categories") {
  private var _stateCategoryFile: Option[File] = None

  /* init */
  def this(file: File) = {
    this()
    if (!file.exists) {
      logger.error(s"State Category file does not exist: $file")
      throw new IllegalArgumentException(s"File does not exist: $file")
    }

    try loadPDX(file)
    catch {
      case e: ParserException =>
        logger.error(s"Parser Exception: $file", e)
      case e: UnexpectedIdentifierException =>
        throw new RuntimeException(e)
    }
    setFile(file)
  }

  def setFile(file: File): Unit = {
    _stateCategoryFile = Some(file)
  }

  @throws[UnexpectedIdentifierException]
  override def loadPDX(expression: Node): Unit = {
    super.loadPDX(expression)
  }

  override def getPDXTypeName: String = "State Category"
}

class StateCategoryDef(pdxIdentifier: String) extends StructuredPDX(pdxIdentifier) {
  final var local_building_slots = new IntPDX("local_building_slots")
  //final var color = new ColorPDX("color") // todo :D

  def this(node: Node) = {
    this(node.name)
    loadPDX(node)
  }

  override protected def childScripts: mutable.Iterable[? <: PDXScript[?]] = {
    ListBuffer(local_building_slots)
  }
  
  def name: String = this.pdxIdentifier

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
