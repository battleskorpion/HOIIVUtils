package com.hoi4utils.hoi4.common.focus

import com.hoi4utils.HOIIVUtilsSpec.layer
import com.hoi4utils.hoi42.common.country_tags.CountryTagService
import com.hoi4utils.hoi42.common.national_focus.{FocusTree, FocusTreeService, SharedFocus}
import com.hoi4utils.main.RegistryLayers
import com.hoi4utils.parser.NodeExtensions.*
import com.hoi4utils.parser.{NodeSeq, ZIOParser}
import com.hoi4utils.script2.{PDXLoader, Registry}
import org.scalamock.ziotest.ScalamockZIOSpec
import org.scalatest.funsuite.AnyFunSuiteLike
import zio.test.junit.JUnitRunnableSpec
import zio.test.{Spec, TestEnvironment, TestResult, assertTrue}
import zio.{Scope, ZIO, ZLayer}

import java.io.File
import scala.collection.mutable.ListBuffer
import scala.util.{Failure, Success, Try}

object FocusTreeSpec extends ScalamockZIOSpec {
  private val testPath = "src/test/resources/pdx/"
  private val filesToTest: List[File] = List(
    new File(testPath + "national_focus" + File.separator + "alabama_fail_24.txt"),
    new File(testPath + "minimichigantest.txt"),
    new File(testPath + "minimichigantest2.txt"),
    new File(testPath + "minimichigantest3.txt"),
    new File(testPath + "Massachusetts_focus.txt"),
    new File(testPath + "Massachusetts_focus_simple.txt"),
    new File(testPath + "texas_tree.txt")
  )

  def foreachFocusTree[R](files: List[File] = filesToTest)(f: FocusTree => ZIO[R, Throwable, TestResult]): ZIO[R & CountryTagService & FocusTreeService & Registry[SharedFocus], Throwable, TestResult] =  // FocusTreeManager & CountryTagService
    ZIO.foreach(files) { file =>
      for {
        treeService <- ZIO.service[FocusTreeService]
        tagsService <- ZIO.service[CountryTagService]
        given Registry[SharedFocus] = treeService.sharedPseudoSharedFocusTree

        node <- new ZIOParser(file).parse
        pdx <- ZIO.attempt {
          val loader = new PDXLoader[FocusTree]()
          val focusTree = new FocusTree(treeService, Some(file))
          val pdxNode = node.getTyped[NodeSeq]("focus_tree")
          val errors = loader.load(pdxNode, focusTree, focusTree)
//          val errors = loader.load(node, focusTree, focusTree)    // using 'node' is WRONG here. must do `val pdxNode = node.getTyped[NodeSeq]("focus_tree")` and use pdxNode
          if (errors.nonEmpty) {
            println(s"Parse errors in ${file.getName}: ${errors.mkString(", ")}")
          }
          focusTree
        }
        result <- f(pdx)
      } yield result
    }.map(TestResult.allSuccesses)

  override def spec: Spec[TestEnvironment & Scope, Any] =
    suite("FocusTree")(
      test("FocusTree should load and populate focus properties") {
        foreachFocusTree() { focusTree =>
          val focuses = focusTree.focuses.$
          assertTrue(focuses.nonEmpty, focuses.forall(_.id.pdxDefinedValueOption.isDefined))
        }
      },
      test("FocusTree should register focuses as a Registry") {
        foreachFocusTree() { focusTree =>
          val focuses = focusTree.referableEntities
          assertTrue(
            focuses.nonEmpty,
            focuses.forall(_.id.pdxDefinedValueOption.isDefined),
            focuses.size.equals(focusTree.focuses.$.size)
          )
        }
      },
      test("Focus items should resolve references correctly") {
        foreachFocusTree() { focusTree =>
          val focuses = focusTree.referableEntities
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
      test("Focus trees should be considered as having a standard focus tree header") {
        foreachFocusTree() { focusTree =>
          for {
            treeService <- ZIO.service[FocusTreeService]
            hasHeader   <- treeService.hasFocusTreeHeader(focusTree.file.get)
          } yield assertTrue(hasHeader)
        }
      },
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
