package com.hoi4utils.main

import com.hoi4utils.hoi42.common.country_tags.CountryTagService
import com.hoi4utils.hoi42.common.national_focus.{FocusTreeService, SharedFocus}
import com.hoi4utils.hoi42.gfx.InterfaceService
import com.hoi4utils.hoi42.history.countries.service.CountryService
import com.hoi4utils.hoi42.map.state.service.StateService
import com.hoi4utils.script2.Registry
import zio.*


object ZLayers {

}

object RegistryLayers {
  val sharedFocusRegistryLayer: ZLayer[FocusTreeService, Nothing, Registry[SharedFocus]] =
    ZLayer.fromZIO(
      ZIO.serviceWith[FocusTreeService](_.sharedPseudoSharedFocusTree)
    )
}
