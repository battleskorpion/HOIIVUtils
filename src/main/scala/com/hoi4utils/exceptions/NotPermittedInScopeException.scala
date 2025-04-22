package com.hoi4utils.exceptions

class NotPermittedInScopeException extends Exception {
  def this(message: String) {
    this()
    super (message)
  }

  def this(message: String, cause: Throwable) {
    this()
    super (message, cause)
  }

  def this(cause: Throwable) {
    this()
    super (cause)
  }
}
