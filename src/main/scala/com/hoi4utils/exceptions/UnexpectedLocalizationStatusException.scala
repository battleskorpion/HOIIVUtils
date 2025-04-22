package com.hoi4utils.exceptions

import com.hoi4utils.clausewitz.localization.Localization


class UnexpectedLocalizationStatusException extends RuntimeException {
  def this(localization: Localization, other: Localization) {
    this()
    super ("Unexpected localization statuses: " + localization + ", status: " + localization.status + " and " + other + ", status: " + other.status)
  }

  def this(localization: Localization) {
    this()
    super ("Unexpected localization status: " + localization + ", status: " + localization.status)
  }
}
