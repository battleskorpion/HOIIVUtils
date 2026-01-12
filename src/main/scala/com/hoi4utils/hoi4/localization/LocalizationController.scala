package com.hoi4utils.hoi4.localization

import com.hoi4utils.hoi4.localization.service.BaseLocalizationService

class LocalizationController(localizationMgr: BaseLocalizationService) {
  
  def saveLocalization(): Unit =
    localizationMgr.saveLocalization()

}
