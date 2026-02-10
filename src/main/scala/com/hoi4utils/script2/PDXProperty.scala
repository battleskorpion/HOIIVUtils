package com.hoi4utils.script2

import com.hoi4utils.script.PDXFileError

import scala.{:+, ::}
import scala.reflect.Selectable.reflectiveSelectable

class PDXProperty[T]
(
  val pdxKey: String,
  private var _value: Option[T] = None
) extends PDXScript[T]:

  private var _isRequired: Boolean = false
  private var _default: Option[T] = None

  def apply(): Option[T] = _value.orElse(_default)
  def $: T = apply().getOrElse(
    throw new IllegalStateException(s"Property $pdxKey is empty and has no default.")
  )
  def pdxDefinedValueOption: Option[T] = _value

  def :=(newValue: T): Unit = _value = Some(newValue)

  infix def default(v: T): PDXProperty[T] =
    _default = Some(v)
    this
  def isDefault: Boolean = apply() == default

  infix def required(v: Boolean): PDXProperty[T] = { _isRequired = v; this }
  def isRequired: Boolean = _isRequired

  def validate(f: T => Boolean): PDXProperty[T] = { /* store validation logic */ this }

class PDXPropertyList[T]
(
  val pdxKey: String,
  private var _values: Option[List[T]] = None
) extends PDXScript[List[T]]:

  private var _isRequired: Boolean = false
  private var _default: Option[List[T]] = None

  def apply(): Option[List[T]] = _values.map(_.reverse).orElse(_default)
  def $: List[T] = apply().getOrElse(
    throw new IllegalStateException(s"Property $pdxKey is empty and has no default.")
  )
  override def pdxDefinedValueOption: Option[List[T]] = _values

  def :+(value: T): Unit = _values match
    case Some(values) => _values = Some(value :: values)
    case None => _values = Some(value :: Nil)

  infix def default(v: List[T]): PDXPropertyList[T] =
    _default = Some(v)
    this

  def isDefault: Boolean = apply() == default

  infix def required(v: Boolean): PDXPropertyList[T] =
    _isRequired = v
    this
  def isRequired: Boolean = _isRequired

  def validate(f: T => Boolean): PDXPropertyList[T] = { /* store validation logic */ this }


