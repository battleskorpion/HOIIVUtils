package com.hoi4utils.parser

import com.hoi4utils.parser.{Node, ParsingContext}
import com.hoi4utils.script.PDXFileError

import java.io.{File, PrintWriter, StringWriter}
import scala.collection.mutable.ListBuffer

case class ExpectedCause(expected: String):
  override def toString: String = "expected " + expected

  infix def actual(actual: String): String = s"$this, actual: $actual"

/**
 * PDXError represents an error encountered while parsing or processing a script file
 * todo add more details later
 */
class ParsingError(val error: String, val cause: String | ExpectedCause, val data: String)(using context: ParsingContext):

  def file: File = context.file
  def line: Option[Int] = context.line
  def column: Option[Int] = context.column

  override def toString: String = cause match
    case cause: String => s"$error: $cause (data: $data) at ${file.getPath}:${line.getOrElse("?")}${column.map(":" + _).getOrElse("")}"
    case expected: ExpectedCause => s"$error: ${expected actual data} at ${file.getPath}:${line.getOrElse("?")}${column.map(":" + _).getOrElse("")}"
