package com.hoi4utils.hoi4.localization

class LocalizationController(localizationMgr: BaseLocalizationService) {
  
  def saveLocalization(): Unit =
    localizationMgr.saveLocalization()

}
