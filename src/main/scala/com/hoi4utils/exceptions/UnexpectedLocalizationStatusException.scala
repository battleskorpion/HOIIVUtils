package com.hoi4utils.exceptions

import com.hoi4utils.hoi4.localization.Localization

class UnexpectedLocalizationStatusException(message: String) extends Exception(message):

  def this(loc: Localization, other: Localization) =
    this(
      s"Unexpected localization statuses: $loc, status: ${loc.status} and $other, status: ${other.status}"
    )

  def this(loc: Localization) =
    this(
      s"Unexpected localization status: $loc, status: ${loc.status}"
    )
