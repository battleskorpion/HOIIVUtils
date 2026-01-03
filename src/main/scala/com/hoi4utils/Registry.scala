package com.hoi4utils

import com.hoi4utils.hoi4.localization.{LocalizationController, BaseLocalizationService}
import wvlet.airframe.*

// todo remove airframe lol
object Registry {
  // Define the design once
  val design: Design = newDesign
    .bind[BaseLocalizationService].toInstance(BaseLocalizationService.get)

  // Start the session once for the lifetime of the app
  val session: Session = design.newSession

  // Java-friendly helper to get the controller
  def getLocalizationController: LocalizationController = session.build[LocalizationController]
}
