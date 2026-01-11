package com.hoi4utils.improvedzio.macros

import zio.macros.ServiceReloader
import zio.{IsReloadable, ZEnvironment, ZIO, ZLayer}

import scala.annotation.experimental

object ImprovedMacros {

  implicit final class ImprovedReloadableSyntax[R, E, Service](private val layer: ZLayer[R, E, Service]) extends AnyVal {

    /**
     * Returns a layer that constructs a version of the service output by this
     * layer that can be dynamically reloaded with `ServiceReloader.reload`.
     */
    def reloadable(implicit 
      tag: zio.Tag[Service],
      tagR: zio.Tag[R],             
      isReloadable: IsReloadable[Service],
      trace: zio.Trace
    ): ZLayer[R & ServiceReloader, ServiceReloader.Error, Service] =
//      ZLayer.fromZIO(ServiceReloader.register(layer))
      ZLayer.fromZIO {
        ZIO.serviceWithZIO[R] { r =>
          // We provide the requirement R to the layer to turn it into ZLayer[Any, E, Service]
          val closedLayer: ZLayer[Any, Any, Service] = ZLayer.succeedEnvironment(ZEnvironment(r)) >>> layer
          ServiceReloader.register(closedLayer)
        }
      }
  }
}
