package com.hoi4utils.hoi4.focus

import com.hoi4utils.extensions.*
import com.hoi4utils.gfx.Interface.getGFX
import com.hoi4utils.hoi4.effect.{Effect, EffectDatabase}
import com.hoi4utils.hoi4.focus.Focus.focusErrors
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

class Focus(var focusTree: FocusTree, node: Node = null) extends StructuredPDX("focus") with Localizable {
  private val FOCUS_COST_FACTOR = 7
  private val DEFAULT_FOCUS_COST = 10.0

  /* attributes */
  val id: StringPDX = new StringPDX("id")   // todo don't allow id to be null that feels wrong
  val icon: MultiPDX[Icon] = new MultiPDX(Some(() => new SimpleIcon()), Some(() => new BlockIcon()), "icon")
  val x: IntPDX = new IntPDX("x") // if relative, relative x
  val y: IntPDX = new IntPDX("y", ExpectedRange.ofPositiveInt) // if relative, relative y
  private val prerequisites: MultiPDX[PrerequisiteSet] = new MultiPDX(None, Some(() => new PrerequisiteSet(() => focusTree.focuses)), "prerequisite")
  private val mutuallyExclusive: MultiPDX[MutuallyExclusiveSet] = new MultiPDX(None, Some(() => new MutuallyExclusiveSet(() => focusTree.focuses)), "mutually_exclusive")
  private val relativePositionFocus = new ReferencePDX[Focus](() => focusTree.focuses, f => f.id.value, "relative_position_id")
  private val cost: DoublePDX = new DoublePDX("cost", ExpectedRange(-1.0, Double.PositiveInfinity))
  private val availableIfCapitulated: BooleanPDX = new BooleanPDX("available_if_capitulated", false, BoolType.YES_NO)
  private val cancelIfInvalid: BooleanPDX = new BooleanPDX("cancel_if_invalid", true, BoolType.YES_NO)
  private val continueIfInvalid: BooleanPDX = new BooleanPDX("continue_if_invalid", false, BoolType.YES_NO)
  //var ddsImage: Image = _
  private val ai_will_do = new AIWillDoPDX
  /* completion reward */
  private val completionReward: CompletionReward = new CompletionReward()

  /* Load focus */
  loadPDX(node, focusErrors)

  /**
   * @inheritdoc
   */
  override protected def childScripts: mutable.Iterable[PDXScript[?]] = {
    ListBuffer(id, icon, x, y, prerequisites, mutuallyExclusive, relativePositionFocus, cost,
      availableIfCapitulated, cancelIfInvalid, continueIfInvalid, ai_will_do, completionReward)
  }

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

  override def toString: String = {
    id.value match {
      case Some(id) => id
      case None => "[Unknown]"
    }
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
        System.err.println(s"Relative position id same as focus id for $this")
        return new Point(focus.x + offsetAcc.x, focus.y + offsetAcc.y)
      }
      // Check for circular references
      if (visited(focus.id.str)) {
        System.err.println(s"Circular reference detected involving focus id: ${id.str} in file ${focusTree.focusFile}")
        return new Point(focus.x + offsetAcc.x, focus.y + offsetAcc.y)
      }

      focus.relativePositionFocus.value match {
        case Some(relativeFocus: Focus) =>
          val newAcc = new Point(focus.x + offsetAcc.x, focus.y + offsetAcc.y)
          // Tail call: pass nextFocus, the updated visited set, and the new accumulated offset.
          absolutePosition(relativeFocus, visited + focus.id.str, newAcc)
        case None =>
          System.err.println(s"Focus id ${focus.relativePositionFocus.getReferenceName} not a valid focus")
          new Point(focus.x + offsetAcc.x, focus.y + offsetAcc.y)
      }
    }

    absolutePosition(this)
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

  def getDDSImage: Option[Image] =
    icon.collectFirst {
      case simpleIcon: SimpleIcon =>
        simpleIcon.value.flatMap(getGFX).flatMap(_.getDDSImage) match {
          case Some(ddsImage) => ddsImage
          case None => null
        }
      case blockIcon: BlockIcon =>
        blockIcon.iconName.flatMap(getGFX).flatMap(_.getDDSImage) match {
          case Some(ddsImage) => ddsImage
          case None => null
        }
    }

  def hasPrerequisites: Boolean = prerequisites.nonEmpty

  def isMutuallyExclusive: Boolean = !(mutuallyExclusive.isUndefined || mutuallyExclusive.isEmpty)

  def displayedCompletionTime(): Double = Math.floor(preciseCompletionTime())

  def preciseCompletionTime(): Double = cost.getOrElse(DEFAULT_FOCUS_COST) * FOCUS_COST_FACTOR
  
  def isLocalized: Boolean = localizationStatus(Property.NAME) != Localization.Status.MISSING

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

  class PrerequisiteSet(referenceFocusesSupplier: () => Iterable[Focus] = () => focusTree.focuses)
    extends MultiReferencePDX[Focus](referenceFocusesSupplier, (f: Focus) => f.id.value, "prerequisite", "focus") {
  }
  /**
   * mutually exclusive is a multi-reference of focuses
   */
  class MutuallyExclusiveSet(referenceFocusesSupplier: () => Iterable[Focus])
    extends MultiReferencePDX[Focus](referenceFocusesSupplier, (f: Focus) => f.id.value, "mutually_exclusive", "focus") {
  }

  trait Icon extends PDXScript[?] {
    // todo
  }

  private class SimpleIcon extends StringPDX("icon") with Icon {
    // todo
  }

  class BlockIcon extends StructuredPDX("icon") with Icon {

    private val `value`: StringPDX = new StringPDX("value")

    override protected def childScripts: mutable.Iterable[? <: PDXScript[?]] = ListBuffer(`value`)

    override def equals(other: PDXScript[?]): Boolean = other match {
      case icon: Focus#Icon => `value`.equals(icon.value) // todo :(
      case _ => false
    }

    def iconName: Option[String] = `value`.value
  }

  class CompletionReward extends CollectionPDX[Effect](EffectDatabase(), "completion_reward") {
    override def loadPDX(expression: Node): Unit = {
      super.loadPDX(expression, focusErrors)
    }

    override def getPDXTypeName: String = "Completion Reward"
  }

  class AIWillDoPDX extends StructuredPDX("ai_will_do") {
    val base = new DoublePDX("base")
    val factor = new DoublePDX("factor")
    val add = new DoublePDX("add")
    val modifier = new MultiPDX[AIWillDoModifierPDX](None, Some(() => new AIWillDoModifierPDX), "modifier")

    override protected def childScripts: mutable.Iterable[? <: PDXScript[?]] = ListBuffer(base, factor, add, modifier)

    override def getPDXTypeName: String = "AI Willingness"

    class AIWillDoModifierPDX extends StructuredPDX("modifier") {
      val base = new DoublePDX("base")
      val factor = new DoublePDX("factor")
      val add = new DoublePDX("add")
      // todo trigger block

      override protected def childScripts: mutable.Iterable[? <: PDXScript[?]] = ListBuffer(base, factor, add)

      override def getPDXTypeName: String = "Modifier"
    }
  }

}

object Focus {

  val focusErrors: ListBuffer[String] = ListBuffer.empty

  def getDataFunctions: Iterable[Focus => ?] = {
    val dataFunctions = ListBuffer[Focus => ?]()
    dataFunctions += (focus => {
      focus.id.getOrElse("[unknown]")
    })
    dataFunctions += (_.localizationText(Property.NAME))
    dataFunctions += (_.localizationText(Property.DESCRIPTION))
    dataFunctions
  }

  def focusesWithPrerequisites(focuses: Iterable[Focus]): List[(Focus, List[Focus])] = {
    focuses.filter(_.hasPrerequisites).map { f =>
      (f, f.prerequisiteSets.flatMap(_.references()))
    }.toList
  }

  //  def schema = new PDXSchema()
}
