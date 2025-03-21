package com.hoi4utils.clausewitz.map

import com.hoi4utils.clausewitz.map.province.Province
import org.apache.logging.log4j.{LogManager, Logger}
import com.hoi4utils.clausewitz.script.{DoublePDX, IntPDX, ListPDX, MultiPDX, PDXFile, PDXScript, StringPDX, StructuredPDX}
import javafx.collections.{FXCollections, ObservableList}

import java.io.File
import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.jdk.javaapi.CollectionConverters

class StrategicRegion extends StructuredPDX("strategic_region") with PDXFile {
  private val LOGGER: Logger = LogManager.getLogger(getClass)

  final val id = new IntPDX("id")
  final val name = new StringPDX("name")
  final val provinces = {
    val loadNewProvince = () => {
      val p = new Province(); Province.add(p); p
    }
    new ListPDX[Province](loadNewProvince, "provinces")
  }
  final val weather = new Weather()

  private var _strategicRegionFile: Option[File] = None

  /* init */
  StrategicRegion.add(this)

  def this(file: File) = {
    this()
    loadPDX(file)
    setFile(file)
  }

  /**
   * @inheritdoc
   */
  override protected def childScripts: mutable.Iterable[PDXScript[?]] = {
    ListBuffer(id, name, provinces, weather)
  }

  def setFile(file: File): Unit = {
    _strategicRegionFile = Some(file)
  }

  override def getFile: Option[File] = _strategicRegionFile

  class Weather extends StructuredPDX("weather") {
    final val period = new MultiPDX[WeatherPeriod](None, Some(() => new WeatherPeriod()), "period")

    /**
     * @inheritdoc
     */
    override protected def childScripts: mutable.Iterable[PDXScript[?]] = {
      ListBuffer(period)
    }

    class WeatherPeriod extends StructuredPDX("period") {
      final val between = new ListPDX[DoublePDX](() => new DoublePDX(), "between")
      final val temperature = new ListPDX[DoublePDX](() => new DoublePDX(), "temperature")
      final val no_phenomenon = new DoublePDX("no_phenomenon")
      final val rain_light = new DoublePDX("rain_light")
      final val rain_heavy = new DoublePDX("rain_heavy")
      final val snow = new DoublePDX("snow")
      final val blizzard = new DoublePDX("blizzard")
      final val arctic_water = new DoublePDX("arctic_water")
      final val mud = new DoublePDX("mud")
      final val sandstorm = new DoublePDX("sandstorm")
      final val min_snow_level = new DoublePDX("min_snow_level")

      /**
       * @inheritdoc
       */
      override protected def childScripts: mutable.Iterable[PDXScript[?]] = {
        ListBuffer(between, temperature, no_phenomenon, rain_light, rain_heavy, snow, blizzard, arctic_water, mud, sandstorm, min_snow_level)
      }
    }
  }

}

object StrategicRegion {
  private val LOGGER: Logger = LogManager.getLogger(getClass)

  private val strategicRegions = new ListBuffer[StrategicRegion]

  def get(file: File): Option[StrategicRegion] = {
    if (file == null) return None
    if (!strategicRegions.exists(_._strategicRegionFile.contains(file))) new StrategicRegion(file)
    strategicRegions.find(_._strategicRegionFile.contains(file))
  }

  def observeStates: ObservableList[StrategicRegion] = {
    FXCollections.observableArrayList(CollectionConverters.asJava(strategicRegions))
  }

  def clear(): Unit = {
    strategicRegions.clear()
  }

  def add(stratRegion: StrategicRegion): Iterable[StrategicRegion] = {
    strategicRegions += stratRegion
    strategicRegions
  }
}
