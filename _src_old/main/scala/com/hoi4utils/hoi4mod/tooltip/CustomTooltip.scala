package com.hoi4utils.hoi4mod.tooltip

import com.hoi4utils.hoi4mod.localization.Localization
import com.hoi4utils.parser.{Node, Parser, ParserException}

import java.io.File
import scala.collection.mutable.ListBuffer

/**
 * Represents one custom tooltip (by its ID), plus optional localization.
 */
class CustomTooltip(val id: String) {
  private var _loc: Option[Localization] = None

  /** Raw tooltip ID (the key). */
  def tooltipID: String = id

  /** Attach the Localization record for this tooltip. */
  def setLocalization(loc: Localization): Unit =
    _loc = Option(loc)

  /** The Localization, if any, for this tooltip. */
  def localization: Option[Localization] = _loc

  /** The resolved text, if we have a Localization attached. */
  def text: Option[String] = _loc.map(_.text)

  override def toString: String = Option(id).getOrElse(super.toString)
}

object CustomTooltip {
  private var tooltips: ListBuffer[CustomTooltip] = ListBuffer.empty

  /**
   * Two data‐accessors: the ID and the resolved text.
   * Replace `Any` with a more precise type if desired.
   */
  def dataFunctions(): Iterable[CustomTooltip => ?] = List(
    _.tooltipID,
    _.text.orNull
  )

  /** Load all `custom_trigger_tooltip` nodes from the given file. */
  def loadTooltips(file: File): Unit = {
    tooltips = ListBuffer.empty[CustomTooltip]

    if (!validFile(file)) return

    val rootNode: Node = try {
      new Parser(file).parse
    } catch {
      case e: ParserException => throw new RuntimeException(e)
    }

    // collect all tooltip IDs
    val expressions: List[Node] =
      rootNode.filter("custom_trigger_tooltip").toList

    for {
      exp   <- expressions
      if exp.contains("tooltip")                   // only those that define a tooltip
      expID <- Option(exp.getValue("tooltip").$stringOrElse(null))
    } tooltips += new CustomTooltip(expID)
  }

  private def validFile(f: File): Boolean =
    Option(f).exists(f => f.exists && f.isFile)

  def getTooltips: Iterable[CustomTooltip] =
    if (tooltips.nonEmpty) tooltips else Nil
}