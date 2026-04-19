package com.hoi4utils.script2

import com.hoi4utils.parser.PDXValueType
import com.hoi4utils.script2.PDXPropertyValueExtensions.@=

import scala.annotation.targetName
import scala.reflect.ClassTag


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

  extension [T <: RegistryMember[T]](pdxProperty: PDXProperty[Reference[T]])
    /**
     * Directly accesses the underlying value of a Reference inside a PDXProperty.
     * Handles both the Option of the property and the Option of the reference.
     */
    def resolve: Option[T] = pdxProperty().flatMap(_.value)

  extension [E <: PDXEntity](pdxProperty: PDXProperty[E])
    /**
     * Reach from a Property into a nested Property that holds a Reference
     */
    def flatMapRef[R <: RegistryMember[R]](f: E => PDXProperty[Reference[R]]): Option[R] =
      for
        entity <- pdxProperty()
        refProp = f(entity)
        ref <- refProp()
        value <- ref.value
      yield value

    /**
     * allows `property ~> (_.subProperty)` returns optional sub-property 
     * @param extractor
     * @tparam C
     * @return
     */
    infix def ~>[C](extractor: E => PDXProperty[C]): Option[C] =
      pdxProperty().flatMap(e => extractor.apply(e).apply())

  extension [P <: PDXEntity](parent: PDXProperty[P])
    infix def /[C](extractor: P => PDXProperty[C])(using d: PDXDecoder[C], ct: ClassTag[C]): PDXProperty[C] =
      new PDXProperty[C]("__virtual", None)(using d)(using ct):
        override def apply(): Option[C] = parent().flatMap(p => extractor(p)())

        override def set(v: C): C = {
          parent().foreach(p => extractor(p).set(v)); v
        }

  extension [P <: PDXEntity](parentOpt: Option[P])
    infix def /[C](extractor: P => PDXProperty[C])(using d: PDXDecoder[C], ct: ClassTag[C]): PDXProperty[C] =
      new PDXProperty[C]("__virtual", None)(using d)(using ct):
        override def apply(): Option[C] = parentOpt.flatMap(p => extractor(p)())

        override def set(v: C): C = {
          parentOpt.foreach(p => extractor(p).set(v)); v
        }

  extension [P](parentOpt: Option[P])
    infix def ~>[C](extractor: P => Option[C]): Option[C] =
      parentOpt.flatMap(extractor)

}
