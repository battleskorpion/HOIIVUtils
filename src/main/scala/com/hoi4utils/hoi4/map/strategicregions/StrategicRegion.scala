package com.hoi4utils.hoi4.map.strategicregions

import com.hoi4utils.hoi4.map.province.Province
import com.hoi4utils.script.*
import com.hoi4utils.script.datatype.StringPDX
import com.hoi4utils.script.seq.{ListPDX, MultiPDX}
import com.typesafe.scalalogging.LazyLogging
import javafx.collections.{FXCollections, ObservableList}

import java.io.File
import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.jdk.javaapi.CollectionConverters

class StrategicRegion(file: File = null) extends StructuredPDX("strategic_region") with PDXFile with LazyLogging {

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

  file match
    case null => // create empty StrategicRegion
    case _ =>
      require(file.exists && file.isFile, s"StrategicRegion $file does not exist or is not a file.")
      loadPDX(file)
      setFile(file)

  /**
   * @inheritdoc
   */
  override protected def childScripts: mutable.Seq[PDXScript[?, ?]] =
    ListBuffer(id, name, provinces, weather)

  def setFile(file: File): Unit =
    _strategicRegionFile = Some(file)

  override def getFile: Option[File] = _strategicRegionFile

  class Weather extends StructuredPDX("weather") {
    final val period = new MultiPDX[WeatherPeriod](None, Some(() => new WeatherPeriod()), "period")

    /**
     * @inheritdoc
     */
    override protected def childScripts: mutable.Seq[PDXScript[?, ?]] =
      ListBuffer(period)

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
      override protected def childScripts: mutable.Seq[PDXScript[?, ?]] = 
        ListBuffer(between, temperature, no_phenomenon, rain_light, rain_heavy, snow, blizzard, arctic_water, mud, sandstorm, min_snow_level)
    }
  }

}

object StrategicRegion extends LazyLogging {
  private val strategicRegions = new ListBuffer[StrategicRegion]

  def get(file: File): Option[StrategicRegion] =
    if (file == null) return None
    if (!strategicRegions.exists(_._strategicRegionFile.contains(file))) new StrategicRegion(file)
    strategicRegions.find(_._strategicRegionFile.contains(file))

  def observeStratRegions: ObservableList[StrategicRegion] =
    FXCollections.observableArrayList(CollectionConverters.asJava(strategicRegions))

  def clear(): Unit = strategicRegions.clear()

  def add(stratRegion: StrategicRegion): Iterable[StrategicRegion] =
    strategicRegions += stratRegion
    strategicRegions
}
