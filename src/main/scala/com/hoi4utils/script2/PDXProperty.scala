package com.hoi4utils.script2

import com.hoi4utils.parser.NodeValueType
import com.hoi4utils.script.PDXFileError

import scala.reflect.ClassTag
import scala.reflect.Selectable.reflectiveSelectable

class PDXProperty[T](val pdxKey: String, private var _value: Option[T] = None)
                    (using override val decoder: PDXDecoder[T]) extends PDXScript[T]:

  private var _isRequired: Boolean = false
  private var _default: Option[T] = None

  override def apply(): Option[T] = _value.orElse(_default)
  def $: T = apply().getOrElse(
    throw new IllegalStateException(s"Property $pdxKey is empty and has no default.")
  )
  override def pdxDefinedValueOption: Option[T] = _value

  def :=(newValue: T): Unit = _value = Some(newValue)

  override def set(value: T): Unit = this := value

  override def extractAndSet(nodeValue: NodeValueType): Either[String, Unit] =
    decoder.decode(nodeValue).map(v => this := v)

  infix def default(v: T): PDXProperty[T] =
    _default = Some(v)
    this
  def isDefault: Boolean = apply() == default

  infix def required(v: Boolean): PDXProperty[T] = { _isRequired = v; this }
  def isRequired: Boolean = _isRequired

  def validate(f: T => Boolean): PDXProperty[T] = { /* store validation logic */ this }

class PDXPropertyList[T](val pdxKey: String, private var _values: Option[List[T]] = None)
                        (using override val decoder: PDXDecoder[List[T]], val elementDecoder: PDXDecoder[T]) 
  extends PDXScript[List[T]]:

  private var _isRequired: Boolean = false
  private var _default: Option[List[T]] = None

  override def apply(): Option[List[T]] = _values.map(_.reverse).orElse(_default)
  def $: List[T] = apply().getOrElse(
    throw new IllegalStateException(s"Property $pdxKey is empty and has no default.")
  )
  override def pdxDefinedValueOption: Option[List[T]] = _values

  def :+(value: T): Unit = _values match
    case Some(values) => _values = Some(value :: values)
    case None => _values = Some(value :: Nil)

  /**
   * ``
   * @param value
   * @note Since apply() reverses, we must store this reversed so the final output is correct.
   */
  override def set(value: List[T]): Unit = _values = Some(value.reverse)

  override def extractAndSet(nodeValue: NodeValueType): Either[String, Unit] =
    elementDecoder.decode(nodeValue).map(v => this :+ v)

  infix def default(v: List[T]): PDXPropertyList[T] =
    _default = Some(v)
    this

  def isDefault: Boolean = apply() == default

  infix def required(v: Boolean): PDXPropertyList[T] =
    _isRequired = v
    this
  def isRequired: Boolean = _isRequired

  def validate(f: T => Boolean): PDXPropertyList[T] = { /* store validation logic */ this }

