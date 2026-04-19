package com.hoi4utils.hoi42.common.national_focus

import com.hoi4utils.script2.{IDReferable, PDXDecoder, PDXEntity, PDXProperty, Reference, Registry, RegistryMember}
import com.hoi4utils.Point
import com.hoi4utils.script2.PDXPropertyValueExtensions.*
import com.typesafe.scalalogging.LazyLogging

import scala.annotation.tailrec

class Focus(var focusTree: FocusTree) extends PDXEntity with IDReferable[String] with RegistryMember[Focus](focusTree) with LazyLogging:
  val DEFAULT_COST: Double = 10.0

  /* attributes */
  val id    = pdx[String]("id") required true
  val icon  = pdx[Icon]("icon")
  /** If relative positioning, relative x */
  val x     = pdx[Int]("x")
  /** If relative positioning, relative y */
  val y     = pdx[Int]("y")
  val cost  = pdx[Double]("cost") default DEFAULT_COST
  val prerequisites = pdxList[PrerequisiteSet]("prerequisite")
  val mutuallyExclusive = pdxList[MutuallyExclusiveSet]("mutually_exclusive")
  val relativePositionFocus = pdx[Reference[Focus]]("relative_position_id")
  val availableIfCapitulated = pdx[Boolean]("available_if_capitulated")
  val cancelIfInvalid = pdx[Boolean]("cancel_if_invalid") default true
  val continueIfInvalid = pdx[Boolean]("continue_if_invalid")
  val aiWillDo = pdxList[AIWillDo]("ai_will_do")


  override def idProperty: PDXProperty[String] = id

  def absoluteX: Int = absolutePosition.x
  def absoluteY: Int = absolutePosition.y

  def relativePosition: Point[Int] = Point[Int](x getOrElse 0, y getOrElse 0)

  /**
   * Calculates and returns the absolute position of the focus, taking into account any relative positioning.
   * @return The absolute position of the focus, as it would be rendered on the focus tree.
   */
  def absolutePosition: Point[Int] = {
    /**
     * Recursively calculate the absolute position of a focus, taking into account relative positions.
     *
     * @param focus     the focus to calculate the absolute position of
     * @param visited   set of focus ids that have been visited to detect circular references
     * @param offsetAcc accumulated point adjustment
     * @return the absolute position of the focus
     */
    @tailrec
    def absolutePosition(focus: Focus, visited: Set[String] = Set.empty, offsetAcc: Point[Int] = Point(0, 0)): Point[Int] = {
      val nextPoint = Point(focus.x + offsetAcc.x, focus.y + offsetAcc.y)

      if focus.relativePositionFocus.isUndefined then
        nextPoint
      else if focus.relativePositionFocus.$id @== focus.id then
        // self-reference detected
        logger.error(s"Relative position id same as focus id for $this")
        nextPoint
      else if focus.id.exists(visited) then
        // circular reference detected
        logger.error(s"Circular reference detected involving focus id: ${id.display} in focus tree ${focusTree.id.display}")
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

object Focus { }

class FocusRegistry extends Registry[Focus] {

  override def idDecoder: PDXDecoder[Int] = summon[PDXDecoder[Int]]
}

class Icon(var spriteID: String) extends PDXEntity:
  // You can later add a reference to the actual Image/Texture
  // once your GFX alias resolver is built.
  override def toString: String = spriteID

/**
 * prerequisite = { focus = focus_id }
 */
class PrerequisiteSet(using Registry[Focus]) extends PDXEntity:
  val focus = pdx[Reference[Focus]]("focus")

/**
 * mutually_exclusive = { focus = focus_id }
 */
class MutuallyExclusiveSet(using Registry[Focus]) extends PDXEntity:
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

