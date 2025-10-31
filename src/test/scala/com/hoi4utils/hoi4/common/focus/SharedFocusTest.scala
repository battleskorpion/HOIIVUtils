package com.hoi4utils.hoi4.common.focus

import com.hoi4utils.hoi4.common.national_focus.{FocusTree, SharedFocusFile}
import com.hoi4utils.hoi4.common.national_focus.FocusTreeManager.hasFocusTreeHeader
import com.hoi4utils.shared.TestBase
import org.scalatest.funsuite.AnyFunSuiteLike

import java.io.File
import scala.collection.mutable.ListBuffer
import scala.util.{Failure, Success, Try}

class SharedFocusTest extends AnyFunSuiteLike {

	private val testPath = "src/test/resources/pdx/"
	private val sharedFocusFilesToTest = List(
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
	private val filesToTest: List[File] = List(
		new File(testPath + "minimichigantest.txt"),
		new File(testPath + "minimichigantest2.txt"),
		new File(testPath + "minimichigantest3.txt"),
	).appendedAll(sharedFocusFilesToTest)

	def withFocusTreeFiles(testFunction: FocusTree => Unit): Unit = {
		filesToTest.foreach { file =>
			if (hasFocusTreeHeader(file))
				Try(new FocusTree(file)) match
					case Success(focusTree) =>
						assert(focusTree != null, s"Focus tree somehow null for file: ${file.getPath}")
						testFunction(focusTree)
					case Failure(e) =>
						fail(s"Failed to parse file [${file.getPath}] - ${e.getClass.getSimpleName}: ${e.getMessage}\n" +
							s"Exception: ${e.getStackTrace.mkString("\n")}")
		}
	}

	def withPureSharedFocusFiles(testFunction: SharedFocusFile => Unit): Unit = {
		filesToTest.foreach { file =>
			if (!hasFocusTreeHeader(file))
				Try(new SharedFocusFile(file)) match
					case Success(sharedFocusFile) =>
						assert(sharedFocusFile != null, s"Shared focus file somehow null for file: ${file.getPath}")
						testFunction(sharedFocusFile)
					case Failure(e) =>
						fail(s"Failed to parse file [${file.getPath}] - ${e.getClass.getSimpleName}: ${e.getMessage}\n" +
							s"Exception: ${e.getStackTrace.mkString("\n")}")
		}
	}

	test("Shared focus files parse without exceptions") {
		withPureSharedFocusFiles { sharedFocusFile =>
			// Just parsing without exceptions is the test
			assert(sharedFocusFile.sharedFocuses.nonEmpty, s"No shared focuses found in file: ${sharedFocusFile.fileNameOrElse("[unknown]")}")

			info(s"Successfully parsed ${sharedFocusFile.sharedFocuses.size} shared focuses from file: ${sharedFocusFile.fileNameOrElse("[unknown]")}")
		}
	}

}
