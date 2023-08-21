package ui.colorgen;

import javafx.fxml.FXML;
import javafx.scene.control.Slider;
import ui.HOIUtilsWindow;

public class ColorGeneratorWindow extends HOIUtilsWindow {
	public ColorGeneratorWindow() {
		fxmlResource = "ColorGeneratorWindow.fxml";
		title = "Color Generator";
	}

	@Override
	public void open() {
		super.open();
	}



	// * Color Generator Window Controller
	@FXML private Slider redMinSlider;
	@FXML private Slider redMaxSlider;
	@FXML private Slider greenMinSlider;
	@FXML private Slider greenMaxSlider;
	@FXML private Slider blueMinSlider;
	@FXML private Slider blueMaxSlider;

	@FXML
	void initialize() {

	}
}
