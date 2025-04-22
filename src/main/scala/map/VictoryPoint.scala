package map



// todo fix!

//package com.hoi4utils.clausewitz.map.province
//
//import com.hoi4utils.clausewitz.localization.Localizable
//import com.hoi4utils.clausewitz.localization.Property
//import org.jetbrains.annotations.NotNull
//
//import scala.jdk.javaapi.CollectionConverters
//import java.util
//import java.util.{ArrayList, List, Map}
//import scala.collection.mutable.Map
//
//
///**
// * localizable: victory point name
// */
//class VictoryPoint protected(province: Int, private[province] var value: Int) extends Localizable {
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
//}
