package com.hoi4utils.hoi4.map

import com.hoi4utils.HOIIVUtilsSpec.layer
import com.hoi4utils.hoi42.common.country_tags.CountryTagService
import com.hoi4utils.hoi42.map.state.service.StateService
import com.hoi4utils.hoi42.map.state.*
import com.hoi4utils.parser.NodeExtensions.getTyped
import com.hoi4utils.parser.{NodeSeq, ZIOParser}
import com.hoi4utils.script2.PDXLoader
import org.scalamock.ziotest.ScalamockZIOSpec
import org.scalatest.Assertions.withClue
import zio.test.{Spec, TestEnvironment, TestResult, assertTrue}
import zio.{Scope, ZIO, ZLayer}

import java.io.File

object StateSpec extends ScalamockZIOSpec {
  private val testPath = "src/test/resources/pdx/state/"
  private val filesToTest: List[File] = List(
    new File(testPath + "1-Cape Cod.txt"),
    new File(testPath + "2-Plymouth County MA.txt"),
    new File(testPath + "3-Bristol County MA.txt"),
    new File(testPath + "4-Suffolk MA.txt"),
    new File(testPath + "5-Boston MA.txt"),
    new File(testPath + "6-Newport County RI.txt"),
    new File(testPath + "7-Providence.txt"),
    new File(testPath + "8-Western MA.txt"),
    new File(testPath + "9-Worcester.txt"),
    new File(testPath + "10-Southern RI.txt"),
    new File(testPath + "11-Miami.txt"),
    new File(testPath + "12-Southern FL.txt"),
    new File(testPath + "13-Florida Panhandle.txt"),
    new File(testPath + "14-Big Bend FL.txt"),
    new File(testPath + "15-Jacksonville.txt"),
    new File(testPath + "16-Eastern FL.txt"),
    new File(testPath + "17-Western FL.txt"),
    new File(testPath + "18-Baton Rouge.txt"),
    new File(testPath + "19-Southeastern LA.txt"),
    new File(testPath + "20-Southwestern LA.txt"),
  )

  def foreachState[R](files: List[File] = filesToTest)(f: State => ZIO[R, Throwable, TestResult]): ZIO[R & CountryTagService & StateService, Throwable, TestResult] =
    ZIO.foreach(files) { file =>
      for {
        stateService <- ZIO.service[StateService]
        tagsService <- ZIO.service[CountryTagService]

        node <- new ZIOParser(file).parse
        pdx <- ZIO.attempt {
          val loader = new PDXLoader[State]()
          val state = new State(stateService, Some(file))
          val pdxNode = node.getTyped[NodeSeq]("state")
          val errors = loader.load(pdxNode, state, state)
          if (errors.nonEmpty)
            println(s"Parse errors in ${file.getName}: ${errors.mkString(", ")}")
          state
        }
        result <- f(pdx)
      } yield result
    }.map(TestResult.allSuccesses)

  override def spec: Spec[TestEnvironment & Scope, Any] =
    suite("State")(
      test("State should load and properties should be populated") {
        foreachState() { state =>
          state.provinces().fold(
            assertTrue(false) ?? s"State '${state.name} (File: ${state.file})' failed to load provinces"
          ) { provinces =>
            assertTrue(
              provinces.nonEmpty,
              provinces.forall(_.value.isDefined)
            ) ?? s"State '${state.name}' provinces are missing data"
          }
        }
      },
    ).provide(
      TestEnvironment.live,
      Scope.default,
      ZLayer.succeed(zio.Clock.ClockLive),
      ZLayer.succeed(zio.Console.ConsoleLive),
      ZLayer.succeed(zio.System.SystemLive),
      ZLayer.succeed(zio.Random.RandomLive),
      layer
    )
}
