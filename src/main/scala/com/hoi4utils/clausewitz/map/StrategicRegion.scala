package com.hoi4utils.clausewitz.map

import com.hoi4utils.clausewitz.map.province.Province
import org.apache.logging.log4j.{LogManager, Logger}
import com.hoi4utils.clausewitz.script.{IntPDX, ListPDX, PDXScript, StringPDX, StructuredPDX}
import javafx.collections.{FXCollections, ObservableList}

import java.io.File
import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.jdk.javaapi.CollectionConverters

class StrategicRegion extends StructuredPDX("strategic_region") {
  private val LOGGER: Logger = LogManager.getLogger(getClass)

  final val id = new IntPDX("id")
  final val name = new StringPDX("name")
  final val provinces = {
    val loadNewProvince = () => {
      val p = new Province(); Province.add(p); p
    }
    new ListPDX[Province](loadNewProvince, "provinces")
  }
  
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
    ListBuffer(id, name, provinces)
  }

  def setFile(file: File): Unit = {
    _strategicRegionFile = Some(file)
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
