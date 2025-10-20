package com.hoi4utils.hoi4mod.map.province

import com.hoi4utils.hoi4mod.map.province.Province
import com.hoi4utils.parser.Node
import com.hoi4utils.script.IntPDX
import com.typesafe.scalalogging.LazyLogging
import org.apache.logging.log4j.{LogManager, Logger}

import scala.collection.mutable.ListBuffer

class Province extends IntPDX {
  override def set(expression: Node): Unit = {
    super.set(expression)
  }

  override def set(value: Int): Int = {
    super.set(value)
  }

  override def set(other: IntPDX): Unit = {
    throw new UnsupportedOperationException("Cannot set a Province to another IntPDX")
  }
  
  def id: Option[Int] = value
}

object Province extends LazyLogging {
  private val provinces = new ListBuffer[Province]()

  /**
   * Clears all provinces and any other relevant values.
   */
  def clear(): Unit = {
    provinces.clear()
  }
  
  private[map] def add(province: Province): List[Province] = {
    provinces += province
    provinces.toList
  }

  def list: List[Province] = provinces.toList

  def apply(id: Int): Province = {
    require(id >= 0, "Province id must be non-negative")
    val newProvince = new Province()
    newProvince.set(id)
    //        provinces += newProvince
    newProvince
//    provinces.find(_ @== id) match {
//      case Some(existing) =>
//        // Return the already-created instance
//        existing
//      case None =>
//        // Create and return a new Province
//        
//    }
  }
}
