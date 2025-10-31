package com.hoi4utils.parser

import java.io.File
import scala.annotation.targetName

class ParserException(message: String = null, cause: Throwable = null, pdx: Option[File | String] = None) extends Exception(
  // composed message
  {
    val base = Option(message).getOrElse("Parser error")
    val suffix = pdx match
      case Some(f: File) => s" [File: ${f.getAbsolutePath}]"
      case Some(s: String) => s" [Script: ${s.preview}]"
      case None => ""
    base + suffix
  },
  cause
)

extension (s: String)
  def preview(max: Int = 200): String =
    if s.length <= max then s else s.take(max) + "..."

object ParserException:
  def apply(msg: String, pdx: File | String) = new ParserException(msg, pdx = Some(pdx))
  @targetName("applyWithImplicit")
  def apply(msg: String)(using pdx: File | String) = new ParserException(msg, pdx = Some(pdx))
