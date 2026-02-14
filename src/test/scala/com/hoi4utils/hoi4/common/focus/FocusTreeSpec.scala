package com.hoi4utils.hoi4.common.focus

import com.hoi4utils.hoi42.common.national_focus.FocusTree
import com.hoi4utils.parser.ZIOParser
import com.hoi4utils.script2.PDXLoader
import org.scalamock.ziotest.ScalamockZIOSpec
import org.scalatest.funsuite.AnyFunSuiteLike
import zio.test.junit.JUnitRunnableSpec
import zio.test.{Spec, TestEnvironment, TestResult, assertTrue}
import zio.{Scope, ZIO}

import java.io.File
import scala.collection.mutable.ListBuffer
import scala.util.{Failure, Success, Try}

object FocusTreeSpec extends ScalamockZIOSpec {
  private val testPath = "src/test/resources/pdx/"
  private val filesToTest: List[File] = List(
    new File(testPath + "minimichigantest.txt"),
    new File(testPath + "minimichigantest2.txt"),
    new File(testPath + "minimichigantest3.txt"),
  )

  def foreachFocusTree(files: List[File] = filesToTest)(f: FocusTree => TestResult): ZIO[Any, Throwable, TestResult] =  // FocusTreeManager & CountryTagService
    ZIO.foreach(files) { file =>
      for {
//          treeManager <- ZIO.service[FocusTreeManager]
//          tagsService <- ZIO.service[CountryTagService]
        node <- new ZIOParser(file).parse
        pdx <- ZIO.attempt {
          val focusTree = new FocusTree()
          val loader = new PDXLoader[FocusTree]()
          val errors = loader.load(node, focusTree, focusTree)
          if (errors.nonEmpty) {
            println(s"Parse errors in ${file.getName}: ${errors.mkString(", ")}")
          }
          focusTree
        }
      } yield pdx
    }.map { pdxs =>
      TestResult.allSuccesses(pdxs.map(f))
    }

  override def spec: Spec[TestEnvironment & Scope, Any] =
    suite("FocusTree")(
      test("FocusTree should load and populate focus properties") {
        foreachFocusTree() { focusTree =>
          val focuses = focusTree.referableEntities
          assertTrue(focuses.nonEmpty, focuses.forall(_.id.pdxDefinedValueOption.isDefined))
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
    )
}
