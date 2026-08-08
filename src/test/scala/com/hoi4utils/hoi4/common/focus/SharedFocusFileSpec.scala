package com.hoi4utils.hoi4.common.focus

import com.hoi4utils.HOIIVUtilsSpec.layer
import com.hoi4utils.hoi42.common.country_tags.CountryTagService
import com.hoi4utils.hoi42.common.national_focus.{FocusTree, FocusTreeService, SharedFocus, SharedFocusFile}
import com.hoi4utils.main.RegistryLayers
import com.hoi4utils.parser.NodeExtensions.*
import com.hoi4utils.parser.{NodeSeq, ZIOParser}
import com.hoi4utils.script2.{PDXLoader, Registry}
import org.scalamock.ziotest.ScalamockZIOSpec
import zio.test.{Spec, TestEnvironment, TestResult, assertTrue}
import zio.{Scope, ZIO, ZLayer}

import java.io.File

object SharedFocusFileSpec extends ScalamockZIOSpec {
  private val testPath = "src/test/resources/pdx/"
  private val filesToTest: List[File] = List(
    new File(testPath + "Canadian_Shared.txt"),
    new File(testPath + "shared_focuses_1_shortest.txt"),
    new File(testPath + "shared_focuses_1_shorter.txt"),
    new File(testPath + "shared_focuses_1_short.txt"),
    new File(testPath + "shared_focuses_1_longer.txt"),
    new File(testPath + "shared_focuses_1_longerer.txt"),
    new File(testPath + "shared_focuses_1_longererer.txt"),
    new File(testPath + "shared_focuses_1_longerererer.txt"),
    new File(testPath + "shared_focuses_1_longy.txt"),
    new File(testPath + "shared_focuses_1_long.txt"),
  )

  def foreachSharedFocusFile[R](files: List[File] = filesToTest)(f: SharedFocusFile => ZIO[R, Throwable, TestResult]): ZIO[R & CountryTagService & FocusTreeService & Registry[SharedFocus], Throwable, TestResult] =
    ZIO.foreach(files) { file =>
      for {
        treeService <- ZIO.service[FocusTreeService]
        tagsService <- ZIO.service[CountryTagService]
        given Registry[SharedFocus] = treeService.sharedPseudoSharedFocusTree

        node <- new ZIOParser(file).parse
        pdx <- ZIO.attempt {
          val loader = new PDXLoader[SharedFocusFile]()
          val sharedFocusFile = new SharedFocusFile(treeService.sharedFocusFileRegistry, Some(file))
          val errors = loader.load(node, sharedFocusFile, sharedFocusFile)
          if (errors.nonEmpty) {
            println(s"Parse errors in ${file.getName}: ${errors.mkString(", ")}")
          }
          sharedFocusFile
        }
        result <- f(pdx)
      } yield result
    }.map(TestResult.allSuccesses)


  override def spec: Spec[TestEnvironment & Scope, Any] =
    suite("SharedFocusFile")(
      test("SharedFocusFile should load and populate shared focus properties") {
        foreachSharedFocusFile() { sff =>
          val focuses = sff.sharedFocuses.$
          assertTrue(focuses.nonEmpty, focuses.forall(_.id.pdxDefinedValueOption.isDefined))
        }
      },
      test("SharedFocusFile should register shared focuses as a Registry") {
        foreachSharedFocusFile() { sff =>
          val focuses = sff.referableEntities
          assertTrue(
            focuses.nonEmpty,
            focuses.forall(_.id.pdxDefinedValueOption.isDefined),
            focuses.size.equals(sff.sharedFocuses.$.size)
          )
        }
      },
      test("Shared Focus items should resolve references correctly") {
        // todo this test isnt necessarily correct with sharedFocusFiles. They might be relatively positioned to something else?
        foreachSharedFocusFile() { sff =>
          val focuses = sff.referableEntities
          val focusWithRef = focuses.find(_.relativePositionFocus.pdxDefinedValueOption.isDefined)
          focusWithRef match {
            case Some(f) =>
              val ref = f.relativePositionFocus.$.value // Resolving the Reference[Focus]
              assertTrue(ref.isDefined) // Checks if the ID exists in the registry
            case None =>
              assertTrue(focuses.nonEmpty)
          }
        }
      },
      test("Shared focus files should not be considered as having a standard focus tree header") {
        foreachSharedFocusFile() { sff =>
          for {
            treeService <- ZIO.service[FocusTreeService]
            hasHeader   <- treeService.hasFocusTreeHeader(sff.file.get)
          } yield assertTrue(!hasHeader)
        }
      }
    ).provide(
      TestEnvironment.live,
      Scope.default,
      ZLayer.succeed(zio.Clock.ClockLive),
      ZLayer.succeed(zio.Console.ConsoleLive),
      ZLayer.succeed(zio.System.SystemLive),
      ZLayer.succeed(zio.Random.RandomLive),
      // shared registry layers
      RegistryLayers.sharedFocusRegistryLayer,    // todo figure out why had to be separate even though included in 'layer'
      layer
    )
}
