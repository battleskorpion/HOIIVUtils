package com.hoi4utils.clausewitz.data.focus

import com.hoi4utils.clausewitz.DataFunctionProvider
import com.hoi4utils.clausewitz.localization.*
import com.hoi4utils.clausewitz.script.*
import com.hoi4utils.clausewitz_parser.Node
import com.hoi4utils.clausewitz.BoolType
import com.hoi4utils.clausewitz.code.scope.*
import com.hoi4utils.clausewitz.code.effect.*

import java.awt.Point
import javafx.scene.image.Image

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

class Focus(var focusTree: FocusTree) extends StructuredPDX with Localizable with DataFunctionProvider[Focus] {
  private val FOCUS_COST_FACTOR = 7
  private val DEFAULT_FOCUS_COST = 10.0

  /* attributes */
  val id: StringPDX = new StringPDX("id")
  val icon: MultiPDX[Icon] = new MultiPDX(Some(() => new SimpleIcon()), Some(() => new BlockIcon()), "icon")
  val x: IntPDX = new IntPDX("x") // if relative, relative x
  val y: IntPDX = new IntPDX("y") // if relative, relative y
  val prerequisites: MultiPDX[PrerequisiteSet] = new MultiPDX(None, Some(() => new PrerequisiteSet(() => focusTree.focuses)), "prerequisite")
  val mutuallyExclusive: MultiPDX[MutuallyExclusiveSet] = new MultiPDX(None, Some(() => new MutuallyExclusiveSet(() => focusTree.focuses)), "mutually_exclusive")
  val relativePosition = new ReferencePDX[Focus](() => focusTree.focuses, f => f.id.get(), "relative_position_id")
  val cost: DoublePDX = new DoublePDX("cost")
  val availableIfCapitulated: BooleanPDX = new BooleanPDX("available_if_capitulated", false, BoolType.YES_NO)
  val cancelIfInvalid: BooleanPDX = new BooleanPDX("cancel_if_invalid", true, BoolType.YES_NO)
  val continueIfInvalid: BooleanPDX = new BooleanPDX("continue_if_invalid", false, BoolType.YES_NO)
  var ddsImage: Image = _

  val completionReward: CompletionReward = new CompletionReward()

//  obj.addAll(childScripts)  // todo: garbage

  def this(focusTree: FocusTree, node: Node) = {
    this(focusTree)
    loadPDX(node)
  }

  override protected def childScripts: mutable.Iterable[PDXScript[?]] = {
    ListBuffer(id, icon, x, y, prerequisites, mutuallyExclusive, relativePosition, cost, availableIfCapitulated, cancelIfInvalid, continueIfInvalid)
  }

  def absoluteX: Int = absolutePosition.x

  def absoluteY: Int = absolutePosition.y

  def position: Point = new Point(x.getOrElse(0), y.getOrElse(0))

  def absolutePosition: Point = {
    if (relativePosition.isUndefined) {
      return position
    }
    if (relativePosition.equals(id)) {
      System.err.println("Relative position id same as focus id for " + this)
      return position
    }

    val relativePositionFocus = relativePosition.get()
    relativePositionFocus match {
      case Some(f) =>
        var adjPoint = f.absolutePosition
        adjPoint = new Point(adjPoint.x + x.getOrElse(0), adjPoint.y + y.getOrElse(0))
        adjPoint
      case None =>
        System.err.println("focus id " + relativePosition.getReferenceName + " not a focus")
        position
    }
  }

  override def toString: String = {
    id.get() match {
      case Some(id) => id
      case None => super.toString
    }
  }

  def setXY(x: Int, y: Int): Point = {
    val prev = new Point(this.x.getOrElse(0), this.y.getOrElse(0))
    this.x.set(x)
    this.y.set(y)
    prev
  }

  def setAbsoluteXY(x: Int, y: Int): Point = {
    val prev = new Point(this.x.getOrElse(0), this.y.getOrElse(0))
    this.x.set(x)
    this.y.set(y)
    this.relativePosition.setNull()
    prev
  }

  def setXY(xy: Point): Point = setXY(xy.x, xy.y)

  def setCost(): Unit = setCost(DEFAULT_FOCUS_COST)

  def setCost(cost: Number): Unit = {
    this.cost.setNode(cost.doubleValue())
  }

  def getDDSImage: Image = ddsImage

  def hasPrerequisites: Boolean = !(prerequisites.isUndefined || prerequisites.isEmpty)

  def isMutuallyExclusive: Boolean = !(mutuallyExclusive.isUndefined || mutuallyExclusive.isEmpty)

  def displayedCompletionTime(): Double = Math.floor(preciseCompletionTime())

  def preciseCompletionTime(): Double = cost.getOrElse(DEFAULT_FOCUS_COST) * FOCUS_COST_FACTOR

  override def getLocalizableProperties: mutable.Map[Property, String] = {
    mutable.Map(Property.NAME -> id.get().get, Property.DESCRIPTION -> s"${id.get().get}_desc")
  }

  override def getLocalizableGroup: Iterable[Localizable] = {
    if (focusTree == null) {
      Iterable(this)
    } else {
      focusTree.getLocalizableGroup
    }
  }

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

  class SimpleIcon() extends StringPDX("icon") with Icon {
  }

  class BlockIcon() extends StructuredPDX("icon") with Icon {

    final private val value: StringPDX = new StringPDX("value")

    override protected def childScripts: Iterable[? <: PDXScript[?]] = Iterable(value)

    override def equals(other: PDXScript[?]): Boolean = {
      other match {
        case icon: Icon => value.equals(icon.get())
        case _ => false
      }
    }
  }

  class CompletionReward extends CollectionPDX[Effect]("completion_reward") {
    @throws[UnexpectedIdentifierException]
    override def loadPDX(expression: Node): Unit = {
      super.loadPDX(expression)
    }

    override protected def newChildScript(expression: Node): Effect = {
      null.asInstanceOf[Effect] // todo fix
    }
  }

}

object Focus {
  def getDataFunctions: Iterable[Function[Focus, ?]] = {
    val dataFunctions = ListBuffer[Function[Focus, ?]]()
    dataFunctions += (focus => focus.id.get())
    dataFunctions += (focus => focus.localizationText(Property.NAME))
    dataFunctions += (focus => focus.localizationText(Property.DESCRIPTION))
    dataFunctions
  }

//  def schema = new PDXSchema()
}
