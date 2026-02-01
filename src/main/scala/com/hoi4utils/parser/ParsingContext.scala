package com.hoi4utils.parser

import com.hoi4utils.script.PDXFile

import java.io.File

case class ParsingContext(file: File, line: Option[Int] = None, column: Option[Int] = None):
  def this(file: File, node: Node[?]) =
    this(file, node.identifierToken.map(_.line), node.identifierToken.map(_.column))

object ParsingContext:
  val Dummy = ParsingContext(null, None, None)
