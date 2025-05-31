package com.hoi4utils.exceptions

class NotPermittedInScopeException(message: String, cause: Throwable) extends Exception(message, cause) {

  def this(cause: Throwable) =
    this(null, cause)
    
  def this(message: String) =
    this(message, null)
}
