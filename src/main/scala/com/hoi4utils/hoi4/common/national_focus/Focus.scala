package com.hoi4utils.hoi4.common.national_focus

import com.hoi4utils.shared.{BoolType, ExpectedRange}
import com.hoi4utils.databases.effect.{Effect, EffectDatabase}
import com.hoi4utils.ddsreader.DDSReader
import com.hoi4utils.exceptions.UnexpectedIdentifierException
import com.hoi4utils.hoi4.common.national_focus.FocusTreesManager.focusTreeErrors
import com.hoi4utils.hoi4.gfx.Interface
import com.hoi4utils.hoi4.localization.{HasDesc, Localizable, Localization, Property}
import com.hoi4utils.hoi4.scope.Scope
import com.hoi4utils.parser.Node
import com.hoi4utils.script.*
import com.hoi4utils.script.datatype.StringPDX
import com.hoi4utils.script.shared.AIWillDoPDX
import dotty.tools.sjs.ir.Trees.JSBinaryOp.&&
import dotty.tools.sjs.ir.Trees.JSUnaryOp.!
import javafx.scene.image.Image

import scala.annotation.tailrec
import scala.collection.mutable
import scala.collection.mutable.ListBuffer


@lombok.extern.slf4j.Slf4j
class Focus(var focusTree: FocusTree, node: Node = null) extends StructuredPDX("focus") with Localizable with HasDesc with Referable:

  private val FOCUS_COST_FACTOR = 7
  private val DEFAULT_FOCUS_COST = 10.0

  /* attributes */
  val id                      = StringPDX("id")
  val icon                    = MultiPDX(Some(() => new SimpleIcon()), Some(() => new BlockIcon()), "icon")
  /** If relative positioning, relative x */
  final val x                 = IntPDX("x")
  /** If relative positioning, relative y */
  final val y                 = IntPDX("y", ExpectedRange.ofPositiveInt)
  val prerequisites           = MultiPDX[PrerequisiteSet](None, Some(() => new PrerequisiteSet(() => focusTree.focuses)), "prerequisite")
  val mutuallyExclusive       = MultiPDX[MutuallyExclusiveSet](None, Some(() => new MutuallyExclusiveSet(() => focusTree.focuses)), "mutually_exclusive")
  val relativePositionFocus   = ReferencePDX[Focus](() => focusTree.focuses, "relative_position_id")
  val cost                    = DoublePDX("cost", ExpectedRange.ofPositiveInfinite(-1))
  val availableIfCapitulated  = BooleanPDX("available_if_capitulated")
  val cancelIfInvalid         = BooleanPDX("cancel_if_invalid", true)
  val continueIfInvalid       = BooleanPDX("continue_if_invalid")
  val ai_will_do              = AIWillDoPDX()
  /** completion reward */
  //  final val completionReward: CompletionReward = new CompletionReward()

  if node != null then loadPDX(node)

  /**
   * @inheritdoc
   */
  override protected def childScripts: mutable.Iterable[PDXScript[?]] =
    ListBuffer(id, icon, x, y, prerequisites, mutuallyExclusive, relativePositionFocus, cost,
      availableIfCapitulated, cancelIfInvalid, continueIfInvalid, ai_will_do)

  def absoluteX: Int = absolutePosition.x

  def absoluteY: Int = absolutePosition.y

  def relativePosition: Point = Point(x getOrElse 0, y getOrElse 0)

  def xyPoint: Point = relativePosition

  /**
   * Calculates and returns the absolute position of the focus, taking into account any relative positioning.
   * @return The absolute position of the focus, as it would be rendered on the focus tree.
   */
  def absolutePosition: Point = {
    /**
     * Recursively calculate the absolute position of a focus, taking into account relative positions.
     *
     * @param focus     the focus to calculate the absolute position of
     * @param visited   set of focus ids that have been visited to detect circular references
     * @param offsetAcc accumulated point adjustment
     * @return the absolute position of the focus
     */
    @tailrec
    def absolutePosition(focus: Focus, visited: Set[String] = Set.empty, offsetAcc: Point = Point(0, 0)): Point = {
      val nextPoint = Point(focus.x + offsetAcc.x, focus.y + offsetAcc.y)
      if focus.relativePositionFocus.isUndefined then
        return nextPoint
      // Check for self-reference
      if focus.relativePositionFocus @== focus.id then
        logger.error(s"Relative position id same as focus id for $this")
        return nextPoint
      // Check for circular references
      if visited(focus.id.str) then
        logger.error(s"Circular reference detected involving focus id: ${id.str} in file ${focusTree.focusFile}")
        return nextPoint

      focus.relativePositionFocus.value match
        case Some(relativeFocus: Focus) =>
          // Tail call: pass nextFocus, the updated visited set, and the new accumulated offset.
          absolutePosition(relativeFocus, visited + focus.id.str, nextPoint)
        case None =>
          logger.error(s"Focus id ${focus.relativePositionFocus.getReferenceName} not a valid focus")
          nextPoint
    }

    absolutePosition(this)
  }

  override def toString: String =
    id.value match
      case Some(id) => id
      case None => "[Unknown]"

  def setID(s: String): Unit =
    this.id.set(s)

  def setXY(x: Int, y: Int): Point =
    this.x @= x
    this.y @= y
    relativePosition

  def setXY(xy: Point): Point = setXY(xy.x, xy.y)

  /**
   * Set the absolute x and y coordinates of the focus. If the focus has a relative position focus, it remains relative to
   * that position, but its absolute coordinates are always the same.
   *
   * @param x                          absolute x-coordinate
   * @param y                          absolute y-coordinate
   * @param updateChildRelativeOffsets if true, update descendant relative focus positions by some offset so that they remain
   *                                   in the same position even though the position of this focus changes
   * @return the previous absolute position
   */
  def setAbsoluteXY(x: Int, y: Int, updateChildRelativeOffsets: Boolean): Point =
    val prevAbsolute = absolutePosition
    val deltas = (x - prevAbsolute.x, y - prevAbsolute.y)
    if deltas == (0, 0) then
      // No position change, so nothing to do
      return prevAbsolute
    relativePositionFocus.value match
      case Some(f) =>
        // keep relative to the focus, but absolute coordinates are always the same
        val rp = f.absolutePosition
        setXY(x - rp.x, y - rp.y)
      case None =>
        // No relative positioning, so just set directly
        setXY(x, y)
    if updateChildRelativeOffsets then
      for focus <- focusTree.focuses do
        // Check if this focus has us as its relative position parent
        if focus.relativePositionFocus @== this then
          focus.offsetXY tupled deltas
    prevAbsolute

  /**
   * Set the absolute x and y coordinates of the focus. If the focus has a relative position focus, it remains relative to
   * that position, but its absolute coordinates are always the same.
   *
   * @param xy                         absolute x- and y-coordinates
   * @param updateChildRelativeOffsets if true, update descendant relative focus positions by some offset so that they remain
   *                                   in the same position even though the position of this focus changes
   * @return the previous absolute position
   */
  def setAbsoluteXY(xy: Point, updateChildRelativeOffsets: Boolean): Point =
    setAbsoluteXY(xy.x, xy.y, updateChildRelativeOffsets)

  def offsetXY(x: Int, y: Int): Point =
    this.x += x
    this.y += y
    xyPoint

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
  def hasAbsolutePosition(x: Int, y: Int): Boolean = absolutePosition == Point(x, y)

  def selfAndRelativePositionedFocuses: List[Focus] =
    val focuses = ListBuffer[Focus]()
    focuses += this

    @tailrec
    def gatherRelativeFocuses(currentFocuses: List[Focus]): Unit =
      val newlyFoundFocuses = ListBuffer[Focus]()
      for
        focus <- focusTree.focuses
        currentFocus <- currentFocuses
        if (focus.relativePositionFocus @== currentFocus) && !focuses.contains(focus)
      do
        focuses += focus
        newlyFoundFocuses += focus
      if newlyFoundFocuses.nonEmpty then
        gatherRelativeFocuses(newlyFoundFocuses.toList)

    gatherRelativeFocuses(List(this))
    focuses.toList

  def setCost(): Unit = setCost(DEFAULT_FOCUS_COST)

  def setCost(cost: Number): Unit =
    this.cost @= cost.doubleValue()

  def getDDSImage: Option[Image] =
    // bad code. it's fine for now.
    if icon.isDefined then
      var img: Option[Image] = None
      for i <- icon do i match
        case simpleIcon: SimpleIcon => simpleIcon.value match
          case Some(iconName) =>
            Interface.getGFX(iconName) match
              case Some(gfx) => img = Some(DDSReader.readDDSImage(gfx).get)
              case None => None
          case None => None
        case blockIcon: BlockIcon => blockIcon.iconName match
          case Some(iconName) =>
            Interface.getGFX(iconName) match
              case Some(gfx) => img = Some(DDSReader.readDDSImage(gfx).get)
              case None => None
          case None => None
        case _ => None
      img
    else None

  def hasPrerequisites: Boolean = prerequisites.nonEmpty

  def isMutuallyExclusive: Boolean = !(mutuallyExclusive.isUndefined || mutuallyExclusive.isEmpty)

  def displayedCompletionTime(): Double = Math.floor(preciseCompletionTime())

  def preciseCompletionTime(): Double = (cost getOrElse DEFAULT_FOCUS_COST) * FOCUS_COST_FACTOR

  override def getLocalizableProperties: mutable.Map[Property, String] =
    //mutable.Map(Property.NAME -> id.get().get, Property.DESCRIPTION -> s"${id.get().get}_desc")
    val id = this.id.getOrElse("")
    mutable.Map(Property.NAME -> id, Property.DESCRIPTION -> s"${id}_desc")

  override def getLocalizableGroup: Iterable[Localizable] =
    if focusTree == null then
      Iterable(this)
    else
      focusTree.getLocalizableGroup

  def isLocalized: Boolean = localizationStatus(Property.NAME) != Localization.Status.MISSING

  def isLocalized(property: Property): Boolean = localization(property) match
    case Some(_) => true
    case None => false

  def isUnlocalized(property: Property): Boolean = !isLocalized(property)

  //  def getCompletionReward: CompletionReward = completionReward

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

  private def setCompletionRewardsOfNode(completionRewardNode: Node): Unit =
    focusTree.countryTag match
      case Some(tag) =>
        setCompletionRewardsOfNode(completionRewardNode, Scope.of(tag))
      case None =>
        setCompletionRewardsOfNode(completionRewardNode, null)

  def mutuallyExclusiveList: List[Focus] = mutuallyExclusive.flatMap(_.references()).toList

  def prerequisiteList: List[Focus] = prerequisites.flatMap(_.references()).toList

  def prerequisiteSets: List[PrerequisiteSet] = prerequisites.toList

  private def setCompletionRewardsOfNode(completionRewardNode: Node, scope: Scope): Unit =
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
    ()

  override def referableID: Option[String] = id.value

  class PrerequisiteSet(referenceFocusesSupplier: () => Iterable[Focus] = () => focusTree.focuses)
    extends MultiReferencePDX[Focus](referenceFocusesSupplier, "prerequisite", "focus"):
    override def handleUnexpectedIdentifier(node: Node, exception: Exception): Unit =
      val fullMessage = s"""Prerequisite Set - Unexpected Identifier Error:
                           |	Exception: ${exception.getMessage}
                           |	Focus Tree ID: ${id.value getOrElse "undefined"}
                           |	Class: ${this.getClass.getSimpleName}
                           |	Node Identifier: ${node.identifier.getOrElse("none")}
                           |	Expected Identifiers: ${pdxIdentifiers.mkString("[", ", ", "]")}
                           |	Node Value: ${Option(node.$).map(_.toString) getOrElse "null"}
                           |	Node Type: ${Option(node.$).map(_.getClass.getSimpleName) getOrElse "null"}""".stripMargin

      focusTreeErrors += fullMessage

  /** mutually exclusive is a multi-reference of focuses */
  class MutuallyExclusiveSet(referenceFocusesSupplier: () => Iterable[Focus])
    extends MultiReferencePDX[Focus](referenceFocusesSupplier, "mutually_exclusive", "focus")

  trait Icon extends PDXScript[?]

  class SimpleIcon extends StringPDX("icon") with Icon

  class BlockIcon extends StructuredPDX("icon") with Icon:

    final private val `value`: StringPDX = new StringPDX("value")

    override protected def childScripts: mutable.Iterable[? <: PDXScript[?]] = ListBuffer(`value`)

    override def equals(other: PDXScript[?]): Boolean = other match
      case icon: Focus#Icon => `value`.equals(icon.value) // todo :(
      case _ => false

    def iconName: Option[String] = `value`.value

  // This is breaking my brain because the loadpdx call here is the most confusing to keep track of with it being a: inner class + override + super. + child child child class
  // like wtf or how the fuck am I suppose to figure out and clean it up so I can log errors and metadata without breaking the whole program like before
  // it so such tightly knitted rat nest of fuck ass object oriented code.
  // trait class and abstracts shouldn't handle exceptions / errors / user expected errors!
  //      cuz the generic code means its hard to get metadata, log, debug etc
  class CompletionReward extends CollectionPDX[Effect](EffectDatabase(), "completion_reward"):

    override def getPDXTypeName: String = "Completion Reward"

object Focus:
  def getDataFunctions: Iterable[Focus => ?] =
    def locOrMissing(p: Property): Focus => String =
      f => f.localizationText(p).getOrElse("[Localization missing]")

    List[Focus => ?](
      f => f.id.getOrElse(s"[Focus missing id, focus tree: ${f.focusTree}, file: ${f.focusTree.fileName}]"),
      locOrMissing(Property.NAME),
      locOrMissing(Property.DESCRIPTION)
    )

  def focusesWithPrerequisites(focuses: Iterable[Focus]): List[(Focus, List[Focus])] =
    focuses.filter(_.hasPrerequisites).map: f =>
      (f, f.prerequisiteSets.flatMap(_.references()))
    .toList

//  def schema = new PDXSchema()