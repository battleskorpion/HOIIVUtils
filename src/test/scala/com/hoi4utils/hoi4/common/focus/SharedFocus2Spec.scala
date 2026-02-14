//package com.hoi4utils.hoi4.common.focus
//
//import com.hoi4utils.hoi4.common.country_tags.CountryTagService
//import com.hoi4utils.hoi4.common.national_focus.{FocusTree, FocusTreeManager, PseudoSharedFocusTree, SharedFocusFile}
//import com.hoi4utils.parser.ZIOParser
//import org.scalamock.ziotest.ScalamockZIOSpec
//import org.scalatest.funsuite.AnyFunSuiteLike
//import zio.test.junit.JUnitRunnableSpec
//import zio.test.{Spec, TestEnvironment, TestResult, assertTrue}
//import zio.{Scope, ZIO}
//
//import java.io.File
//import scala.collection.mutable.ListBuffer
//import scala.util.{Failure, Success, Try}
//
//// i had junit extended first followed by scalamock but it was not working anyways and causing issues not letting me even run this properly
//object SharedFocus2Spec extends ScalamockZIOSpec {
//
//  private val testPath = "src/test/resources/pdx/"
//  private val sharedFocusFilesToTest = List(
//    new File(testPath + "shared_focuses_1_shortest.txt"),
//    new File(testPath + "shared_focuses_1_shorter.txt"),
//    new File(testPath + "shared_focuses_1_short.txt"),
//    new File(testPath + "shared_focuses_1_longer.txt"),
//    new File(testPath + "shared_focuses_1_longerer.txt"),
//    new File(testPath + "shared_focuses_1_longererer.txt"),
//    new File(testPath + "shared_focuses_1_longerererer.txt"),
//    new File(testPath + "shared_focuses_1_longy.txt"),
//    new File(testPath + "shared_focuses_1_long.txt"),
//  )
//  private val filesToTest: List[File] = List(
//    new File(testPath + "minimichigantest.txt"),
//    new File(testPath + "minimichigantest2.txt"),
//    new File(testPath + "minimichigantest3.txt"),
//  ).appendedAll(sharedFocusFilesToTest)
//
//  def foreachFocusTree(files: List[File] = filesToTest)(f: FocusTree => TestResult): ZIO[FocusTreeManager & CountryTagService, Throwable, TestResult] =
//    ZIO.foreach(files) { file =>
//      for {
//        treeManager <- ZIO.service[FocusTreeManager]
//        tagsService <- ZIO.service[CountryTagService]
//        node <- new ZIOParser(file).parse
//        pdx <- ZIO.attempt {
//          val focusTree = new FocusTree()
//          val loader = new PDXLoader[FocusTree]()
//          val errors = loader.load(node, focusTree, focusTree)
//          if (errors.nonEmpty) {
//            println(s"Parse errors in ${file.getName}: ${errors.mkString(", ")}")
//          }
//          focusTree
//        }
//      } yield pdx
//    }.map { pdxs =>
//      TestResult.allSuccesses(pdxs.map(f))
//    }
//
//  def foreachFilteredSharedFocusFile(files: List[File] = filesToTest)(f: SharedFocusFile => TestResult): ZIO[FocusTreeManager & CountryTagService, Throwable, TestResult] =
//    for {
//      treeManager <- ZIO.service[FocusTreeManager]
//      tagsService <- ZIO.service[CountryTagService]
//
//      filteredFiles <- ZIO.filter(files) { file => !treeManager.hasFocusTreeHeader(file) }
//      pdxs <- ZIO.foreach(filteredFiles) { file =>
//        for {
//          node <- new ZIOParser(file).parse
//          pdx <- ZIO.attempt {
//            val focusTree = new SharedFocusFile(file)
//            val loader = new PDXLoader[SharedFocusFile]()
//            val errors = loader.load(node, focusTree, focusTree)
//            if (errors.nonEmpty) {
//              println(s"Parse errors in ${file.getName}: ${errors.mkString(", ")}")
//            }
//            focusTree
//          }
//        } yield pdx
//      }
//    } yield TestResult.allSuccesses(pdxs.map(f))
//
//  override def spec: Spec[TestEnvironment & Scope, Any] =
//    suite("SharedFocus")(
//      test("Determining focus tree header works correctly (for focus trees)") {
//        foreachFocusTree() { focusTree =>
//          assertTrue(!sharedFocusFilesToTest.contains(focusTree.getFile)) ?? s"Successfully verified focus tree header detection for file: ${focusTree.getFile.map(_.getName).getOrElse("[unknown]")}"
//        }
//      },
//      test("Determining focus tree header works correctly (for shared focuses trees)") {
//        foreachFilteredSharedFocusFile() { sharedFocusFile =>
//          assertTrue(sharedFocusFilesToTest.contains(sharedFocusFile.getFile.getOrElse(false))) ?? s"Successfully verified focus tree header detection for file: ${sharedFocusFile.getFile.map(_.getName).getOrElse("[unknown]")}"
//        }
//      },
//      test("Shared focus files parse without exceptions") {
//        foreachFilteredSharedFocusFile() { sharedFocusFile =>
//          assertTrue(sharedFocusFile.sharedFocuses.nonEmpty)
//            ?? s"No shared focuses found in file: ${sharedFocusFile.fileNameOrElse("[unknown]")}"
//          &&
//          assertTrue(sharedFocusFile.sharedFocuses.head.id.isDefined)
//        }
//      },
//      test("Shared focus files contribute to Pseudo Shared Focus Tree") {
//        for {
//          pseudoTree <- PseudoSharedFocusTree()
//          pseudoTreeFocuses = pseudoTree.listFocuses
//          result <- foreachFilteredSharedFocusFile() { sharedFocusFile =>
//            (assertTrue(sharedFocusFile.sharedFocuses.nonEmpty) ??
//              s"No shared focuses found in file: ${sharedFocusFile.fileNameOrElse("[unknown]")}") &&
//              (assertTrue(pseudoTreeFocuses.nonEmpty) ??
//                s"No focuses found in PseudoSharedFocusTree after loading SharedFocusFile file: ${sharedFocusFile.fileNameOrElse("[unknown]")}") &&
//              (assertTrue(sharedFocusFile.sharedFocuses.forall(pseudoTreeFocuses.contains)) ??
//                s"Not all shared focuses of SharedFocusFile file ${sharedFocusFile.fileNameOrElse("[unknown]")} are present in PseudoSharedFocusTree")
//          }
//        } yield result
//      }
//    ).provide(
//      FocusTreeManager.live,
//      CountryTagService.live
//    )
//}
//
