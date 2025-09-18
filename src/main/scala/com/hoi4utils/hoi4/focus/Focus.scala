package com.hoi4utils.hoi4.focus

import com.hoi4utils.ddsreader.DDSReader
import com.hoi4utils.exceptions.UnexpectedIdentifierException
import com.hoi4utils.gfx.Interface
import com.hoi4utils.hoi4.effect.{Effect, EffectDatabase}
import com.hoi4utils.hoi4.scope.Scope
import com.hoi4utils.localization.{Localizable, Localization, Property}
import com.hoi4utils.parser.Node
import com.hoi4utils.script.*
import com.hoi4utils.{BoolType, ExpectedRange}
import javafx.scene.image.Image

import java.awt.Point
import scala.annotation.tailrec
import scala.collection.mutable
import scala.collection.mutable.ListBuffer


@lombok.extern.slf4j.Slf4j
class Focus(var focusTree: FocusTree) extends StructuredPDX("focus") with Localizable {
  private val FOCUS_COST_FACTOR = 7
  private val DEFAULT_FOCUS_COST = 10.0

  /* attributes */
  final val id: StringPDX = new StringPDX("id")   // todo don't allow id to be null that feels wrong
  final val icon: MultiPDX[Icon] = new MultiPDX(Some(() => new SimpleIcon()), Some(() => new BlockIcon()), "icon")
  final val x: IntPDX = new IntPDX("x") // if relative, relative x
  final val y: IntPDX = new IntPDX("y", ExpectedRange.ofPositiveInt) // if relative, relative y
  final val prerequisites: MultiPDX[PrerequisiteSet] = new MultiPDX(None, Some(() => new PrerequisiteSet(() => focusTree.focuses)), "prerequisite")
  final val mutuallyExclusive: MultiPDX[MutuallyExclusiveSet] = new MultiPDX(None, Some(() => new MutuallyExclusiveSet(() => focusTree.focuses)), "mutually_exclusive")
  final val relativePositionFocus = new ReferencePDX[Focus](() => focusTree.focuses, f => f.id.value, "relative_position_id")
  final val cost: DoublePDX = new DoublePDX("cost", ExpectedRange(-1.0, Double.PositiveInfinity))
  final val availableIfCapitulated: BooleanPDX = new BooleanPDX("available_if_capitulated", false, BoolType.YES_NO)
  final val cancelIfInvalid: BooleanPDX = new BooleanPDX("cancel_if_invalid", true, BoolType.YES_NO)
  final val continueIfInvalid: BooleanPDX = new BooleanPDX("continue_if_invalid", false, BoolType.YES_NO)
  //var ddsImage: Image = _
  final val ai_will_do = new AIWillDoPDX
  /* completion reward */
  final val completionReward: CompletionReward = new CompletionReward()

  def this(focusTree: FocusTree, node: Node) = {
    this(focusTree)
    loadPDX(node)
  }

  /**
   * @inheritdoc
   */
  override protected def childScripts: mutable.Iterable[PDXScript[?]] = {
    ListBuffer(id, icon, x, y, prerequisites, mutuallyExclusive, relativePositionFocus, cost,
      availableIfCapitulated, cancelIfInvalid, continueIfInvalid, ai_will_do, completionReward)
  }

  def absoluteX: Int = absolutePosition.x

  def absoluteY: Int = absolutePosition.y

  def position: Point = new Point(x.getOrElse(0), y.getOrElse(0))

  def absolutePosition: Point = {
    /**
     * Recursively calculate the absolute position of a focus, taking into account relative positions.
     * @param focus the focus to calculate the absolute position of
     * @param visited set of focus ids that have been visited to detect circular references
     * @param offsetAcc accumulated point adjustment
     * @return the absolute position of the focus
     */
    @tailrec
    def absolutePosition(focus: Focus, visited: Set[String] = Set.empty, offsetAcc: Point = new Point(0, 0)): Point = {
      if (focus.relativePositionFocus.isUndefined) {
        return new Point(focus.x + offsetAcc.x, focus.y + offsetAcc.y)
      }
      // Check for self-reference
      if (focus.relativePositionFocus @== focus.id) {
        logger.error(s"Relative position id same as focus id for $this")
        return new Point(focus.x + offsetAcc.x, focus.y + offsetAcc.y)
      }
      // Check for circular references
      if (visited(focus.id.str)) {
        logger.error(s"Circular reference detected involving focus id: ${id.str} in file ${focusTree.focusFile}")
        return new Point(focus.x + offsetAcc.x, focus.y + offsetAcc.y)
      }

      focus.relativePositionFocus.value match {
        case Some(relativeFocus: Focus) =>
          val newAcc = new Point(focus.x + offsetAcc.x, focus.y + offsetAcc.y)
          // Tail call: pass nextFocus, the updated visited set, and the new accumulated offset.
          absolutePosition(relativeFocus, visited + focus.id.str, newAcc)
        case None =>
          logger.error(s"Focus id ${focus.relativePositionFocus.getReferenceName} not a valid focus")
          new Point(focus.x + offsetAcc.x, focus.y + offsetAcc.y)
      }
    }

    absolutePosition(this)
  }

  override def toString: String = {
    id.value match {
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
    relativePositionFocus.value match {
      case Some(f) =>
        // keep relative to the focus, but absolute coordinates are always the same
        val rp = f.absolutePosition
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
    this.cost.set(cost.doubleValue())
  }

  def getDDSImage: Option[Image] = {
    // bad code. it's fine for now.
    if (icon.isDefined) {
      var img: Option[Image] = None
      for (i <- icon) i match {
        case simpleIcon: SimpleIcon => simpleIcon.value match {
          case Some(iconName) =>
            Interface.getGFX(iconName) match {
              case Some(gfx) => img = Some(DDSReader.readDDSImage(gfx).get)
              case None => None
            }
          case None => None
        }
        case blockIcon: BlockIcon => blockIcon.iconName match {
          case Some(iconName) =>
            Interface.getGFX(iconName) match {
              case Some(gfx) => img = Some(DDSReader.readDDSImage(gfx).get)
              case None => None
            }
          case None => None
        }
        case _ => None
      }
      img
    }
    else None
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

  def isLocalized(property: com.hoi4utils.localization.Property): Boolean = localization(property) match {
    case Some(_) => true
    case None => false
  }

  def isUnlocalized(property: com.hoi4utils.localization.Property): Boolean = !isLocalized(property)

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
    focusTree.countryTag match {
      case Some(tag) =>
        setCompletionRewardsOfNode(completionRewardNode, Scope.of(tag))
      case None =>
        setCompletionRewardsOfNode(completionRewardNode, null)
    }
  }

  def mutuallyExclusiveList: List[Focus] =   mutuallyExclusive.flatMap(_.references()).toList

  def prerequisiteList: List[Focus] = prerequisites.flatMap(_.references()).toList
  
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
//            logger.error("effect not found: " + n.name())
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
    extends MultiReferencePDX[Focus](referenceFocusesSupplier, (f: Focus) => f.id.value, "prerequisite", "focus") {

    def this() = {
      this(() => focusTree.focuses)
    }
  }
  /**
   * mutually exclusive is a multi-reference of focuses
   */
  class MutuallyExclusiveSet(referenceFocusesSupplier: () => Iterable[Focus])
    extends MultiReferencePDX[Focus](referenceFocusesSupplier, (f: Focus) => f.id.value, "mutually_exclusive", "focus") {
  }

  trait Icon extends PDXScript[?] {
  }

  class SimpleIcon extends StringPDX("icon") with Icon {
  }

  class BlockIcon extends StructuredPDX("icon") with Icon {

    final private val `value`: StringPDX = new StringPDX("value")

    override protected def childScripts: mutable.Iterable[? <: PDXScript[?]] = ListBuffer(`value`)

    override def equals(other: PDXScript[?]): Boolean = other match {
      case icon: Focus#Icon => `value`.equals(icon.value) // todo :(
      case _ => false
    }

    def iconName: Option[String] = `value`.value
  }

  // This is breaking my brain because the loadpdx call here is the most confusing to keep track of with it being a: inner class + override + super. + child child child class
  // like wtf or how the fuck am I suppose to figure out and clean it up so I can log errors and metadata without breaking the whole program like before
  // it so such tightly knitted rat nest of fuck ass object oriented code.
  // trait class and abstracts shouldn't handle exceptions / errors / user expected errors!
  //      cuz the generic code means its hard to get metadata, log, debug etc
  //TODO This small class is producing MOST of the unexpected identifier exceptions that show up in terminal, can you clean it up @Skorp or explain how I can touch it, please.
  class CompletionReward extends CollectionPDX[Effect](EffectDatabase(), "completion_reward") {
    @throws[UnexpectedIdentifierException]
    override def loadPDX(expression: Node): Unit = {
      super.loadPDX(expression)
    }

    override def getPDXTypeName: String = "Completion Reward"
  }

  class AIWillDoPDX extends StructuredPDX("ai_will_do") {
    final val base = new DoublePDX("base")
    final val factor = new DoublePDX("factor")
    final val add = new DoublePDX("add")
    final val modifier = new MultiPDX[AIWillDoModifierPDX](None, Some(() => new AIWillDoModifierPDX), "modifier")

    override protected def childScripts: mutable.Iterable[? <: PDXScript[?]] = ListBuffer(base, factor, add, modifier)

    override def getPDXTypeName: String = "AI Willingness"

    class AIWillDoModifierPDX extends StructuredPDX("modifier") {
      final val base = new DoublePDX("base")
      final val factor = new DoublePDX("factor")
      final val add = new DoublePDX("add")
      // todo trigger block

      override protected def childScripts: mutable.Iterable[? <: PDXScript[?]] = ListBuffer(base, factor, add)

      override def getPDXTypeName: String = "Modifier"
    }
  }

}

object Focus {
  def getDataFunctions: Iterable[Focus => ?] = {
    def locOrMissing(p: Property): Focus => String =
      f => f.localizationText(p).getOrElse("[Localization missing]")

    List[Focus => ?](
      f => f.id.getOrElse(s"[Focus missing id, focus tree: ${f.focusTree}, file: ${f.focusTree.fileName}]"),
      locOrMissing(Property.NAME),
      locOrMissing(Property.DESCRIPTION)
    )
  }

  def focusesWithPrerequisites(focuses: Iterable[Focus]): List[(Focus, List[Focus])] = {
    focuses.filter(_.hasPrerequisites).map { f =>
      (f, f.prerequisiteSets.flatMap(_.references()))
    }.toList
  }

  //  def schema = new PDXSchema()
}
