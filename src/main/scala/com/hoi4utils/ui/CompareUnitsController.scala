package com.hoi4utils.ui

import com.hoi4utils.HOIIVFiles
import com.hoi4utils.hoi4.units.SubUnit
import com.hoi4utils.ui.HOIIVUtilsAbstractController
import com.hoi4utils.ui.javafx_ui.DiffViewPane
import javafx.fxml.FXML
import javafx.scene.layout.AnchorPane

import scala.collection.mutable
import scala.jdk.CollectionConverters._

class CompareUnitsController extends HOIIVUtilsAbstractController {

  setFxmlResource("CompareUnits.fxml")
  setTitle("Compare Units")
  @FXML
  var rootAnchorPane: AnchorPane = new AnchorPane()

  private var unitsDiffViewPane: DiffViewPane = _
  private val skipNullProperties = true

  /**
   * {@inheritDoc}
   */
  @FXML
  def initialize(): Unit = {
    val customUnits = SubUnit.read(HOIIVFiles.Mod.units_folder).asJava
    val baseUnits = SubUnit.read(HOIIVFiles.HOI4.units_folder).asJava

    unitsDiffViewPane = new DiffViewPane("Base Unit Details", "Custom Unit Details")
    rootAnchorPane.getChildren.add(unitsDiffViewPane)
    // set anchors
    AnchorPane.setTopAnchor(unitsDiffViewPane, 30.0)
    AnchorPane.setBottomAnchor(unitsDiffViewPane, 0.0)
    AnchorPane.setLeftAnchor(unitsDiffViewPane, 0.0)
    AnchorPane.setRightAnchor(unitsDiffViewPane, 0.0)

    /* add data */
    // custom unit
    val customUnitText = mutable.Buffer.empty[String]
    for (i <- 0 until customUnits.size) {
      val unit = customUnits.get(i)
      appendUnitDetails(customUnitText, unit)
      customUnitText.append("")
    }
    if (customUnits.isEmpty) customUnitText.append("No custom units found")

    // base unit
    val baseUnitText = mutable.Buffer.empty[String]
    for (i <- 0 until baseUnits.size) {
      val unit = baseUnits.get(i)
      appendUnitDetails(baseUnitText, unit)
      baseUnitText.append("")
    }
    if (baseUnits.isEmpty) baseUnitText.append("No base units found")

    // append
    unitsDiffViewPane.setData(baseUnitText.asJava, customUnitText.asJava)
  }

  private def appendUnitDetails(unitText: collection.mutable.Buffer[String], unit: SubUnit): Unit = {
    val df = SubUnit.dataFunctions()
    val dfl = SubUnit.dataLabels().asJava
    val maxLabelWidth = dfl.asScala.map(_.length).max

    for (i <- 0 until df.size) {
      val data = df.apply(i).apply(unit)
      if (skipNullProperties && data == null) {}
      else {
        val dataLabel = dfl.get(i)
        val spacing = " " * (maxLabelWidth - dataLabel.length)
        val str = s"$dataLabel: $spacing${if (data == null) "[null]" else data.toString}"
        unitText.append(str)
      }
    }
  }
}