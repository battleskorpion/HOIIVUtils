package com.hoi4utils.exceptions

import com.hoi4utils.localization.Localization

class LocalizationExistsException extends Throwable {
  def this(localization: Localization) {
    this()
    super ("Localization already exists: " + localization.ID)
  }

  def this(prevLocalization: Localization, localization: Localization) {
    this()
    super ("Localization already exists: " + prevLocalization + "\n\tand cannot be replaced by: " + localization)
  }

  def this(prevLocalization: Localization, text: String) {
    this()
    super ("Localization already exists: " + prevLocalization + "\n\tand cannot be replaced with the text: " + text)
  }
}
