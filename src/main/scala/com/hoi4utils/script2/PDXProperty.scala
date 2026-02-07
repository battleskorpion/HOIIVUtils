package com.hoi4utils.script2

import com.hoi4utils.script.PDXFileError

import scala.reflect.Selectable.reflectiveSelectable

class PDXProperty[T]
(
  val pdxKey: String,
  private var _value: Option[T]
)(using parser: PDXParser[T]):

  private var _isRequired: Boolean = false

  def apply(): T = _value.get
  def :=(newValue: T): Unit = _value = Some(newValue)

  // Fluent API for configuration
  // TODO bad default is if value is NONE, then return default value
  infix def default(v: T): PDXProperty[T] =
    _value = Some(v);
    this

  infix def required(v: Boolean): PDXProperty[T] = { _isRequired = v; this }
  def isRequired: Boolean = _isRequired

  def validate(f: T => Boolean): PDXProperty[T] = { /* store validation logic */ this }

  def loadFrom(node: Node, context: String)(handleError: PDXFileError => Unit): Unit =
    parser.parse(node) match {
      case Right(newValue) => _value = Some(newValue)
      case Left(errorMsg)  =>
        handleError(new PDXFileError(
          message = s"Field '$pdxKey' in $context: $errorMsg",
          errorNode = node
        ))
    }
