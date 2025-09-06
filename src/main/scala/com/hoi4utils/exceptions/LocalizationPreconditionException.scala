package com.hoi4utils.exceptions

import com.hoi4utils.localization.Localization

case class LocalizationPreconditionException(precondition: String, cause: Throwable = null)
  extends Exception(
    s"Can not perform localization since the following precondition was not met: $precondition",
    cause
  )
