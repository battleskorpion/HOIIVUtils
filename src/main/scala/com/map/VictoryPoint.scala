package com.map

import com.hoi4utils.localization.{Localizable, Property}

import java.util
import scala.collection.mutable
import scala.collection.mutable.ListBuffer

/**
 * localizable: victory point name
 *
 * TODO: implement
 */
class VictoryPoint protected(var province: Int = 0, var value: Int = 0) extends Localizable:
  
  /**
   * Default method to get the localizable property identifiers and keys.
   *
   * @return a map of localizable property identifiers and keys.
   */
  override def getLocalizableProperties: mutable.Map[Property, String] = ???

  /**
   * Gets the localizable group of objects that this object is a part of.
   *
   * @return the localizable group of objects.
   */
  override def getLocalizableGroup: Iterable[? <: Localizable] = ???


object VictoryPoint:
  var victoryPoints: ListBuffer[VictoryPoint] = ListBuffer.empty
  var victoryPointErrors: ListBuffer[String] = ListBuffer.empty

