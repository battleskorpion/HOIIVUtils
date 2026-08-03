package com.hoi4utils.main

import com.hoi4utils.hoi42.common.national_focus.{FocusTreeService, SharedFocus}
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
