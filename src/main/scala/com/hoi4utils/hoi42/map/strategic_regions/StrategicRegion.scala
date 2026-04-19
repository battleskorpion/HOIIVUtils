package com.hoi4utils.hoi42.map.strategic_regions

import com.hoi4utils.hoi42.map.province.Province
import com.hoi4utils.script2.*

import java.io.File

class StrategicRegion(var stratRegions: StrategicRegionRegistry, var file: Option[File]) extends PDXEntity with IDReferable[Int]
  with RegistryMember[StrategicRegion](stratRegions):

  val id: PDXProperty[Int] = pdx[Int]("id")
  val name = pdx[String]("name")
  val provinces = pdxList[Province]("provinces")
  val weather = pdx[Weather]("weather")

object StrategicRegion { }

class StrategicRegionRegistry extends Registry[StrategicRegion] {

  override def idDecoder: PDXDecoder[Int] = summon[PDXDecoder[Int]]
}

class Weather extends PDXEntity:
  val period = pdxList[WeatherPeriod]("period")

class WeatherPeriod extends PDXEntity:
  val between = pdxList[Double]("between")
  val temperature = pdxList[Double]("temperature")
  val noPhenomenon = pdx[Double]("no_phenomenon")
  val rainLight = pdx[Double]("rain_light")
  val rainHeavy = pdx[Double]("rain_heavy")
  val snow = pdx[Double]("snow")
  val blizzard = pdx[Double]("blizzard")
  val arcticWater = pdx[Double]("arctic_water")
  val mud = pdx[Double]("mud")
  val sandstorm = pdx[Double] ("sandstorm")
  val minSnowLevel = pdx[Double]("min_snow_level")
