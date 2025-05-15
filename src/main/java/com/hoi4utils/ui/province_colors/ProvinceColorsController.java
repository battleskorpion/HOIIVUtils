package com.hoi4utils.ui.province_colors;

import com.hoi4utils.HOIIVUtils;
import com.hoi4utils.clausewitz.map.gen.ColorGenerator;
import com.hoi4utils.ui.HOIIVUtilsAbstractController;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.control.Slider;
import javafx.scene.layout.GridPane;
import javafx.scene.shape.Rectangle;
import lombok.Setter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

import static javafx.scene.paint.Color.BLACK;
import static javafx.scene.paint.Color.rgb;

public class ProvinceColorsController extends HOIIVUtilsAbstractController {
	public static final Logger LOGGER = LogManager.getLogger(ProvinceColorsController.class);

    // Getters and setters for color ranges
    // Color constraints from old code
	@Setter
    private static int redMin = 0;
	@Setter
    private static int redMax = 255;
	@Setter
    private static int greenMin = 0;
	@Setter
    private static int greenMax = 255;
	@Setter
    private static int blueMin = 0;
	@Setter
    private static int blueMax = 255;

	@FXML
	public Label idWindowName;
	@FXML
	private TextField colorInputField; // Input for number of colors
	@FXML
	private Button generateButton; // Button to trigger BMP generation
	@FXML
	private Label statusLabel; // Status feedback to user
	@FXML
	private ProgressIndicator progressIndicator; // Indicates ongoing generation process
	@FXML
	private GridPane colorPreviewGrid; // GridPane for color preview

	// Sliders for color ranges
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

	// Labels for slider values
	@FXML
	private Label minRedAmtLabel;
	@FXML
	private Label maxRedAmtLabel;
	@FXML
	private Label minGreenAmtLabel;
	@FXML
	private Label maxGreenAmtLabel;
	@FXML
	private Label minBlueAmtLabel;
	@FXML
	private Label maxBlueAmtLabel;

	private String input = "65536";


	private final String outputPath = HOIIVUtils.get("hDir") + File.separator + "Generated Province Colors.bmp";

	public ProvinceColorsController() {
		setFxmlResource("ProvinceColors.fxml");
		setTitle("HOIIVUtils Province Colors");
	}

	@FXML
	void initialize() {
		idWindowName.setText("Province Colors - Unique Color Generator");
		colorInputField.setText(input); // Set default input value
		progressIndicator.setVisible(false); // Hide progress indicator initially

		// Initialize sliders
		setupSliders();
	}

	/**
	 * TODO: Fix Sliders to work with colorGenerator Example, if max red is 0 and min red is 0, then generate no red.
	 * Sets up the sliders with listeners and initial values
	 */
	private void setupSliders() {
		// Initialize sliders with default values
		redMinSlider.setValue(redMin);
		redMaxSlider.setValue(redMax);
		greenMinSlider.setValue(greenMin);
		greenMaxSlider.setValue(greenMax);
		blueMinSlider.setValue(blueMin);
		blueMaxSlider.setValue(blueMax);

		// Initialize labels
		minRedAmtLabel.setText(Integer.toString(redMin));
		maxRedAmtLabel.setText(Integer.toString(redMax));
		minGreenAmtLabel.setText(Integer.toString(greenMin));
		maxGreenAmtLabel.setText(Integer.toString(greenMax));
		minBlueAmtLabel.setText(Integer.toString(blueMin));
		maxBlueAmtLabel.setText(Integer.toString(blueMax));

		// Add listeners to sliders
		redMinSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
			redMin = newValue.intValue();
			minRedAmtLabel.setText(Integer.toString(redMin));
			updateColorPreview(getNumColorsGenerate());
		});

		redMaxSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
			redMax = newValue.intValue();
			maxRedAmtLabel.setText(Integer.toString(redMax));
			updateColorPreview(getNumColorsGenerate());
		});

		greenMinSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
			greenMin = newValue.intValue();
			minGreenAmtLabel.setText(Integer.toString(greenMin));
			updateColorPreview(getNumColorsGenerate());
		});

		greenMaxSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
			greenMax = newValue.intValue();
			maxGreenAmtLabel.setText(Integer.toString(greenMax));
			updateColorPreview(getNumColorsGenerate());
		});

		blueMinSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
			blueMin = newValue.intValue();
			minBlueAmtLabel.setText(Integer.toString(blueMin));
			updateColorPreview(getNumColorsGenerate());
		});

		blueMaxSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
			blueMax = newValue.intValue();
			maxBlueAmtLabel.setText(Integer.toString(blueMax));
			updateColorPreview(getNumColorsGenerate());
		});
	}

	/**
	 * Gets the number of colors to generate from the input field
	 * @return The number of colors to generate
	 */
	private int getNumColorsGenerate() {
		try {
			int numColors = Integer.parseInt(colorInputField.getText());

			if (numColors <= 0) {
				statusLabel.setText("Error: Number of colors must be positive.");
				return 0;
			}

			// Calculate the maximum possible colors within the RGB constraints
			int redRange = Math.max(1, redMax - redMin + 1);
			int greenRange = Math.max(1, greenMax - greenMin + 1);
			int blueRange = Math.max(1, blueMax - blueMin + 1);
			long maxPossibleColors = (long) redRange * greenRange * blueRange;

			// Check if exceeding maximum possible unique colors based on constraints
			if (numColors > maxPossibleColors) {
				numColors = (int) maxPossibleColors;
				statusLabel.setText("Warning: Limited to " + numColors + " colors based on RGB ranges.");
                LOGGER.warn("Requested more colors than possible with current RGB ranges. Limited to {}", numColors);
			}

			// Also check the absolute maximum (16.7 million)
			if (numColors > (1 << 24) - 1) {
				numColors = (1 << 24) - 1;
				statusLabel.setText("Warning: Limited to maximum of " + numColors + " unique colors.");
                LOGGER.warn("Requested more colors than possible (24-bit RGB). Limited to {}", numColors);
			}

			return numColors;
		} catch (NumberFormatException e) {
			statusLabel.setText("Error: Please enter a valid number.");
			return 0;
		}
	}

	@FXML
	private void handleColorInputField() {
		input = colorInputField.getText();
		int numColors = getNumColorsGenerate();

		if (numColors > 0) {
			// Update the color preview
			updateColorPreview(numColors);
			statusLabel.setText("Ready to generate BMP with " + numColors + " unique colors.");
		}
	}

	/**
	 * Handler for "Generate BMP" button click.
	 */
	@FXML
	private void handleGenerateButton() {
		int numColors = getNumColorsGenerate();

		if (numColors <= 0) {
			return; // Error already displayed in getNumColorsGenerate
		}

		// Show progress indicator
		statusLabel.setText("Generating BMP with " + numColors + " unique colors...");
		progressIndicator.setVisible(true);
		generateButton.setDisable(true);

		ColorGenerator colorGenerator = new ColorGenerator();

		// Run BMP generation on a background thread
		new Thread(() -> {
			try {
				
				colorGenerator.generateColors(numColors, outputPath);

				// Update UI on completion
				Platform.runLater(() -> {
					progressIndicator.setVisible(false);
					generateButton.setDisable(false);
					statusLabel.setText("BMP generated successfully: " + outputPath);
                    LOGGER.info("Generated BMP with {} unique colors: {}", numColors, outputPath);
				});
			} catch (Exception e) {
				LOGGER.fatal("Error generating BMP", e);
				Platform.runLater(() -> {
					progressIndicator.setVisible(false);
					generateButton.setDisable(false);
					statusLabel.setText("Error: Failed to generate BMP - " + e.getMessage());
					LOGGER.error("Failed to generate BMP", e);
				});
			}
		}).start();
	}

	/**
	 * TODO: Fix Preview
	 * Updates the color preview in the UI with the specified number of unique colors.
	 *
	 * @param numColors Number of unique colors to display.
	 */
	private void updateColorPreview(int numColors) {
		if (numColors <= 0) return;

		colorPreviewGrid.getChildren().clear();

		int columns = (int) Math.ceil(Math.sqrt(numColors));
		int boxSize = 8;
		int previewLimit = Math.min(numColors, 1000);

		for (int i = 0; i < previewLimit; i++) {
			// Create a rectangle with the color
			Rectangle rect = new Rectangle(boxSize, boxSize);
			// TODO: Get some colors from colorGenerator to be preview here, somehow?
			int rColor = 0;
			int gColor = 0;
			int bColor = 0;
			rect.setFill(rgb(rColor, gColor, bColor));
			rect.setStroke(BLACK);

			// Add the rectangle to the GridPane
			int row = i / columns;
			int col = i % columns;
			colorPreviewGrid.add(rect, col, row);
		}
	}
}