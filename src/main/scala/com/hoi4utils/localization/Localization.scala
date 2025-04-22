package com.hoi4utils.localization

import com.hoi4utils.exceptions.UnexpectedLocalizationStatusException
import com.hoi4utils.localization.Localization.Status

import java.io.File

case class Localization(
                         ID: String,
                         version: Option[Int],
                         text: String,
                         status: Localization.Status
                       ) {

  /** Does this record allow itself to be replaced by `other`? */
  @throws[UnexpectedLocalizationStatusException]
  def isReplaceableBy(other: Localization): Boolean = status match {
    case Status.NEW =>
      if other.status != Status.NEW then
        throw UnexpectedLocalizationStatusException(this, other)
      else true

    case Status.EXISTS =>
      if other.status != Status.UPDATED then
        throw UnexpectedLocalizationStatusException(this, other)
      else true

    case Status.UPDATED =>
      if other.status != Status.UPDATED && other.status != Status.EXISTS then
        throw UnexpectedLocalizationStatusException(this, other)
      else true

    case Status.VANILLA => false
    case Status.MISSING => false
  }

  /** Can this localization be replaced by *any* new one? */
  def isReplaceable: Boolean = status match
    case Status.NEW | Status.EXISTS | Status.UPDATED => true
    case Status.VANILLA | Status.MISSING               => false

  def isNew: Boolean = status == Status.NEW

  /** Copy with new text; always bumps you to UPDATED. */
  def replaceWith(newText: String): Localization =
    copy(text = newText, status = Status.UPDATED)

  /**
   * Copy with new text and version, preserving NEW→NEW but EXISTS→UPDATED, etc.
   */
  @throws[UnexpectedLocalizationStatusException]
  def replaceWith(newText: String, newVersion: Option[Int], file: File): Localization = {
    val newStatus = status match
      case Status.NEW     => Status.NEW
      case Status.EXISTS  => Status.UPDATED
      case Status.UPDATED => Status.UPDATED
      case _               => throw UnexpectedLocalizationStatusException(this)
    copy(version = newVersion, text = newText, status = newStatus)
  }

  override def toString: String =
    s"$ID:${version.map(_.toString).getOrElse("")} \"$text\""

  /** If your key ends in "_desc", strip that suffix for the base key. */
  def baseKey: String =
    if ID.endsWith("_desc") then ID.stripSuffix("_desc") else ID
}

object Localization {
  enum Status {
    case EXISTS, NEW, UPDATED, VANILLA, MISSING
  }

  /** Java‐style overloads of your record‐constructors: */
  def apply(id: String, text: String, status: Status): Localization =
    Localization(id, None, text, status)

  def apply(id: String, text: String): Localization =
    Localization(id, None, text, Status.NEW)

  def apply(id: String, text: String, version: Int): Localization =
    Localization(id, Some(version), text, Status.NEW)
}
