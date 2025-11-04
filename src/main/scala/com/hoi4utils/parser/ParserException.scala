package com.hoi4utils.parser

import java.io.File
import scala.annotation.targetName

class ParserException(
  message: String = null,
  cause: Throwable = null,
  pdx: Option[File | String] = None,
  val token: Option[Token] = None  // Store the problematic token with line/column info
) extends Exception(
  // composed message
  {
    val base = Option(message).getOrElse("Parser error")
    val locationSuffix = token match
      case Some(t) => s" [Line ${t.line}, Column ${t.column}]"
      case None => ""
    val fileSuffix = pdx match
      case Some(f: File) => s" [File: ${f.getAbsolutePath}]"
      case Some(s: String) => s" [Script: ${s.preview}]"
      case None => ""
    base + locationSuffix + fileSuffix
  },
  cause
)

extension (s: String)
  def preview(max: Int = 200): String =
    if s.length <= max then s else s.take(max) + "..."

object ParserException:
  def apply(msg: String, pdx: File | String) = new ParserException(msg, pdx = Some(pdx))

  def apply(msg: String, token: Token, pdx: File | String) =
    new ParserException(msg, token = Some(token), pdx = Some(pdx))

  @targetName("applyWithImplicit")
  def apply(msg: String)(using pdx: File | String) = new ParserException(msg, pdx = Some(pdx))

  @targetName("applyWithTokenAndImplicit")
  def apply(msg: String, token: Token)(using pdx: File | String) =
    new ParserException(msg, token = Some(token), pdx = Some(pdx))
