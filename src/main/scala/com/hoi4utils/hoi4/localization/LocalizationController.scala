package com.hoi4utils.hoi4.localization

class LocalizationController(localizationMgr: LocalizationService) {
  
  def saveLocalization(): Unit =
    localizationMgr.saveLocalization()

}
