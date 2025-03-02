package com.hoi4utils.clausewitz.data.focus

import com.hoi4utils.ExpectedRange
import com.hoi4utils.clausewitz.{BoolType, DataFunctionProvider}
import com.hoi4utils.clausewitz.code.effect.*
import com.hoi4utils.clausewitz.code.scope.*
import com.hoi4utils.clausewitz.localization.*
import com.hoi4utils.clausewitz.script.*
import com.hoi4utils.clausewitz_parser.Node
import javafx.scene.image.Image

import java.awt.Point
import scala.annotation.targetName
import scala.collection.mutable
import scala.collection.mutable.ListBuffer

@lombok.extern.slf4j.Slf4j
class Focus(var focusTree: FocusTree) extends StructuredPDX("focus") with Localizable with DataFunctionProvider[Focus] {
  private val FOCUS_COST_FACTOR = 7
  private val DEFAULT_FOCUS_COST = 10.0

  /* attributes */
  final val id: StringPDX = new StringPDX("id")   // todo don't allow id to be null that feels wrong
  final val icon: MultiPDX[Icon] = new MultiPDX(Some(() => new SimpleIcon()), Some(() => new BlockIcon()), "icon")
  final val x: IntPDX = new IntPDX("x") // if relative, relative x
  final val y: IntPDX = new IntPDX("y", ExpectedRange.ofPositiveInt) // if relative, relative y
  final val prerequisites: MultiPDX[PrerequisiteSet] = new MultiPDX(None, Some(() => new PrerequisiteSet(() => focusTree.focuses)), "prerequisite")
  final val mutuallyExclusive: MultiPDX[MutuallyExclusiveSet] = new MultiPDX(None, Some(() => new MutuallyExclusiveSet(() => focusTree.focuses)), "mutually_exclusive")
  final val relativePositionFocus = new ReferencePDX[Focus](() => focusTree.focuses, f => f.id.get(), "relative_position_id")
  final val cost: DoublePDX = new DoublePDX("cost", ExpectedRange(-1.0, Double.PositiveInfinity))
  final val availableIfCapitulated: BooleanPDX = new BooleanPDX("available_if_capitulated", false, BoolType.YES_NO)
  final val cancelIfInvalid: BooleanPDX = new BooleanPDX("cancel_if_invalid", true, BoolType.YES_NO)
  final val continueIfInvalid: BooleanPDX = new BooleanPDX("continue_if_invalid", false, BoolType.YES_NO)
  //var ddsImage: Image = _
  /* completion reward */
  val completionReward: CompletionReward = new CompletionReward()

  def this(focusTree: FocusTree, node: Node) = {
    this(focusTree)
    loadPDX(node)
  }

  override protected def childScripts: mutable.Iterable[PDXScript[?]] = {
    ListBuffer(id, icon, x, y, prerequisites, mutuallyExclusive, relativePositionFocus, cost, availableIfCapitulated,
      cancelIfInvalid, continueIfInvalid, completionReward)
  }

  def absoluteX: Int = absolutePosition().x

  def absoluteY: Int = absolutePosition().y

  def position: Point = new Point(x.getOrElse(0), y.getOrElse(0))

  def absolutePosition: Point = absolutePosition(Set.empty)

  private def absolutePosition(visited: Set[String] = Set.empty): Point = {
    if (relativePositionFocus.isUndefined) {
      return position
    }
    // Check for self-reference
    if (relativePositionFocus @== id) {
      System.err.println(s"Relative position id same as focus id for $this")
      return position
    }
    // Check for circular references
    if (visited(id.str)) {
      System.err.println(s"Circular reference detected involving focus id: ${id.str} in file ${focusTree.focusFile}")
      return position
    }

    relativePositionFocus.get() match {
      case Some(f) =>
        // Call absolutePosition on the focus, adding the current focus id to the visited set
        // Add our relative coordinates to the relative focus absolute position to obtain our absolute position
        val adjPoint = f.absolutePosition(visited + id.str)
        val absolutePoint = new Point(adjPoint.x + x.getOrElse(0), adjPoint.y + y.getOrElse(0))
        absolutePoint
      case None =>
        System.err.println(s"Focus id ${relativePositionFocus.getReferenceName} not a valid focus")
        position
    }
  }

  override def toString: String = {
    id.get() match {
      case Some(id) => id
      case None => "[Unknown]"
    }
  }

  def setID(s: String): Unit = {
    this.id.set(s)
  }

  def setXY(x: Int, y: Int): Point = {
    val prev = new Point(this.x.getOrElse(0), this.y.getOrElse(0))
    this.x @= x
    this.y @= y
    prev
  }

  /**
   * Set the absolute x and y coordinates of the focus. If the focus has a relative position focus, it remains relative to
   * that position, but its absolute coordinates are always the same.
   *
   * @param x absolute x-coordinate
   * @param y absolute y-coordinate
   * @param updateChildRelativeOffsets if true, update descendant relative focus positions by some offset so that they remain
   *                                   in the same position even though the position of this focus changes.
   * @return the previous absolute position.
   */
  def setAbsoluteXY(x: Int, y: Int, updateChildRelativeOffsets: Boolean): Point = {
    val prevAbsolute = absolutePosition
    relativePositionFocus.get() match {
      case Some(f) =>
        // keep relative to the focus, but absolute coordinates are always the same
        val rp = f.absolutePosition()
        setXY(x - rp.x, y - rp.y)
      case None => setXY(x, y)
    }
    if (updateChildRelativeOffsets) {
      for (focus <- focusTree.focuses) {
        if (focus.relativePositionFocus @== this) {
          focus.offsetXY(prevAbsolute.x - x, prevAbsolute.y - y)
        }
      }
    }
    prevAbsolute
  }

  def setXY(xy: Point): Point = setXY(xy.x, xy.y)

  def offsetXY(x: Int, y: Int): Point = {
    val prev = new Point(this.x.getOrElse(0), this.y.getOrElse(0))
    this.x @= (this.x.getOrElse(0) + x)
    this.y @= (this.y.getOrElse(0) + y)
    prev
  }

  /**
   * Check if the focus is at the given relative position.
   * @param x relative x coordinate
   * @param y relative y coordinate
   * @return
   */
  def hasRelativePosition(x: Int, y: Int): Boolean = (this.x @== x) && (this.y @== y) 

  /**
   * Check if the focus is at the given absolute position.
   *
   * @param x absolute x-coordinate
   * @param y absolute y-coordinate
   * @return
   */
  def hasAbsolutePosition(x: Int, y: Int): Boolean = this.absoluteX == x && this.absoluteY == y

  def setCost(): Unit = setCost(DEFAULT_FOCUS_COST)

  def setCost(cost: Number): Unit = {
    this.cost.setNode(cost.doubleValue())
  }

  def getDDSImage: Option[Image] = {
    if (icon.isDefined) {
      for (i <- icon) i match {
        case simpleIcon: SimpleIcon => simpleIcon.get() match {
          case Some(iconName) =>
            // todo load using Interface to find gfx
            None
          case None => None
        }
        case blockIcon: BlockIcon => blockIcon.iconName match {
          case Some(iconName) =>
            // todo load using Interface to find gfx
            None
          case None => None
        }
        case _ => None
      }
    }
    None
  }

  def hasPrerequisites: Boolean = prerequisites.nonEmpty

  def isMutuallyExclusive: Boolean = !(mutuallyExclusive.isUndefined || mutuallyExclusive.isEmpty)

  def displayedCompletionTime(): Double = Math.floor(preciseCompletionTime())

  def preciseCompletionTime(): Double = cost.getOrElse(DEFAULT_FOCUS_COST) * FOCUS_COST_FACTOR

  override def getLocalizableProperties: mutable.Map[Property, String] = {
    //mutable.Map(Property.NAME -> id.get().get, Property.DESCRIPTION -> s"${id.get().get}_desc")
    val id = this.id.getOrElse("")
    mutable.Map(Property.NAME -> id, Property.DESCRIPTION -> s"${id}_desc")
  }

  override def getLocalizableGroup: Iterable[Localizable] = {
    if (focusTree == null) {
      Iterable(this)
    } else {
      focusTree.getLocalizableGroup
    }
  }
  
  def isLocalized: Boolean = localizationStatus(Property.NAME) != Localization.Status.MISSING

  override def toScript: String = {
    val details = new StringBuilder()
    for (property <- childScripts) {
      val text = property.toScript
      if (text != null) {
        details.append(text)
      }
    }
    details.toString()
  }

  def getCompletionReward: CompletionReward = completionReward

//  def setCompletionReward(completionReward: List[Effect]): Unit = {
//    this.completionReward = completionReward
//  }

//  def setCompletionReward(completionRewardNode: Node): Unit = {
//    completionReward = ListBuffer()
//    if (completionRewardNode.valueIsNull) {
//      return
//    }
//
//    setCompletionRewardsOfNode(completionRewardNode)
//  }

  private def setCompletionRewardsOfNode(completionRewardNode: Node): Unit = {
    focusTree.country.get() match {
      case Some(countryTag) =>
        setCompletionRewardsOfNode(completionRewardNode, Scope.of(countryTag))
      case None =>
        setCompletionRewardsOfNode(completionRewardNode, null)
    }
  }

  def mutuallyExclusiveList: List[Focus] = mutuallyExclusive.flatten().toList
  
  def prerequisiteList: List[Focus] = prerequisites.flatten().toList
  
  def prerequisiteSets: List[PrerequisiteSet] = prerequisites.toList

  private def setCompletionRewardsOfNode(completionRewardNode: Node, scope: Scope): Unit = {
//    completionRewardNode.$ match {
//      case l: ListBuffer[Node] => for(n <- l) {
//        if (n.value().isList) {
//          var s: Scope = null
//          if (CountryTagsManager.exists(n.name())) {
//            s = Scope.of(CountryTagsManager.get(n.name()))
//          } else {
//            try {
//              s = Scope.of(n.name(), scope)
//            } catch {
//              case e: NotPermittedInScopeException =>
//                println(e.getMessage)
//            }
//          }
//
//          if (s == null || s.scopeCategory == ScopeCategory.EFFECT) {
//            var effect: Effect = null
//            try {
//              effect = Effect.of(n.name(), scope, n.value())
//            } catch {
//              case e: InvalidEffectParameterException =>
//                println(e.getMessage)
//              case e: NotPermittedInScopeException =>
//                println(e.getMessage + ", scope: " + scope + ", list? " + n.name())
//            }
//            if (effect != null) {
//              completionReward += effect
//            } else {
//              println("Scope " + n.name() + " unknown.")
//            }
//          } else {
//            setCompletionRewardsOfNode(n, s)
//          }
//        } else if (!n.valueIsNull) {
//          var effect: Effect = null
//          try {
//            effect = Effect.of(n.name(), scope, n.value())
//          } catch {
//            case e: InvalidEffectParameterException =>
//              println(e.getMessage)
//            case e: NotPermittedInScopeException =>
//              println(e.getMessage + ", scope: " + scope)
//          }
//          if (effect == null) {
//            System.err.println("effect not found: " + n.name())
//          } else {
//            if (effect.hasSupportedTargets) {
//              try {
//                effect.setTarget(n.value().string(), scope)
//              } catch {
//                case e: IllegalStateException =>
//                  e.printStackTrace()
//              }
//            }
//            completionReward += effect
//          }
//        } else {
//          var effect: Effect = null
//          try {
//            effect = Effect.of(n.name(), scope)
//          } catch {
//            case e: NotPermittedInScopeException =>
//              println(e.getMessage)
//          }
//          if (effect != null) {
//            completionReward += effect
//          }
//        }
//      }
//      case _ =>
//    }
  }

  class PrerequisiteSet(referenceFocusesSupplier: () => Iterable[Focus])
    extends MultiReferencePDX[Focus](referenceFocusesSupplier, (f: Focus) => f.id.get(), "prerequisite", "focus") {

    def this() = {
      this(() => focusTree.focuses)
    }
  }
  /**
   * mutually exclusive is a multi-reference of focuses
   */
  class MutuallyExclusiveSet(referenceFocusesSupplier: () => Iterable[Focus])
    extends MultiReferencePDX[Focus](referenceFocusesSupplier, (f: Focus) => f.id.get(), "mutually_exclusive", "focus") {
  }

  trait Icon extends PDXScript[?] {
  }

  class SimpleIcon extends StringPDX("icon") with Icon {
  }

  class BlockIcon extends StructuredPDX("icon") with Icon {

    final private val value: StringPDX = new StringPDX("value")

    override protected def childScripts: mutable.Iterable[? <: PDXScript[?]] = ListBuffer(value)

    override def equals(other: PDXScript[?]): Boolean = {
      other match {
        case icon: Icon => value.equals(icon.get()) // todo :(
        case _ => false
      }
    }

    def iconName: Option[String] = value.get()
  }

  class CompletionReward extends CollectionPDX[Effect](EffectDatabase(), "completion_reward") {
    @throws[UnexpectedIdentifierException]
    override def loadPDX(expression: Node): Unit = {
      super.loadPDX(expression)
    }
  }

}

object Focus {
  def getDataFunctions: Iterable[Focus => ?] = {
    val dataFunctions = ListBuffer[Focus => ?]()
    dataFunctions += (focus => {
      focus.id.getOrElse("[unknown]")
    })
    dataFunctions += (focus => focus.localizationText(Property.NAME))
    dataFunctions += (focus => focus.localizationText(Property.DESCRIPTION))
    dataFunctions
  }

//  def schema = new PDXSchema()
}
