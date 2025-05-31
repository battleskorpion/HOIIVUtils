package com.hoi4utils.exceptions

import com.hoi4utils.localization.Localization

class LocalizationExistsException(message: String) extends Exception(message) {
  def this(localization: Localization) =
    this(s"Localization already exists: ${localization.ID}")

  def this(prev: Localization, next: Localization) =
    this(
      s"Localization already exists: $prev\n\tand cannot be replaced by: $next"
    )

  def this(prev: Localization, text: String) =
    this(
      s"Localization already exists: $prev\n\tand cannot be replaced with the text: $text"
    )
}
