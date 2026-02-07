package com.hoi4utils.hoi42.common.national_focus

import com.hoi4utils.script2.PDXEntity

class Focus(var focusTree: FocusTree) extends PDXEntity:
//  var id    by pdx[String]("id").required
//  var icon: Option[String],
//  var x: Int, y: Int,
//  var cost: Double,
//  var icon: Option[String],
//  var prerequisites: List[List[String]] = Nil, // List of mutually inclusive sets
//  var mutuallyExclusive: List[String] = Nil,
//  var relativePositionFocus: Option[String] = None,
//  var availableIfCapitulated: Boolean = false,
//  var cancelIfInvalid: Boolean = true,
//  var continueIfInvalid: Boolean = false,
//  var aiWillDo: Option[AIWillDo] = None,

  val id    = pdx[String]("id") required true
  val x     = pdx[Int]("x") default 0
  val y     = pdx[Int]("y")
  val cost  = pdx[Double]("cost") default 10.0
//  var icon by pdx[Icon]("icon").as(IconParser)

