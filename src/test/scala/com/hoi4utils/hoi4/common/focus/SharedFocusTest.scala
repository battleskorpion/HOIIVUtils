package com.hoi4utils.hoi4.common.focus

import com.hoi4utils.hoi4.common.national_focus.{FocusTree, PseudoSharedFocusTree, SharedFocusFile}
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

	test("Determing focus tree header works correctly") {
		withFocusTreeFiles { focusTree =>
			assert(!sharedFocusFilesToTest.contains(focusTree.getFile))

			info(s"Successfully verified focus tree header detection for file: ${focusTree.getFile.map(_.getName).getOrElse("[unknown]")}")
		}
		withPureSharedFocusFiles { sharedFocusFile =>
			assert(sharedFocusFilesToTest.contains(sharedFocusFile.getFile.getOrElse(false)))

			info(s"Successfully verified focus tree header detection for file: ${sharedFocusFile.getFile.map(_.getName).getOrElse("[unknown]")}")
		}
	}

	test("Shared focus files parse without exceptions") {
		withPureSharedFocusFiles { sharedFocusFile =>
			assert(sharedFocusFile.sharedFocuses.nonEmpty, s"No shared focuses found in file: ${sharedFocusFile.fileNameOrElse("[unknown]")}")
			assert(sharedFocusFile.sharedFocuses.head.id.isDefined)

			info(s"Successfully parsed ${sharedFocusFile.sharedFocuses.size} shared focuses from file: ${sharedFocusFile.fileNameOrElse("[unknown]")}")
		}
	}

	test("Shared focus files contribute to Pseudo Shared Focus Tree") {
		withPureSharedFocusFiles { sharedFocusFile =>
			assert(sharedFocusFile.sharedFocuses.nonEmpty, s"No shared focuses found in file: ${sharedFocusFile.fileNameOrElse("[unknown]")}")
			val pseudoTreeFocuses = PseudoSharedFocusTree().listFocuses
			assert(pseudoTreeFocuses.nonEmpty, s"No focuses found in PseudoSharedFocusTree after loading SharedFocusFile file: ${sharedFocusFile.fileNameOrElse("[unknown]")}")
			assert(sharedFocusFile.sharedFocuses.forall(pseudoTreeFocuses.contains), s"Not all shared focuses of SharedFocusFile file ${sharedFocusFile.fileNameOrElse("[unknown]")} are present in PseudoSharedFocusTree")

			info(s"Successfully verified that ${sharedFocusFile.sharedFocuses.size} shared focuses from file: ${sharedFocusFile.fileNameOrElse("[unknown]")} are present in PseudoSharedFocusTree")
		}
	}

}
