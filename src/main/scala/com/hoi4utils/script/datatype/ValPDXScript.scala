package com.hoi4utils.script.datatype

import com.hoi4utils.script.PDXScript
import com.hoi4utils.shared.ExpectedRange

import scala.annotation.targetName

/**
 * A PDX script that holds a value of type T.
 * Allows shi**y clausewitz engine numbers to be used in a more sane way.
 * @tparam T
 */
trait ValPDXScript[T <: AnyVal] extends PDXScript[T] with Comparable[T] {
  def isDefaultRange: Boolean

  def defaultRange: ExpectedRange[T]

  def minValue: T

  def maxValue: T

  def minValueNonInfinite: T

  def maxValueNonInfinite: T

  def defaultValue: T

  /**
   * Checks the value of the script is equal to the given value.
   * @param other
   * @return
   */
  @targetName("getEquals")
  def @==(other: T): Boolean = value match
    case Some(v) => v.equals(other)
    case None => false

  /**
   * Checks the value of the script is equal to the value of the given script.
   * @param other
   * @return
   */
  @targetName("getEquals")
  def @==(other: ValPDXScript[T]): Boolean = value match
    case Some(v) => other @== v
    case None => false

  /**
   * Checks if the value of the script is not equal to the given value.
   * @param other
   * @return
   */
  @targetName("getNotEquals")
  def @!=(other: T): Boolean = !(this @== other)

  /**
   * Sets the value of the script to the given value.
   * @param other
   */
  def @=(other: T): Unit = set(other)

  /**
   * Sets the value of the script to the value of the given script.
   *
   * @param other
   */
  def @=(other: PDXScript[T]): Unit = other.value match
    case Some(v) => set(v)
    case None => setNull()

  /**
   * Sets the value of the script to the given value. If the given value is the script's default value
   * and the script's value is empty, does nothing.
   */
  def @=?(other: T): Unit = {
    if (other == defaultValue && value.isEmpty) return
    set(other)
  }

  @targetName("unaryPlus")
  def unary_+ : T

  @targetName("unaryMinus")
  def unary_- : T

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

  override def compareTo(o: T): Int = value match
    case Some(v) => v match
      case i: Int => i.compareTo(o.asInstanceOf[Int])
      case d: Double => d.compareTo(o.asInstanceOf[Double])
      case b: Boolean => b.compareTo(o.asInstanceOf[Boolean])
      //case s: String => s.compareTo(o.asInstanceOf[String])
      case _ => throw new IllegalArgumentException(s"Cannot compare $v to $o")
    case None => throw new IllegalArgumentException("Cannot compare null to $o")

  def compareTo(o: ValPDXScript[T]): Option[Int] =
    o.value match
      case Some(v) => Some(this.compareTo(v))
      case None => None

  def asString: String = this.value.map(_.toString).getOrElse("")

  override def toString: String = if asString == "" then "[null]" else asString

}
