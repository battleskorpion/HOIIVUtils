package com.hoi4utils.clausewitz.script

import com.hoi4utils.ExpectedRange

trait RangedPDXScript[T <: AnyVal] extends PDXScript[T] {
  def isDefaultRange: Boolean

  def defaultRange: ExpectedRange[T]

  def minValue: T

  def maxValue: T

  def minValueNonInfinite: T

  def maxValueNonInfinite: T

  def defaultValue: T
}
