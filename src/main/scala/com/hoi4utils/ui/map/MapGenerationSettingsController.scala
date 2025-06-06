package com.hoi4utils.ui.map

import com.hoi4utils.clausewitz.map.{ProvinceDeterminationType, ProvinceGenConfig}
import com.hoi4utils.clausewitz.map.seed.SeedGenType
import com.hoi4utils.ui.HOIIVUtilsAbstractController
import javafx.fxml.FXML
import javafx.scene.control.{ChoiceBox, TextField}

// todo rename to province gen properties window or whatever
class MapGenerationSettingsController extends HOIIVUtilsAbstractController {

  @FXML var seaLevelTextField: TextField = _
  @FXML var numSeedsTextField: TextField = _
  @FXML var seedGenChoiceBox: ChoiceBox[SeedGenType] = _
  @FXML var provinceDeterminationChoiceBox: ChoiceBox[ProvinceDeterminationType] = _

  private var properties: ProvinceGenConfig = _

  setFxmlResource("MapGenerationSettings.fxml")
  setTitle("Map Generation Settings")

  /**
   * This constructor is used internally by javafx.
   * Use the primary constructor to create a new instance.
   * Then call open(Object...) to set the properties.
   */
  def this(properties: ProvinceGenConfig) = {
    this()
    this.properties = properties
  }

  /**
   * Initialize method called by JavaFX
   */
  @FXML
  def initialize(): Unit = {
    if (properties != null) {
      seaLevelTextField.setText(properties.seaLevel().toString)
      numSeedsTextField.setText(properties.numSeeds().toString)

      // default should be grid_seed
      seedGenChoiceBox.getItems.addAll(SeedGenType.values(): _*)
      seedGenChoiceBox.setValue(properties.generationType())

      provinceDeterminationChoiceBox.getItems.addAll(ProvinceDeterminationType.values(): _*)
      provinceDeterminationChoiceBox.setValue(properties.determinationType())
    }
  }

  @FXML
  private def onSetSeaLevel(): Unit = {
    // TODO don't actually change properties until apply, but update any preview
  }

  @FXML
  private def onSetNumSeeds(): Unit = {
    // TODO don't actually change properties until apply, but update any preview
  }

  @FXML
  private def onApplyChanges(): Unit = {
    val seaLevel = seaLevelTextField.getText.toInt
    val numSeeds = numSeedsTextField.getText.toInt
    val generationType = seedGenChoiceBox.getValue
    val determinationType = provinceDeterminationChoiceBox.getValue

    println(s"prev. seaLevel: ${properties.seaLevel()}, prev. numSeeds: ${properties.numSeeds()}")

    properties.setSeaLevel(seaLevel)
    properties.setNumSeeds(numSeeds)
    properties.setGenerationType(generationType)
    properties.setDeterminationType(determinationType)

    println(s"updated seaLevel: ${properties.seaLevel()}, updated numSeeds: ${properties.numSeeds()}")
  }

  // Method to set properties (equivalent to the open method pattern)
  def setProperties(config: ProvinceGenConfig): Unit = {
    this.properties = config
    if (seaLevelTextField != null) { // Check if initialize has been called
      initialize()
    }
  }
}