package com.HOIIVUtils.ui.colorgen;

import javafx.fxml.FXML;
import javafx.scene.control.Slider;
import com.HOIIVUtils.ui.HOIIVUtilsStageLoader;

public class ColorGeneratorWindow extends HOIIVUtilsStageLoader {
	public ColorGeneratorWindow() {
		setFxmlResource("ColorGeneratorWindow.fxml");
		setTitle("Color Generator");
	}

	// * Color Generator Window Controller
	@FXML
	private Slider redMinSlider;
	@FXML
	private Slider redMaxSlider;
	@FXML
	private Slider greenMinSlider;
	@FXML
	private Slider greenMaxSlider;
	@FXML
	private Slider blueMinSlider;
	@FXML
	private Slider blueMaxSlider;

	@FXML
	void initialize() {

	}
}
