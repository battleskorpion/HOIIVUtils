package com.hoi4utils.hoi4mod.map.province

import com.hoi4utils.hoi4mod.localization.{Localizable, Property}

import scala.collection.mutable



//
/**
 * localizable: victory point name
 *
 * TODO: Remove?
 */
class VictoryPoint protected(province: Int, var value: Int) extends Localizable {
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
//  this.province = Province.apply(province)
//  private[province] var province: Province = null
//
//  def value: Int = value
//
//  def setValue(value: Int): Unit = {
//    this.value = value
//  }
//
//  @NotNull override def getLocalizableProperties: Map[Property, String] = CollectionConverters.asScala(util.Map.of(Property.NAME, "VP_NAME"))
//
//  @NotNull override def getLocalizableGroup: Iterable[_ <: Localizable] = CollectionConverters.asScala(VictoryPoint.victoryPoints)
//}
//
//object VictoryPoint {
//  private[province] val victoryPoints = new util.ArrayList[VictoryPoint]
//
//  def of(province: Int, value: Int): VictoryPoint = {
//    import scala.collection.JavaConversions._
//    for (vp <- victoryPoints) {
//      if (vp.province.id == province) return vp
//    }
//    new VictoryPoint(province, value)
//  }
}
