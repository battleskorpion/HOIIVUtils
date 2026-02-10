package com.hoi4utils.hoi42.common.national_focus

import com.hoi4utils.script2.{PDXEntity, PDXProperty, Referable, Reference}

class Focus(var focusTree: FocusTree) extends PDXEntity with Referable[String]:
  val DEFAULT_COST: Double = 10.0

  /* attributes */
  val id    = pdx[String]("id") required true
  val icon  = pdx[Icon]("icon")
  /** If relative positioning, relative x */
  val x     = pdx[Int]("x")
  /** If relative positioning, relative y */
  val y     = pdx[Int]("y")
  val cost  = pdx[Double]("cost") default DEFAULT_COST
  val prerequisites = pdx[PrerequisiteSet]("prerequisite")
  val mutuallyExclusive = pdx[MutuallyExclusiveSet]("mutually_exclusive")
  val relativePositionFocus = pdx[Reference[Focus]]("relative_position_id")
  val availableIfCapitulated = pdx[Boolean]("available_if_capitulated")
  val cancelIfInvalid = pdx[Boolean]("cancel_if_invalid") default true
  val continueIfInvalid = pdx[Boolean]("continue_if_invalid")
  val aiWillDo = pdx[AIWillDo]("ai_will_do")


  override def idProperty: PDXProperty[String] = id


class Icon(var spriteID: String) extends PDXEntity:
  // You can later add a reference to the actual Image/Texture
  // once your GFX alias resolver is built.
  override def toString: String = spriteID

/**
 * prerequisite = { focus = focus_id }
 */
class PrerequisiteSet(tree: FocusTree) extends PDXEntity:
  val focus = pdx[Reference[Focus]]("focus")

/**
 * mutually_exclusive = { focus = focus_id }
 */
class MutuallyExclusiveSet(tree: FocusTree) extends PDXEntity:
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

