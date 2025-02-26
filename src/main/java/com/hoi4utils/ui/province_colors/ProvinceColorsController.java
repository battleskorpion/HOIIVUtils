package com.hoi4utils.ui.province_colors;

import com.hoi4utils.clausewitz.HOIIVUtils;
import com.hoi4utils.ui.HOIIVUtilsWindow;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.shape.Rectangle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ProvinceColorsController extends HOIIVUtilsWindow {
	public static final Logger LOGGER = LogManager.getLogger(ProvinceColorsController.class);
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
	private String input = "1000";

	public ProvinceColorsController() {
		setFxmlResource("ProvinceColors.fxml");
		setTitle("HOIIVUtils Province Colors");
	}

	@FXML
	void initialize() {
		idWindowName.setText("Province Colors - Unique Color Generator");
		colorInputField.setText(input); // Set default input value
		progressIndicator.setVisible(false); // Hide progress indicator initially
	}
	@FXML
	private void handleColorInputField() {
		input = colorInputField.getText();

		// Validate input
		if (input.isEmpty() || !input.matches("\\d+")) {
			statusLabel.setText("Error: Please enter a valid number.");
			return;
		}

		int numColors = Integer.parseInt(input);

		if (numColors <= 0) {
			statusLabel.setText("Error: Number of colors must be positive.");
			return;
		}

		// Update the color preview
		updateColorPreview(numColors);

		statusLabel.setText("Ready to generate BMP with " + numColors + " unique colors.");
	}

	/**
	 * Handler for "Generate BMP" button click.
	 */
	@FXML
	private void handleGenerateButton() {
		input = colorInputField.getText();

		// Validate input
		if (input.isEmpty() || !input.matches("\\d+")) {
			statusLabel.setText("Error: Please enter a valid number.");
			return;
		}

		int numColors = Integer.parseInt(input);

		if (numColors <= 0) {
			statusLabel.setText("Error: Number of colors must be positive.");
			return;
		}

		// Update the color preview
		updateColorPreview(numColors);

		// Show progress indicator
		statusLabel.setText("Generating BMP with " + numColors + " unique colors...");
		progressIndicator.setVisible(true);

		// Run BMP generation on a background thread
		new Thread(() -> {
			try {
				String outputPath = HOIIVUtils.HOIIVUTILS_DIR + File.separator + "output.bmp";
				generateBMP(numColors, outputPath);

				// Update UI on completion
				javafx.application.Platform.runLater(() -> {
					progressIndicator.setVisible(false);
					statusLabel.setText("BMP generated successfully: " + outputPath);
					LOGGER.info("Generated BMP with " + numColors + " unique colors: " + outputPath);
				});
			} catch (Exception e) {
				e.printStackTrace();
				javafx.application.Platform.runLater(() -> {
					progressIndicator.setVisible(false);
					statusLabel.setText("Error: Failed to generate BMP.");
				});
			}
		}).start();
	}

	/**
	 * Generates a BMP file with a specified number of unique colors.
	 *
	 * @param numColors  The total number of unique colors to generate.
	 * @param outputPath The file path to save the BMP.
	 */
	private void generateBMP(int numColors, String outputPath) throws IOException {
		// Calculate closest square dimensions
		int dimension = (int) Math.ceil(Math.sqrt(numColors));
		BufferedImage image = new BufferedImage(dimension, dimension, BufferedImage.TYPE_INT_RGB);

		// Generate unique colors and fill the image
		int colorIndex = 0;
		for (int y = 0; y < dimension; y++) {
			for (int x = 0; x < dimension; x++) {
				if (colorIndex < numColors) {
					Color color = generateUniqueColor(colorIndex, numColors);
					image.setRGB(x, y, color.getRGB());
					colorIndex++;
				} else {
					image.setRGB(x, y, Color.BLACK.getRGB()); // Fill remaining pixels with black
				}
			}
		}

		// Save the BMP file
		File outputFile = new File(outputPath);
		ImageIO.write(image, "bmp", outputFile);
	}

	/**
	 * Generates a unique color based on the index.
	 *
	 * @param index       The current color index.
	 * @param totalColors The total number of colors to generate.
	 * @return A unique Color object.
	 */
	private Color generateUniqueColor(int index, int totalColors) {
		// Simple RGB color distribution logic
		int r = (index * 53) % 256;  // Red channel
		int g = (index * 97) % 256;  // Green channel
		int b = (index * 193) % 256; // Blue channel
		return new Color(r, g, b);
	}

	/**
	 * Updates the color preview in the UI with the specified number of unique colors.
	 *
	 * @param numColors Number of unique colors to display.
	 */
	private void updateColorPreview(int numColors) {
		colorPreviewGrid.getChildren().clear(); // Clear previous preview

		int columns = (int) Math.ceil(Math.sqrt(numColors)); // Number of columns in the preview grid
		int boxSize = 8; // Size of each color box (in pixels)

		for (int i = 0; i < Math.min(numColors, 1000000); i++) { // Limit preview to 1000000 colors
			// Generate unique color
			Color color = generateUniqueColor(i, numColors);

			// Create a rectangle with the color
			Rectangle rect = new Rectangle(boxSize, boxSize);
			rect.setFill(javafx.scene.paint.Color.rgb(color.getRed(), color.getGreen(), color.getBlue()));
			rect.setStroke(javafx.scene.paint.Color.BLACK); // Border for visibility

			// Add the rectangle to the GridPane
			int row = i / columns;
			int col = i % columns;
			colorPreviewGrid.add(rect, col, row);
		}
	}
}
