package com.hoi4utils.clausewitz.map.province

import org.jetbrains.annotations.NotNull
import org.apache.logging.log4j.{LogManager, Logger}

import scala.collection.mutable.ListBuffer

class Province private(private val _id: Int) extends Comparable[Province] {
  /* init */
  require(_id >= 0, "Province id must be non-negative")

  Province.add(this)

  def id = _id

  override def compareTo(o: Province): Int = this.id.compareTo(o.id)
}

object Province {
  val LOGGER: Logger = LogManager.getLogger(classOf[Province])
  private val provinces = new ListBuffer[Province]()

  /**
   * Clears all provinces and any other relevant values.
   */
  def clear(): Unit = {
    provinces.clear()
  }

  def add(province: Province): List[Province] = {
    provinces += province
    provinces.toList
  }

  def list: List[Province] = provinces.toList

  def apply(id: Int): Province = {
    require(id >= 0, "Province id must be non-negative")
    provinces.find(_.id == id) match {
      case Some(existing) =>
        // Return the already-created instance
        existing
      case None =>
        // Create, cache, and return a new Province
        val newProvince = new Province(id)
        provinces += newProvince
        newProvince
    }
  }
}
