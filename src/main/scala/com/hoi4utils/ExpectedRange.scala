package com.hoi4utils

class ExpectedRange[T <: AnyVal](val min: T, val max: T):

  def apply(start: T, end: T): ExpectedRange[T] = new ExpectedRange[T](start, end)

  def minNonInfinite: T = min match
    case min: Double =>
      if min.isInfinite then Double.MinValue.asInstanceOf[T]
      else min
    case _ => min

  def maxNonInfinite: T = max match
    case max: Double =>
      if max.isInfinite then Double.MaxValue.asInstanceOf[T]
      else max
    case _ => max

object ExpectedRange:
  def ofInt: ExpectedRange[Int] = new ExpectedRange[Int](Int.MinValue, Int.MaxValue)

  def ofPositiveInt: ExpectedRange[Int] = new ExpectedRange[Int](0, Int.MaxValue)

  def ofDouble: ExpectedRange[Double] = new ExpectedRange[Double](Double.MinValue, Double.MaxValue)

  def ofPositiveDouble: ExpectedRange[Double] = new ExpectedRange[Double](0, Double.MaxValue)

  def ofInfinite: ExpectedRange[Double] = new ExpectedRange[Double](Double.NegativeInfinity, Double.PositiveInfinity)

  def ofPositiveInfinite: ExpectedRange[Double] = new ExpectedRange[Double](0, Double.PositiveInfinity)

  def ofNegativeInfinite: ExpectedRange[Double] = new ExpectedRange[Double](Double.NegativeInfinity, 0)