package com.hoi4utils.ui.province_colors;

import com.hoi4utils.clausewitz.HOIIVUtils;
import com.hoi4utils.ui.HOIIVUtilsAbstractController;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.control.Slider;
import javafx.scene.layout.GridPane;
import javafx.scene.shape.Rectangle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class ProvinceColorsController extends HOIIVUtilsAbstractController {
	public static final Logger LOGGER = LogManager.getLogger(ProvinceColorsController.class);

	// Color constraints from old code
	private static int redMin = 0;
	private static int redMax = 255;
	private static int greenMin = 0;
	private static int greenMax = 255;
	private static int blueMin = 0;
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

	private String input = "10";

	// Set to track generated colors for uniqueness check
	private Set<Integer> generatedColors = new HashSet<>();

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
				numColors = (int) Math.min(Integer.MAX_VALUE, maxPossibleColors);
				statusLabel.setText("Warning: Limited to " + numColors + " colors based on RGB ranges.");
				LOGGER.warn("Requested more colors than possible with current RGB ranges. Limited to " + numColors);
			}

			// Also check the absolute maximum (16.7 million)
			if (numColors > (1 << 24) - 1) {
				numColors = (1 << 24) - 1;
				statusLabel.setText("Warning: Limited to maximum of " + numColors + " unique colors.");
				LOGGER.warn("Requested more colors than possible (24-bit RGB). Limited to " + numColors);
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

		// Clear the set of generated colors
		generatedColors.clear();

		// Show progress indicator
		statusLabel.setText("Generating BMP with " + numColors + " unique colors...");
		progressIndicator.setVisible(true);
		generateButton.setDisable(true);

		// Run BMP generation on a background thread
		new Thread(() -> {
			try {
				String outputPath = HOIIVUtils.HOIIVUTILS_DIR + File.separator + "output.bmp";
				generateBMP(numColors, outputPath);

				// Update UI on completion
				javafx.application.Platform.runLater(() -> {
					progressIndicator.setVisible(false);
					generateButton.setDisable(false);
					statusLabel.setText("BMP generated successfully: " + outputPath);
					LOGGER.info("Generated BMP with " + numColors + " unique colors: " + outputPath);
				});
			} catch (Exception e) {
				e.printStackTrace();
				javafx.application.Platform.runLater(() -> {
					progressIndicator.setVisible(false);
					generateButton.setDisable(false);
					statusLabel.setText("Error: Failed to generate BMP - " + e.getMessage());
					LOGGER.error("Failed to generate BMP", e);
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
		int colorCount = 0;
		int attemptCount = 0;
		final int MAX_ATTEMPTS = 1000000; // Avoid infinite loops

		while (colorCount < numColors && attemptCount < MAX_ATTEMPTS) {
			Color color = generateUniqueColor(colorCount, numColors);
			int rgb = color.getRGB() & 0xFFFFFF; // Mask to just RGB values (no alpha)

			// Check if this color is already used
			if (!generatedColors.contains(rgb)) {
				generatedColors.add(rgb);

				// Calculate position in the image
				int x = colorCount % dimension;
				int y = colorCount / dimension;

				// Set the color in the image
				image.setRGB(x, y, color.getRGB());
				colorCount++;

				// Update progress indicator periodically
				if (colorCount % 100 == 0 || colorCount == numColors) {
					final int currentCount = colorCount;
					javafx.application.Platform.runLater(() -> {
						progressIndicator.setProgress((double) currentCount / numColors);
					});
				}
			}

			attemptCount++;

			// If we're having trouble finding unique colors, log a warning
			if (attemptCount >= MAX_ATTEMPTS) {
				LOGGER.warn("Reached maximum attempt limit while generating unique colors");

				int finalColorCount = colorCount;
				javafx.application.Platform.runLater(() -> {
					statusLabel.setText("Warning: Difficulty finding unique colors. Generated " + finalColorCount + " colors.");
				});
			}
		}

		// Fill any remaining pixels with black
		if (colorCount < numColors) {
			for (int y = 0; y < dimension; y++) {
				for (int x = 0; x < dimension; x++) {
					int index = y * dimension + x;
					if (index >= colorCount && index < dimension * dimension) {
						image.setRGB(x, y, Color.BLACK.getRGB());
					}
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
		// Calculate the number of possible colors within the constraints
		int redRange = redMax - redMin + 1;
		int greenRange = greenMax - greenMin + 1;
		int blueRange = blueMax - blueMin + 1;

		// Use prime number multiplication and large constants to create a more random distribution
		// The goal is to visit as many unique RGB combinations as possible
		long multiplier = 2862933555777941757L; // Large prime
		long addend = 3037000493L; // Another prime

		// Generate a pseudo-random number based on the index
		long randomValue = (index * multiplier + addend) & 0x7FFFFFFFFFFFFFFFL;

		// Extract RGB components from the random value, respecting the min/max constraints
		int r = redMin + (int)(randomValue % redRange);
		randomValue /= redRange;

		int g = greenMin + (int)(randomValue % greenRange);
		randomValue /= greenRange;

		int b = blueMin + (int)(randomValue % blueRange);

		// Use deterministic but well-distributed hash if we're getting colors that are too similar
		if (index > totalColors / 2) {
			// Alternative algorithm to maximize distance between colors
			r = redMin + (int)(((index * 7621) + 1) % redRange);
			g = greenMin + (int)(((index * 4567) + 5) % greenRange);
			b = blueMin + (int)(((index * 3571) + 7) % blueRange);
		}

		return new Color(r, g, b);
	}

	/**
	 * Updates the color preview in the UI with the specified number of unique colors.
	 *
	 * @param numColors Number of unique colors to display.
	 */
	private void updateColorPreview(int numColors) {
		if (numColors <= 0) return;

		colorPreviewGrid.getChildren().clear();
		generatedColors.clear();

		int columns = (int) Math.ceil(Math.sqrt(numColors));
		int boxSize = 8;
		int previewLimit = Math.min(numColors, 1000);

		int maxRetries = 100; // Prevent infinite loops with a max retry count

		for (int i = 0; i < previewLimit; i++) {
			// Generate unique color
			Color color = generateUniqueColor(i, numColors);
			int rgb = color.getRGB() & 0xFFFFFF;

			int retryCount = 0;
			// Check if this color is already used
			while (generatedColors.contains(rgb) && retryCount < maxRetries) {
				// Try a slightly different approach on retries
				color = generateUniqueColor(i + retryCount * previewLimit, numColors * 2);
				rgb = color.getRGB() & 0xFFFFFF;
				retryCount++;
			}

			if (retryCount >= maxRetries) {
				LOGGER.warn("Too many retries to generate unique color at index " + i);
				// Use a fallback color or break
				color = new Color(i % 256, (i / 256) % 256, (i / 65536) % 256);
				rgb = color.getRGB() & 0xFFFFFF;
			}

			generatedColors.add(rgb);

			// Create a rectangle with the color
			Rectangle rect = new Rectangle(boxSize, boxSize);
			rect.setFill(javafx.scene.paint.Color.rgb(color.getRed(), color.getGreen(), color.getBlue()));
			rect.setStroke(javafx.scene.paint.Color.BLACK);

			// Add the rectangle to the GridPane
			int row = i / columns;
			int col = i % columns;
			colorPreviewGrid.add(rect, col, row);

			// Safety check for RGB space exhaustion
			if (generatedColors.size() >= (redMax - redMin + 1) * (greenMax - greenMin + 1) * (blueMax - blueMin + 1)) {
				LOGGER.warn("Cannot generate more unique colors within the current RGB range constraints");
				break;
			}
		}
	}

	// Getters and setters for color ranges
	public static void setRedMin(int value) {
		redMin = value;
	}

	public static void setRedMax(int value) {
		redMax = value;
	}

	public static void setGreenMin(int value) {
		greenMin = value;
	}

	public static void setGreenMax(int value) {
		greenMax = value;
	}

	public static void setBlueMin(int value) {
		blueMin = value;
	}

	public static void setBlueMax(int value) {
		blueMax = value;
	}
}



// Old code for generating unique colors from before javafx

//package com.HOIIVUtils.ui.colorgen;
//
//import com.HOIIVUtils.hoi4utils.map.province.ColorGenerator;
//
//import javax.swing.*;
//import javax.swing.event.ChangeEvent;
//import javax.swing.event.ChangeListener;
//import java.awt.event.ActionEvent;
//import java.awt.event.ActionListener;
//import java.util.concurrent.Executors;
//import java.util.concurrent.ScheduledExecutorService;
//import java.util.concurrent.TimeUnit;
//
//public class ColorGeneratorMenu extends JFrame {
//	private JPanel colorGeneratorJPanel;
//	private JSlider redMinSlider;
//	private JSlider redMaxSlider;
//	private JSlider greenMinSlider;
//	private JSlider greenMaxSlider;
//	private JSlider blueMinSlider;
//	private JSlider blueMaxSlider;
//	private JButton generateButton;
//	private JTextField numColorsTextField;
//	private JLabel minGreenAmtLabel;
//	private JLabel maxGreenAmtLabel;
//	private JLabel minRedAmtLabel;
//	private JLabel maxRedAmtLabel;
//	private JLabel minBlueAmtLabel;
//	private JLabel maxBlueAmtLabel;
//	private JProgressBar generateProgressBar;
//
//	public ColorGeneratorMenu() {
//		super("Color Generator");
//
//		/* vars */
//		JSlider[] sliders = {redMinSlider, redMaxSlider, greenMinSlider, greenMaxSlider, blueMinSlider, blueMaxSlider};
//		JLabel[] sliderAmtLabels = {minRedAmtLabel, maxRedAmtLabel, minGreenAmtLabel, maxGreenAmtLabel, minBlueAmtLabel, maxBlueAmtLabel};
//
//		/* color generator */
//		ColorGenerator colorGenerator = new ColorGenerator();
//
//		/* component visibility */
//		generateProgressBar.setVisible(false);
//
//		setContentPane(colorGeneratorJPanel);
//		setSize(700, 500);
//		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
//		pack();
//
//		/* action listeners */
//		generateButton.addActionListener(new ActionListener() {
//			/**
//			 * Invoked when an action occurs.
//			 *
//			 * @param e the event to be processed
//			 */
//			@Override
//			public void actionPerformed(ActionEvent e) {
//				Thread generateThread = new Thread() {
//					public void run() {
//						int numColors;
//
//						/* get num colors to generate */
//						try {
//							SwingUtilities.invokeAndWait(() -> {
//								generateButton.setEnabled(false);
//								generateButton.setVisible(false);
//							});
//						}
//						catch (Exception e) {
//							e.printStackTrace();
//						}
//
//						try {
//							numColors = getNumColorsGenerate();
//						} catch (Exception exc) {
//							System.err.println(exc.getMessage());
//							System.err.println("\t" + "in " + this);
//							try {
//								SwingUtilities.invokeAndWait(() -> {
//									generateButton.setEnabled(true);
//									generateButton.setVisible(true);
//								});
//							}
//							catch (Exception e) {
//								e.printStackTrace();
//							}
//							return;
//						}
//
//						/* display progress bar */
//						try {
//							SwingUtilities.invokeAndWait(() -> {
//								generateProgressBar.setMaximum(numColors);
//								generateProgressBar.setVisible(true);
//							});
//						}
//						catch (Exception e) {
//							e.printStackTrace();
//						}
//
//						/* generate colors */
//						colorGenerator.generateColors(numColors);
//
//						/* reset visibility */
//						final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
//						// delay at least half a second
//						executorService.schedule(() -> {
//							try {
//								SwingUtilities.invokeAndWait(() -> {
//									generateProgressBar.setVisible(false);
//									generateButton.setEnabled(true);
//									generateButton.setVisible(true);
//								});
//							}
//							catch (Exception e) {
//								e.printStackTrace();
//							}
//						}, 500, TimeUnit.MILLISECONDS);
//
//						System.out.println("Finished on " + Thread.currentThread());
//					}
//
//				};
//				generateThread.start();
//			}
//		});
//
//		for (int i = 0; i < sliders.length; i++) {
//			int finalI = i;
//			sliders[i].addChangeListener(new ChangeListener() {
//				@Override
//				public void stateChanged(ChangeEvent e) {
//					int value = sliders[finalI].getValue();
//					updateValuesFromSlider(value, sliderAmtLabels[finalI], finalI);
//				}
//			});
//		}
//	}
//
//	private void updateValuesFromSlider(int value, JLabel label, int index) {
//		label.setText(Integer.toString(value));
//
//		switch (index) {
//			case 0:
//				ColorGenerator.setRedMin(value);
//				break;
//			case 1:
//				ColorGenerator.setRedMax(value);
//				break;
//			case 2:
//				ColorGenerator.setGreenMin(value);
//				break;
//			case 3:
//				ColorGenerator.setGreenMax(value);
//				break;
//			case 4:
//				ColorGenerator.setBlueMin(value);
//				break;
//			case 5:
//				ColorGenerator.setBlueMax(value);
//				break;
//			default:
//				break;
//		}
//	}
//
//	/**
//	 * @throws IllegalArgumentException from <code>Integer.parseInt()</code> if <code>numColorsTextField</code> cannot be read.
//	 * @return number of colors selected to generate
//	 */
//	private int getNumColorsGenerate() {
//		int numColors;
//		numColors = Integer.parseInt(numColorsTextField.getText());
//
//		if (numColors > (1 << 24) - 1) {
//			numColors = (1 << 24) - 1;
//			String err = "Error: Attempting to generate more unique colors than is possible. Will generate max possible "
//					+ "[" + numColors + "]" + " instead.";
//			JOptionPane.showMessageDialog(this, err, this.getTitle(), JOptionPane.WARNING_MESSAGE);
//			System.err.println(err);
//		}
//		return numColors;
//	}
//
//}
