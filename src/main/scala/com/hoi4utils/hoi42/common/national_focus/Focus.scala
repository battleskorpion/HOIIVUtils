package com.hoi4utils.hoi42.common.national_focus

import com.hoi4utils.script2.{IDReferable, PDXDecoder, PDXDecoderException, PDXEntity, PDXProperty, Reference, Registry, RegistryMember}
import com.hoi4utils.{IntPoint, Point}
import com.hoi4utils.hoi4.localization.{HasDesc, Localizable, Property}
import com.hoi4utils.parser.NodeValueType
import com.hoi4utils.script2.PDXPropertyValueExtensions.*
import com.typesafe.scalalogging.LazyLogging

import scala.annotation.tailrec
import scala.collection.mutable.ListBuffer

class Focus(var focusRegistry: FocusRegistry[?]) extends PDXEntity with IDReferable[String] with RegistryMember[Focus](focusRegistry)
  with Localizable with HasDesc with LazyLogging:
  val DEFAULT_COST: Double = 10.0

  /* attributes */
  val id    = pdx[String]("id") required true
  val icon  = pdx[Icon]("icon")
  /** If relative positioning, relative x */
  val x     = pdx[Int]("x") required true
  /** If relative positioning, relative y */
  val y     = pdx[Int]("y") required true
  val cost  = pdx[Double]("cost") default DEFAULT_COST
  val prerequisites = pdxList[PrerequisiteSet]("prerequisite")
  val mutuallyExclusive = pdxList[MutuallyExclusiveSet]("mutually_exclusive")
  val relativePositionFocus = pdx[Reference[Focus]]("relative_position_id")
  val availableIfCapitulated = pdx[Boolean]("available_if_capitulated") default false
  val cancelIfInvalid = pdx[Boolean]("cancel_if_invalid") default true
  val continueIfInvalid = pdx[Boolean]("continue_if_invalid") default false
  val aiWillDo = pdxList[AIWillDo]("ai_will_do")


  override def idProperty: PDXProperty[String] = id

  def absoluteX: Int = absolutePosition.x
  def absoluteY: Int = absolutePosition.y

  def relativePosition: IntPoint = Point(x getOrElse 0, y getOrElse 0)

  /**
   * Calculates and returns the absolute position of the focus, taking into account any relative positioning.
   * @return The absolute position of the focus, as it would be rendered on the focus tree.
   */
  def absolutePosition: IntPoint = {
    /**
     * Recursively calculate the absolute position of a focus, taking into account relative positions.
     *
     * @param focus     the focus to calculate the absolute position of
     * @param visited   set of focus ids that have been visited to detect circular references
     * @param offsetAcc accumulated point adjustment
     * @return the absolute position of the focus
     */
    @tailrec
    def absolutePosition(focus: Focus, visited: Set[String] = Set.empty, offsetAcc: IntPoint = Point(0, 0)): IntPoint = {
      val nextPoint = Point(focus.x + offsetAcc.x, focus.y + offsetAcc.y)

      if focus.relativePositionFocus.isUndefined then
        nextPoint
      else if focus.relativePositionFocus.$id @== focus.id then
        // self-reference detected
        logger.error(s"Relative position id same as focus id for $this")
        nextPoint
      else if focus.id.exists(visited) then
        // circular reference detected
        focusRegistry match
          case tree: FocusTree =>
            logger.error(s"Circular reference detected involving focus id: ${id.display} in focus tree ${tree.id.display}")
          case _ =>
            logger.error(s"Circular reference detected involving focus id: ${id.display} in focus registry $focusRegistry")
        nextPoint
      else focus.relativePositionFocus.resolve match
        case Some(relativeFocus: Focus) =>
          // Tail call: pass nextFocus, the updated visited set, and the new accumulated offset.
          absolutePosition(relativeFocus, visited + focus.id.$, nextPoint)
        case None =>
          logger.error(s"Focus id ${focus.relativePositionFocus.$id} not a valid focus")
          nextPoint
    }

    absolutePosition(this)
  }

  def setXY(x: Int, y: Int): IntPoint =
    this.x @= x
    this.y @= y
    relativePosition

  def setXY(xy: IntPoint): IntPoint = setXY(xy.x, xy.y)

  /**
   * Set the absolute x and y coordinates of the focus. If the focus has a relative position focus, it remains relative to
   * that position, but its absolute coordinates are always the same.
   *
   * @param newPos                     absolute x- and y-coordinates
   * @param updateChildRelativeOffsets if true, update descendant relative focus positions by some offset so that they remain
   *                                   in the same position even though the position of this focus changes
   * @return the previous absolute position
   */
  def setAbsoluteXY(newPos: IntPoint, updateChildRelativeOffsets: Boolean): IntPoint =
    val prevAbsolute = absolutePosition

    val deltas = newPos - prevAbsolute
    // If there is no position change, nothing to do
    if !deltas.isZero then
      relativePositionFocus.resolve match
        case Some(f) =>
          // keep relative to the focus, but absolute coordinates are always the same
          val relPos = f.absolutePosition
          setXY(newPos - relPos)
        case None =>
          // No relative positioning, so just set directly
          setXY(newPos)
      if updateChildRelativeOffsets then
        // Update focuses that has us as its relative position parent
        for
          focus <- focusRegistry.focusesList
          if focus.relativePositionFocus.isDefined
          if focus.relativePositionFocus.$id @== this.id
        do
          focus.offsetXY(deltas)

    prevAbsolute

  def offsetXY(offset: IntPoint): IntPoint =
    this.x += offset.x
    this.y += offset.y
    relativePosition

  /**
   * Check if the focus is at the given relative position.
   * @param x relative x coordinate
   * @param y relative y coordinate
   * @return
   */
  def hasRelativePosition(pos: IntPoint): Boolean = (this.x @== pos.x) && (this.y @== pos.y)

  /**
   * Check if the focus is at the given absolute position.
   *
   * @param x absolute x-coordinate
   * @param y absolute y-coordinate
   * @return
   */
  def hasAbsolutePosition(pos: IntPoint): Boolean = absolutePosition == pos

  def selfAndRelativePositionedFocuses: List[Focus] =
    val focuses = ListBuffer[Focus]()
    focuses += this

    @tailrec
    def gatherRelativeFocuses(currentFocuses: List[Focus]): Unit =
      val newlyFoundFocuses = ListBuffer[Focus]()
      for
        focus <- focusRegistry.focusesList
        currentFocus <- currentFocuses
        if focus.relativePositionFocus.isDefined
        if (focus.relativePositionFocus.$id @== currentFocus.id) && !focuses.contains(focus)
      do
        focuses += focus
        newlyFoundFocuses += focus
      if newlyFoundFocuses.nonEmpty then
        gatherRelativeFocuses(newlyFoundFocuses.toList)

    gatherRelativeFocuses(List(this))
    focuses.toList

  override def localizableProperties: Map[Property, String] =
    Map(Property.NAME -> this.id.getOrElse(""), Property.DESCRIPTION -> s"${id}_desc")

  override def getLocalizableGroup: Iterable[Localizable] =
    if focusRegistry == null then
      Iterable(this)
    else
      focusRegistry.getLocalizableGroup

object Focus { }

trait FocusRegistry[F <: Focus] extends Registry[F] {

  override def idDecoder: PDXDecoder[String] = summon[PDXDecoder[String]]

  def focusesList: List[F] = referableEntities.toList

  /**
   * @inheritdoc
   *
   * The localizable group for a focus registry is the list of focuses.
   */
  def getLocalizableGroup: Iterable[? <: Localizable] = focusesList

}

class Icon(var spriteID: String) extends PDXEntity:

  val spriteValue: PDXProperty[String] = pdx[String]("value")

  def getSpriteID: String = spriteValue.value.getOrElse(spriteID)

  // You can later add a reference to the actual Image/Texture
  // once your GFX alias resolver is built.
  override def toString: String = spriteID

object Icon {
  given PDXDecoder[Icon] with
    override def decode(v: NodeValueType): Either[String, Icon] = v match
      case s: String =>
        // Handles: icon = "GFX_my_icon" or icon = GFX_my_icon
        Right(new Icon(s))

      case i: Icon =>
        // Handles: icon = { spriteID = "GFX_my_icon" }
        // (PDXLoader already instantiated Icon via createEmpty and populated its properties)
        Right(i)

      case _ =>
        Left(s"Expected String or Icon entity block, got ${v.getClass.getSimpleName}")

    override def createEmpty(context: Any): Option[Icon] =
      val clazz = Icon.getClass
      val instance = clazz.getConstructors.find { c =>
        c.getParameterTypes.exists(_.isAssignableFrom(context.getClass))
      } match
        case Some(c) => c.newInstance(context)
        case None =>
          try
            clazz.getConstructor().newInstance()
          catch
            case e: NoSuchMethodException => throw PDXDecoderException(s"There is no constructor for ${clazz.getSimpleName} which supports $context")

      Some(instance.asInstanceOf[Icon])

}

/**
 * prerequisite = { focus = focus_id }
 */
class PrerequisiteSet(using Registry[Focus]) extends PDXEntity:
  val focus = pdx[Reference[Focus]]("focus")

/**
 * mutually_exclusive = { focus = focus_id }
 */
class MutuallyExclusiveSet(using Registry[Focus]) extends PDXEntity:
  // todo ???
  val focus = pdx[Reference[Focus]]("focus")

class AIWillDo() extends PDXEntity:
  val base = pdx[Double]("base")
  val factor = pdx[Double]("factor")
  val add = pdx[Double]("add")
  val modifier = pdx[AIWillDoModifier]("modifier")

class AIWillDoModifier() extends PDXEntity:
  val factor = pdx[Double]("factor")
  val add    = pdx[Double]("add")
  // TODO add triggers/conditions here

