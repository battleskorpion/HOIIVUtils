package com.hoi4utils.clausewitz.data.focus

import com.hoi4utils.clausewitz.DataFunctionProvider
import com.hoi4utils.clausewitz.localization.Localizable
import com.hoi4utils.clausewitz.script._
import com.hoi4utils.clausewitz_parser.Node
import com.hoi4utils.clausewitz.BoolType
import java.awt.Point

import java.util
import java.util._
import java.util.function.Supplier

import javafx.scene.image.Image

class Focus(var focusTree: FocusTree) extends StructuredPDX with Localizable with Comparable[Focus] with DataFunctionProvider[Focus] {
  private val FOCUS_COST_FACTOR = 7
  private val DEFAULT_FOCUS_COST = 10.0

  /* attributes */
  val id: StringPDX = new StringPDX("id")
  val icon: MultiPDX[Icon] = new MultiPDX(() => new Icon(), "icon")
  val x: IntegerPDX = new IntegerPDX("x") // if relative, relative x
  val y: IntegerPDX = new IntegerPDX("y") // if relative, relative y
  val prerequisites: MultiPDX[PrerequisiteSet] = new MultiPDX(() => new PrerequisiteSet(() => focusTree.focuses), "prerequisite")
  val mutuallyExclusive: MultiPDX[MutuallyExclusiveSet] = new MultiPDX(() => new MutuallyExclusiveSet(() => focusTree.focuses), "mutually_exclusive")
  val relativePosition = new ReferencePDX[Focus](() => focusTree.focuses, (f: Focus) => f.id.get(), "relative_position_id")
  val cost: DoublePDX = new DoublePDX("cost")
  val availableIfCapitulated: BooleanPDX = new BooleanPDX("available_if_capitulated", false, BoolType.YES_NO)
  val cancelIfInvalid: BooleanPDX = new BooleanPDX("cancel_if_invalid", true, BoolType.YES_NO)
  val continueIfInvalid: BooleanPDX = new BooleanPDX("continue_if_invalid", false, BoolType.YES_NO)
  var ddsImage: Image = uninitialized

  val completionReward: CompletionReward = new CompletionReward()

  obj.addAll(childScripts)

  def this(focusTree: FocusTree, node: Node) = {
    this(focusTree)
    loadPDX(node)
  }

  override protected def childScripts: Seq[PDXScript[_]] = {
    Seq(id, icon, x, y, prerequisites, mutuallyExclusive, relativePosition, cost, availableIfCapitulated, cancelIfInvalid, continueIfInvalid)
  }

  def absoluteX(): Int = absolutePosition().x

  def absoluteY(): Int = absolutePosition().y

  def position(): Point = new Point(x.getOrElse(0), y.getOrElse(0))

  def absolutePosition(): Point = {
    if (relativePosition.isUndefined) {
      return position()
    }
    if (relativePosition.objEquals(id)) {
      System.err.println("Relative position id same as focus id for " + this)
      return position()
    }

    val relativePositionFocus = relativePosition.get()
    if (relativePositionFocus == null) {
      System.err.println("focus id " + relativePosition.getReferenceName + " not a focus")
      return position()
    }
    var adjPoint = relativePositionFocus.absolutePosition()
    adjPoint = new Point(adjPoint.x + x.getOrElse(0), adjPoint.y + y.getOrElse(0))

    adjPoint
  }

  override def toString: String = id.get()

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
    this.cost.set(cost.doubleValue())
  }

  def getDDSImage: Image = ddsImage

  def hasPrerequisites: Boolean = !(prerequisites.isUndefined || prerequisites.isEmpty)

  def isMutuallyExclusive: Boolean = !(mutuallyExclusive.isUndefined || mutuallyExclusive.isEmpty)

  def displayedCompletionTime(): Double = Math.floor(preciseCompletionTime())

  def preciseCompletionTime(): Double = cost.getOrElse(DEFAULT_FOCUS_COST) * FOCUS_COST_FACTOR

  override def getLocalizableProperties: java.util.Map[Property, String] = {
    Map(Property.NAME -> id.get(), Property.DESCRIPTION -> s"${id.get()}_desc")
  }

  override def getLocalizableGroup: Seq[Localizable] = {
    if (focusTree == null) {
      Seq(this)
    } else {
      focusTree.getLocalizableGroup
    }
  }

  def toScript: String = {
    val details = new StringBuilder()
    for (property <- childScripts) {
      val text = property.toScript
      if (text != null) {
        details.append(text)
      }
    }
    details.toString()
  }

  def getCompletionReward: util.List[Effect] = completionReward

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
    setCompletionRewardsOfNode(completionRewardNode, Scope.of(focusTree.country.get()))
  }

  private def setCompletionRewardsOfNode(completionRewardNode: Node, scope: Scope): Unit = {
    for (n <- completionRewardNode.value().list()) {
      if (n.value().isList) {
        var s: Scope = null
        if (CountryTagsManager.exists(n.name())) {
          s = Scope.of(CountryTagsManager.get(n.name()))
        } else {
          try {
            s = Scope.of(n.name(), scope)
          } catch {
            case e: NotPermittedInScopeException =>
              println(e.getMessage)
          }
        }

        if (s == null || s.scopeCategory == ScopeCategory.EFFECT) {
          var effect: Effect = null
          try {
            effect = Effect.of(n.name(), scope, n.value())
          } catch {
            case e: InvalidEffectParameterException =>
              println(e.getMessage)
            case e: NotPermittedInScopeException =>
              println(e.getMessage + ", scope: " + scope + ", list? " + n.name())
          }
          if (effect != null) {
            completionReward += effect
          } else {
            println("Scope " + n.name() + " unknown.")
          }
        } else {
          setCompletionRewardsOfNode(n, s)
        }
      } else if (!n.valueIsNull) {
        var effect: Effect = null
        try {
          effect = Effect.of(n.name(), scope, n.value())
        } catch {
          case e: InvalidEffectParameterException =>
            println(e.getMessage)
          case e: NotPermittedInScopeException =>
            println(e.getMessage + ", scope: " + scope)
        }
        if (effect == null) {
          System.err.println("effect not found: " + n.name())
        } else {
          if (effect.hasSupportedTargets) {
            try {
              effect.setTarget(n.value().string(), scope)
            } catch {
              case e: IllegalStateException =>
                e.printStackTrace()
            }
          }
          completionReward += effect
        }
      } else {
        var effect: Effect = null
        try {
          effect = Effect.of(n.name(), scope)
        } catch {
          case e: NotPermittedInScopeException =>
            println(e.getMessage)
        }
        if (effect != null) {
          completionReward += effect
        }
      }
    }
  }

  class PrerequisiteSet(referenceFocusesSupplier: Supplier[util.Collection[Focus]]) extends MultiReferencePDX[Focus]
  (referenceFocusesSupplier, (f: Focus) => f.id.get(), "prerequisite", "focus") {
    def this() = {
      this(() => focusTree.focuses)
    }
  }

  /**
   * mutually exclusive is a multi-reference of focuses
   */
  class MutuallyExclusiveSet(referenceFocusesSupplier: Supplier[util.Collection[Focus]]) extends MultiReferencePDX[Focus]
  (referenceFocusesSupplier, (f: Focus) => f.id.get(), "mutually_exclusive", "focus") {
  }

  class Icon extends DynamicPDX[String, StructuredPDX](() =>
    new StringPDX("icon"),
    new StructuredPDX("icon") {
      final private val value: StringPDX = new StringPDX("value")

      override protected def childScripts: util.Collection[? <: PDXScript[?]] = util.List.of(value)

      override def nodeEquals(other: PDXScript[_]): Boolean = {
        other match {
          case icon: Icon => value.nodeEquals(icon.get())
          case _ => false
        }
      }
    },
    "value")
    {
  }

  class CompletionReward extends CollectionPDXScript[Effect[?]]("completion_reward") {
    @throws[UnexpectedIdentifierException]
    override def loadPDX(expression: Node): Unit = {
      super.loadPDX(expression)
    }

    override protected def newChildScript(expression: Node): Effect[?] = {
    }
  }

}

object Focus {
  def getDataFunctions: Seq[Function[Focus, ?]] = {
    val dataFunctions = mutable.ListBuffer[Function[Focus, ?]]()
    dataFunctions += (focus => focus.id.get())
    dataFunctions += (focus => focus.localizationText(Property.NAME))
    dataFunctions += (focus => focus.localizationText(Property.DESCRIPTION))
    dataFunctions
  }

//  def schema = new PDXSchema()
}
