package com.hoi4utils.custom_scala

sealed abstract class BoolType(val trueResponse: String, val falseResponse: String):
  def matches(value: String): Boolean = value.equalsIgnoreCase(trueResponse) || value.equalsIgnoreCase(falseResponse)

  def parse(value: String): Boolean =
    if value.equalsIgnoreCase(trueResponse) then true
    else if value.equalsIgnoreCase(falseResponse) then false
    else null.asInstanceOf[Boolean]


object BoolType:
  case object TRUE_FALSE extends BoolType("true", "false")
  case object YES_NO extends BoolType("yes", "no")

  val values: List[BoolType] = List(TRUE_FALSE, YES_NO)