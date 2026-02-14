package com.hoi4utils

//import com.hoi4utils.hoi4.common.country_tags.CountryTagService
import com.hoi4utils.hoi4.common.focus.{FocusTreeSpec, SharedFocusSpec}
//import com.hoi4utils.hoi4.common.national_focus.FocusTreeManager
import com.hoi4utils.parser.{ZIOParser, ZIOParserSpec}
//import com.hoi4utils.script.PDXScriptSpec
import org.scalamock.ziotest.ScalamockZIOSpec
import zio.{Clock, Random, Scope, ZLayer}
import zio.test.{Spec, TestEnvironment}

object HOIIVUtilsSpec extends ScalamockZIOSpec {
  override def spec: Spec[TestEnvironment & Scope, Any] = suite("HOIIVUtils scalamock-zio suite")(
    ZIOParserSpec.spec,
//    PDXScriptSpec.spec,
//    SharedFocusSpec.spec,
    FocusTreeSpec.spec
  ).provide(
    ZLayer.make[TestEnvironment & Scope](
      TestEnvironment.live,
      Scope.default,
      ZLayer.succeed(zio.Clock.ClockLive),
      ZLayer.succeed(zio.Console.ConsoleLive),
      ZLayer.succeed(zio.System.SystemLive),
      ZLayer.succeed(zio.Random.RandomLive)
    )
  )
}
