package com.hoi4utils.hoi4.common.national_focus

import com.hoi4utils.script.{HeadlessPDX, MultiPDX, PDXFile, PDXScript, StructuredPDX}
import dotty.tools.sjs.ir.Trees.JSBinaryOp.&&

import java.io.File
import scala.collection.mutable
import scala.collection.mutable.ListBuffer

class SharedFocusFile(_file: Option[File]) extends StructuredPDX with HeadlessPDX with PDXFile {

	/* init */
	_file match
		case Some(f) =>
			require(f.exists && f.isFile, s"Shared focuses file $f does not exist or is not a file.")
			loadPDX(f)
		case None =>

	def this(file: File) = this(Some(file))

	/* PDX attributes */
	val sharedFocuses: MultiPDX[SharedFocus] = MultiPDX[SharedFocus](None, Some(() => SharedFocus()), "shared_focus")

	override protected def childScripts: mutable.Iterable[? <: PDXScript[?]] = ListBuffer(sharedFocuses)

	override def getFile: Option[File] = _file

}
