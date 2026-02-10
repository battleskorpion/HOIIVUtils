package com.hoi4utils.script2

import com.hoi4utils.script.PDXFileError

import scala.reflect.Selectable.reflectiveSelectable

class PDXProperty[T]
(
  val pdxKey: String,
  private var _value: Option[T] = None
):

  private var _isRequired: Boolean = false
  private var _default: Option[T] = None

  def apply(): Option[T] = _value.orElse(_default)
  def $: T = apply().getOrElse(
    throw new IllegalStateException(s"Property $pdxKey is empty and has no default.")
  )

  def :=(newValue: T): Unit = _value = Some(newValue)

  infix def default(v: T): PDXProperty[T] =
    _default = Some(v)
    this
  def isDefault: Boolean = apply() == default 

  infix def required(v: Boolean): PDXProperty[T] = { _isRequired = v; this }
  def isRequired: Boolean = _isRequired

  def validate(f: T => Boolean): PDXProperty[T] = { /* store validation logic */ this }
