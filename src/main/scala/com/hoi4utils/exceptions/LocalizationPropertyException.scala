package com.hoi4utils.exceptions

import com.hoi4utils.localization.{Localization, Property}

class LocalizationPropertyException(message: String) extends Exception(message) {

  def this(property: Property, obj: Any) =
    this(
      s"The localization property $property is not applicable for this object."
    )

}
