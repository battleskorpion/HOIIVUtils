package com.hoi4utils.script2

import com.hoi4utils.parser.PDXValueType

import scala.annotation.targetName


object PDXPropertyValueExtensions {
  extension [TV <: PDXValueType](pdxProperty:  PDXProperty[TV])
    @targetName("getEquals")
    def @==(other: TV): Boolean = value match
      case Some(v) => v.equals(other)
      case None => false

    /**
     * Checks the value of the script is equal to the value of the given script.
     *
     * @param other
     * @return
     */
    @targetName("getEquals")
    def @==(other: PDXProperty[TV]): Boolean = value match
      case Some(v) => other @== v
      case None => false

    /**
     * Checks if the value of the script is not equal to the given value.
     *
     * @param other
     * @return
     */
    @targetName("getNotEquals")
    def @!=(other: TV): Boolean = !(pdxProperty @== other)

    /**
     * Sets the value of the script to the given value.
     *
     * @param other
     */
    def @=(other: TV): Unit = set(other)

    /**
     * Sets the value of the script to the value of the given script.
     *
     * @param other
     */
    def @=(other: PDXProperty[TV]): Unit = other.value match
      case Some(v) => set(v)
      case None => pdxProperty.clear()

    /**
     * Sets the value of the script to the given value. If the given value is the script's default value
     * and the script's value is empty, does nothing.
     */
    def @=?(other: TV): Unit =
      if (pdxProperty.isDefined || !pdxProperty.isDefault(other)) set(other)

    def value: Option[TV] = pdxProperty()

    def set(other: TV): Unit = pdxProperty := other

//    override def compareTo(o: T): Int = value match
//      case Some(v) => v match
//        case i: Int => i.compareTo(o.asInstanceOf[Int])
//        case d: Double => d.compareTo(o.asInstanceOf[Double])
//        case b: Boolean => b.compareTo(o.asInstanceOf[Boolean])
//        //case s: String => s.compareTo(o.asInstanceOf[String])
//        case _ => throw new IllegalArgumentException(s"Cannot compare $v to $o")
//      case None => throw new IllegalArgumentException("Cannot compare null to $o")
//
//    def compareTo(o: ValPDXScript[T, NodeV]): Option[Int] =
//      o.value match
//        case Some(v) => Some(this.compareTo(v))
//        case None => None

    def asString: String = value.map(_.toString).getOrElse("")

    override def toString: String = if asString == "" then "[null]" else asString

  extension [N <: Int | Double & PDXValueType](pdxProperty:  PDXProperty[N])(using num: Fractional[N] | Integral[N])
    @targetName("unaryPlus")
    def unary_+ : N = pdxProperty.value match
      case Some(v) => v
      case None => num.zero

    @targetName("unaryMinus")
    def unary_- : N = pdxProperty.value match
      case Some(v) => num.negate(v)
      case None => num.zero

    @targetName("plus")
    def +(other: N): N = pdxProperty.value match
      case Some(v) => num.plus(v, other)
      case None => other

    @targetName("minus")
    def -(other: N): N = pdxProperty.value match
      case Some(v) => num.minus(v, other)
      case None => num.negate(other)

    @targetName("multiply")
    def *(other: N): N = pdxProperty.value match
      case Some(v) => num.times(v, other)
      case None => num.zero

    @targetName("divide")
    def /(other: N): N = pdxProperty.value match
      case Some(v) => num match
        case f: Fractional[N] => f.div(v, other)
        case i: Integral[N] => i.quot(v, other)
      case None => num.zero

    @targetName("plusEquals")
    def +=(other: N): N = pdxProperty.set(pdxProperty + other)

    @targetName("minusEquals")
    def -=(other: N): N = pdxProperty.set(pdxProperty - other)

    @targetName("multiplyEquals")
    def *=(other: N): N = pdxProperty.set(pdxProperty * other)

    @targetName("divideEquals")
    def /=(other: N): N = pdxProperty.set(pdxProperty / other)
}
