package com.hoi4utils.clausewitz.script

import ammonite.Main
import ammonite.util.Colors

import java.io.InputStream
import java.util.concurrent.{BlockingQueue, LinkedBlockingQueue}
import java.io.OutputStream
import java.io.PrintStream
import javafx.application.Platform
import javafx.scene.control.TextArea

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.reflect.ClassTag

object ScalaEmbeddedRepl {
  /**
   * Opens an Ammonite REPL, binding the provided pdxScripts so that
   * the user can manipulate them interactively.
   */
  def openRepl(
    consoleTextAreaStream: TextAreaPrintStream,
    errorTextAreaStream: TextAreaPrintStream,
    lineQueue: LinkedBlockingQueue[String],
    pdxScripts: Iterable[PDXScript[?]]
  ): Unit = {

    // We run this in a Future so it doesn't block the UI thread
    Future {
      val in = new BlockingQueueInputStream(lineQueue)
      val out = consoleTextAreaStream
      val err = errorTextAreaStream

      val customPredefCode =
        """
          |// Hide the default Predef print/println
          |import scala.Predef.{print => _, println => _}
          |import com.hoi4utils.clausewitz.script._
          |import scala.reflect.ClassTag
          |
          |// Define new ones that call 'replOut'
          |//def print(x: Any)   = replOut.print(x.toString)
          |//def println(x: Any) = replOut.println(x.toString)
          |//def println()       = replOut.println()
          |
          |//System.setIn(inStream) // does not work :(
          |//System.setOut(outStream)
          |
          |def toType[T <: PDXScript[?]](list: Iterable[PDXScript[?]])(implicit ct: ClassTag[T]): Iterable[T] =
          |  list.collect { case t if ct.runtimeClass.isInstance(t) => t.asInstanceOf[T] }
        """.stripMargin

      // Create an Ammonite REPL with your custom streams and a custom welcome banner.
      val repl = ammonite.Main(
        predefCode = customPredefCode,
        //inputStream = in,     // doesnt work well // doesnt work at all
        //outputStream = out,   // doesnt work well
        errorStream = err,    // doesnt work well
        welcomeBanner = Some(
          """Welcome to the HOI4Utils Debug REPL!
            |Type `pdxScripts` to see the loaded scripts.
            |Use the 'toType' function to filter the loaded scripts into the desired PDXScript class type.
            |Type `exit` or press Ctrl+D to quit.
            |""".stripMargin
        )
      )

      // Bind your pdxScripts to the REPL
      repl.run(
        "replOut" -> consoleTextAreaStream,
        "inStream" -> in,
        "outStream" -> out,
        "pdxScripts" -> pdxScripts,
      )
    }
  }

}

class BlockingQueueInputStream(lineQueue: BlockingQueue[String]) extends InputStream {
  private var currentBytes: Array[Byte] = Array.empty
  private var pos = 0

  override def read(): Int = {
    if (pos >= currentBytes.length) {
      // Blocks until a new line is available
      val line = lineQueue.take()
      currentBytes = (line + "\n").getBytes("UTF-8")
      pos = 0
    }
    val b = currentBytes(pos)
    pos += 1
    b & 0xff
  }

  override def available(): Int = {
    if (pos < currentBytes.length) {
      currentBytes.length - pos
    } else {
      0
    }
  }


}


class TextAreaPrintStream(textArea: TextArea) extends PrintStream(new OutputStream {
  private val sb = new StringBuilder()

  override def write(b: Int): Unit = {
    sb.append(b.toChar)
    if (b == '\n') {
      flushBuffer()
    }
  }

  override def flush(): Unit = flushBuffer()

  private def flushBuffer(): Unit = {
    val text = sb.toString
    sb.setLength(0)
    Platform.runLater(() => textArea.appendText(text))
  }
})