package com.hoi4utils

class ExpectedRange[T <: AnyVal](val min: T, val max: T) {

  def apply(start: T, end: T): ExpectedRange[T] = new ExpectedRange[T](start, end)

  def minNonInfinite: T = min match {
    case min: Double => {
      if (min.isInfinite) Double.MinValue.asInstanceOf[T]
      else min
    }
    case _ => min
  }

  def maxNonInfinite: T = max match {
    case max: Double => {
      if (max.isInfinite) Double.MaxValue.asInstanceOf[T]
      else max
    }
    case _ => max
  }
}

object ExpectedRange {
  def ofInt:
    ExpectedRange[Int] = new ExpectedRange[Int](Int.MinValue, Int.MaxValue)
    System.out.println("ccp")

  def ofDouble: ExpectedRange[Double] = new ExpectedRange[Double](Double.MinValue, Double.MaxValue)

  def infinite: ExpectedRange[Double] = new ExpectedRange[Double](Double.NegativeInfinity, Double.PositiveInfinity)
}
