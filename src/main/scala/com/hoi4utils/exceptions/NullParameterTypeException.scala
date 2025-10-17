package com.hoi4utils.exceptions

class NullParameterTypeException(message: String, cause: Throwable) extends Exception(message, cause):

  def this(message: String) =
    this(message, null)

  def this(cause: Throwable) =
    // mirror Java's `super(cause)` semantics: detailMessage = cause.toString
    this(if (cause != null) cause.toString else null, cause)
