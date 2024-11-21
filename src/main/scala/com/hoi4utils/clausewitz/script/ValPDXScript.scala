package com.hoi4utils.clausewitz.script

import com.hoi4utils.ExpectedRange

import scala.annotation.targetName

trait ValPDXScript[T <: AnyVal] extends PDXScript[T] {
  def isDefaultRange: Boolean

  def defaultRange: ExpectedRange[T]

  def minValue: T

  def maxValue: T

  def minValueNonInfinite: T

  def maxValueNonInfinite: T

  def defaultValue: T

  @targetName("getEquals")
  def @==(other: T): Boolean = get() match {
    case Some(value) => value == other
    case None => false
  }

  @targetName("getNotEquals")
  def @!=(other: T): Boolean = !(this @== other)

  def @=(other: T): Unit = set(other)

  def @=(other: PDXScript[T]): Unit = other.get() match {
    case Some(value) => set(value)
    case None => setNull()
  }

  @targetName("plus")
  def +(other: T): T

  @targetName("minus")
  def -(other: T): T

  @targetName("multiply")
  def *(other: T): T

  @targetName("divide")
  def /(other: T): T

  @targetName("plusEquals")
  def +=(other: T): T = set(this + other)

  @targetName("minusEquals")
  def -=(other: T): T = set(this - other)

  @targetName("multiplyEquals")
  def *=(other: T): T = set(this * other)

  @targetName("divideEquals")
  def /=(other: T): T = set(this / other)

}
