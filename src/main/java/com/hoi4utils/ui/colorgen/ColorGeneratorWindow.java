package main.java.com.hoi4utils.ui.colorgen;

import javafx.fxml.FXML;
import javafx.scene.control.Slider;
import main.java.com.hoi4utils.ui.HOIIVUtilsWindow;

public class ColorGeneratorWindow extends HOIIVUtilsWindow {
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
