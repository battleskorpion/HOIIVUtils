package com.hoi4utils.hoi4.localization

class LocalizationController(localizationMgr: LocalizationManager) {
  
  def saveLocalization(): Unit =
    localizationMgr.saveLocalization()

}
