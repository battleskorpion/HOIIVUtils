package com.hoi4utils.custom_scala

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

  def ofPositiveInt(min: Int): ExpectedRange[Int] = new ExpectedRange[Int](-1, Int.MaxValue)

  def ofDouble: ExpectedRange[Double] = new ExpectedRange[Double](Double.MinValue, Double.MaxValue)

  def ofPositiveDouble: ExpectedRange[Double] = new ExpectedRange[Double](0, Double.MaxValue)

  def ofPositiveDouble(min: Double): ExpectedRange[Double] = new ExpectedRange[Double](-1, Double.MaxValue)

  def ofInfinite: ExpectedRange[Double] = new ExpectedRange[Double](Double.NegativeInfinity, Double.PositiveInfinity)

  def ofPositiveInfinite: ExpectedRange[Double] = new ExpectedRange[Double](0, Double.PositiveInfinity)

  def ofNegativeInfinite: ExpectedRange[Double] = new ExpectedRange[Double](Double.NegativeInfinity, 0)

  def ofPositiveInfinite(min: Double): ExpectedRange[Double] = new ExpectedRange[Double](min, Double.PositiveInfinity)

  def ofNegativeInfinite(max: Double): ExpectedRange[Double] = new ExpectedRange[Double](Double.NegativeInfinity, max)