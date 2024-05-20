package com.HOIIVUtils.ui.focus_view;

import com.HOIIVUtils.hoi4utils.Settings;
import com.HOIIVUtils.hoi4utils.ddsreader.DDSReader;
import com.HOIIVUtils.hoi4utils.ioexceptions.IllegalLocalizationFileTypeException;
import com.HOIIVUtils.hoi4utils.HOIIVFile;
import com.HOIIVUtils.hoi4utils.clausewitz_data.country.CountryTag;
import com.HOIIVUtils.hoi4utils.clausewitz_data.focus.FixFocus;
import com.HOIIVUtils.hoi4utils.clausewitz_data.focus.Focus;
import com.HOIIVUtils.hoi4utils.clausewitz_data.focus.FocusTree;
import com.HOIIVUtils.hoi4utils.clausewitz_data.localization.FocusLocalizationFile;
import javafx.fxml.FXML;
import java.util.List;
import java.util.ArrayList;
import javafx.scene.control.Button;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import com.HOIIVUtils.ui.javafx.image.JavaFXImageUtils;
import com.HOIIVUtils.ui.HOIIVUtilsStageLoader;
import javax.swing.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Set;

public class FocusTreeWindow extends HOIIVUtilsStageLoader {
	static final int FOCUS_X_SCALE = 90; // ~2x per 1 y
	public static final int CENTER_FOCUS_X = (FOCUS_X_SCALE / 2);
	static final int FOCUS_Y_SCALE = 140;
	public static final int CENTER_FOCUS_Y = FOCUS_Y_SCALE / 2;
	public static final int VISIBLE_DROPDOWN_ROW_COUNT = 20;
	public static final int X_OFFSET_FIX = 30;
	public static final int Y_OFFSET_FIX = 40;

	@FXML
	Canvas focusTreeCanvas;
	@FXML
	ScrollPane focusTreeCanvasScrollPane;
	@FXML
	ComboBox<FocusTree> focusTreeDropdown;
	@FXML
	Button exportFocusTreeButton;

	private FocusTree focusTree;
	private Tooltip focusTooltipView;
	private Focus focusDetailsFocus;

	private double mouseX;
	private double mouseY;
	private Focus draggedFocus;

	public FocusTreeWindow() {
		setFxmlResource("FocusTreeWindow.fxml");
		setTitle("Focus Tree View");
	}

	private void exportFocusTree(FocusTree focusTree, String path) {
		try (PrintWriter writer = new PrintWriter(new File(path))) {
			// Write the focus tree to the file
			// writer.println(focusTree.toHoI4Format());
		} catch (FileNotFoundException e) {
			JOptionPane.showMessageDialog(null, "Error exporting focus tree: " + e.getMessage(), "Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	private int getMinX() {
		return focusTree.minX();
	}

	private Image gfxFocusUnavailable;

	int getMaxX() {
		return focusTree.focuses().stream().mapToInt(Focus::absoluteX).max().orElse(200);
	}

	int getMaxY() {
		return focusTree.focuses().stream().mapToInt(Focus::absoluteY).max().orElse(200);
	}

	@FXML
	private void handleExportFocusTreeButtonClick() {
		String path = "focusOutput.txt";
		exportFocusTree(focusTree, path);
		JOptionPane.showMessageDialog(null, "Focus tree exported to " + path, "Export Successful",
				JOptionPane.INFORMATION_MESSAGE);
	}

	/**
	 * Called when the application is initialized.
	 * 
	 * Sets up the focus tree dropdown and its listeners, sets the focus tree
	 * canvas's width and height, draws the focus tree, and sets up the
	 * focus tree canvas's scroll pane.
	 */
	@FXML
	void initialize() {
		// Set up the focus tree dropdown and its listeners
		focusTreeDropdown.setItems(FocusTree.observeFocusTrees());
		focusTreeDropdown.setTooltip(new Tooltip("Select a focus tree to view"));
		focusTreeDropdown.setVisibleRowCount(VISIBLE_DROPDOWN_ROW_COUNT);
		focusTreeDropdown.getSelectionModel().select(0);
		focusTreeDropdown.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue != null) {
				focusTree = newValue;
				drawFocusTree(focusTree);
			}
		});

		exportFocusTreeButton.setOnAction(event -> handleExportFocusTreeButtonClick());

		// Load the focus unavailable image
		gfxFocusUnavailable = loadFocusUnavailableImage();

		// Set up the focus tree
		focusTree = FocusTree.get(new CountryTag("SMA"));
		try {
			focusTree.setLocalization(new FocusLocalizationFile(
					HOIIVFile.localization_eng_folder + "\\focus_Massachusetts_SMA_l_english.yml"));
		} catch (IllegalLocalizationFileTypeException e) {
			throw new IllegalLocalizationFileTypeException("Error loading focus tree localization", e);
		}

		// If focusTree is still null, assign a new value
		if (focusTree == null) {
			focusTree = FocusTree.get(new File(HOIIVFile.focus_folder + "//massachusetts.txt"));
			try {
				focusTree.setLocalization(new FocusLocalizationFile(
						HOIIVFile.localization_eng_folder + "\\focus_Massachusetts_SMA_l_english.yml"));
			} catch (IllegalLocalizationFileTypeException e) {
				throw new IllegalLocalizationFileTypeException("Error loading focus tree localization", e);
			}
		}

		try {
			FixFocus.addFocusLoc(focusTree);
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Calculate the maximum X and Y values
		int maxX = getMaxX();
		int maxY = getMaxY();

		// Set the canvas's width and height based on the maximum x and y values
		focusTreeCanvas.setWidth(Math.max(FOCUS_X_SCALE * (maxX - getMinX()) + 2 * X_OFFSET_FIX, 800));
		focusTreeCanvas.setHeight(Math.max(FOCUS_Y_SCALE * (maxY + 2), 600));

		// Set up the focus tree canvas's scroll pane
		focusTreeCanvasScrollPane.setOnMousePressed(e -> {
			if (e.getButton() == MouseButton.MIDDLE)
				focusTreeCanvasScrollPane.setPannable(true);
		});
		focusTreeCanvasScrollPane.setOnMouseReleased(e -> {
			if (e.getButton() == MouseButton.MIDDLE)
				focusTreeCanvasScrollPane.setPannable(false);
		});

		// Draw the focus tree
		if (Settings.DRAW_FOCUS_TREE.enabled()) {
			drawFocusTree(focusTree);
		}

		if (Settings.DEV_MODE.enabled()) {
			JOptionPane.showMessageDialog(null,
					"dev @end of initialize() - loaded focuses: " + focusTree.focuses().size()
							+ "\n" + "loaded tree of country: " + focusTree.country()
							+ "\n" + "draw focus tree: " + Settings.DRAW_FOCUS_TREE.enabled());
		}
	}

	public Canvas focusTreeCanvas() {
		return focusTreeCanvas;
	}

	private Image loadFocusUnavailableImage() {
		InputStream fis = null;
		try {
			fis = getClass().getClassLoader()
					.getResourceAsStream("com/HOIIVUtils/hoi4utils/hoi4files/gfx/focus_unavailable_bg.dds");
			if (fis == null) {
				throw new FileNotFoundException(
						"Unable to find 'com/HOIIVUtils/hoi4utils/hoi4files/gfx/focus_unavailable_bg.dds'");
			}

			byte[] buffer = new byte[fis.available()];
			int bytesRead = fis.read(buffer);
			fis.close();

			int[] ddspixels = DDSReader.read(buffer, DDSReader.ARGB, 0);
			int ddswidth = DDSReader.getWidth(buffer);
			int ddsheight = DDSReader.getHeight(buffer);

			return JavaFXImageUtils.imageFromDDS(ddspixels, ddswidth, ddsheight);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	private void drawFocus(GraphicsContext gc2D, Focus focus, int minX) {

		gc2D.setFill(Color.WHITE);
		int x1 = FOCUS_X_SCALE * (focus.absoluteX() + minX) + X_OFFSET_FIX;
		int y1 = FOCUS_Y_SCALE * focus.absoluteY() + Y_OFFSET_FIX;
		int yAdj1 = (int) (FOCUS_Y_SCALE / 2.2);
		int yAdj2 = (FOCUS_Y_SCALE / 2) + 20;

		gc2D.drawImage(gfxFocusUnavailable, x1 - 32, y1 + yAdj1);
		gc2D.drawImage(focus.getDDSImage(), x1, y1);
		String name = focus.name();
		gc2D.fillText(name, x1 - 20, y1 + yAdj2);
	}

	public void drawFocusTree(FocusTree focusTree) {
		if (Settings.DRAW_FOCUS_TREE.disabled())
			return;

		// if (focusTree != null) { // todo
		// }

		GraphicsContext gc2D = focusTreeCanvas.getGraphicsContext2D();

		// Calculate the minimum X coordinate
		int minX = -getMinX();

		// Clear the canvas with a dark gray color
		double width = focusTreeCanvas.getWidth();
		double height = focusTreeCanvas.getHeight();
		gc2D.setFill(Color.DARKGRAY);
		gc2D.fillRect(0, 0, width, height);

		// Load the focus unavailable image
		Image GFX_focus_unavailable = loadFocusUnavailableImage();

		// Draw the prerequisites

		List<Focus> focusList = new ArrayList<>(focusTree.focuses());
		drawPrerequisites(gc2D, focusList, minX);
		drawMutuallyExclusiveFocuses(gc2D, focusList, minX);

		// Draw the focuses
		for (Focus focus : focusTree.focuses()) {
			drawFocus(gc2D, focus, minX);
		}
	}

	private void drawPrerequisites(GraphicsContext gc2D, List<Focus> focuses, int minX) {

		gc2D.setStroke(Color.BLACK);
		gc2D.setLineWidth(3);

		for (Focus focus : focuses) {
			if (focus.hasPrerequisites()) {
				for (Set<Focus> prereqFocusSet : focus.getPrerequisites()) {
					for (Focus prereqFocus : prereqFocusSet) {
						int x1 = FOCUS_X_SCALE * (focus.absoluteX() + minX);
						int y1 = FOCUS_Y_SCALE * focus.absoluteY();
						int linex1 = x1 + (FOCUS_X_SCALE / 2) + X_OFFSET_FIX;
						int liney1 = y1 + Y_OFFSET_FIX;
						int linex2 = linex1;
						int liney2 = liney1 - 12;
						int liney4 = (FOCUS_Y_SCALE * prereqFocus.absoluteY()) + Y_OFFSET_FIX;
						int linex4 = (FOCUS_X_SCALE * (prereqFocus.absoluteX() + minX)) + (FOCUS_X_SCALE / 2)
								+ X_OFFSET_FIX;
						int linex3 = linex4;
						int liney3 = liney2;
						// gc2D.setStroke(Color.BLACK);
						gc2D.strokeLine(linex1, liney1, linex2, liney2);
						// gc2D.setStroke(Color.RED);
						gc2D.strokeLine(linex2, liney2, linex3, liney3);
						// gc2D.setStroke(Color.GREEN);
						gc2D.strokeLine(linex3, liney3, linex4, liney4);
					}
				}
			}
		}
	}

	private void drawMutuallyExclusiveFocuses(GraphicsContext gc2D, List<Focus> focuses, int minX) {

		gc2D.setStroke(Color.DARKRED);
		gc2D.setLineWidth(3);

		for (Focus focus : focuses) {
			if (focus.isMutuallyExclusive()) {
				for (Focus mutexFocus : focus.getMutuallyExclusive()) {
					int x1 = FOCUS_X_SCALE * (focus.absoluteX() + minX) + (FOCUS_X_SCALE / 2) + X_OFFSET_FIX;
					int y1 = FOCUS_Y_SCALE * focus.absoluteY() + (FOCUS_Y_SCALE / 2) + Y_OFFSET_FIX;
					int x2 = FOCUS_X_SCALE * (mutexFocus.absoluteX() + minX) + (FOCUS_X_SCALE / 2) + X_OFFSET_FIX;
					int y2 = FOCUS_Y_SCALE * mutexFocus.absoluteY() + (FOCUS_Y_SCALE / 2) + Y_OFFSET_FIX;

					// Draw a line between the two mutually exclusive focuses
					gc2D.strokeLine(x1, y1, x2, y2);

					// Draw a circle at the start of the line
					gc2D.setFill(Color.DARKRED);
					gc2D.fillOval(x1 - 5, y1 - 5, 10, 10);

					// Draw a circle at the end of the line
					gc2D.setFill(Color.DARKRED);
					gc2D.fillOval(x2 - 5, y2 - 5, 10, 10);
				}
			}
		}

		/* focus image */
		for (Focus focus : focusTree.focuses()) {
			int x1 = FOCUS_X_SCALE * (focus.absoluteX() + minX) + X_OFFSET_FIX;
			int y1 = FOCUS_Y_SCALE * focus.absoluteY();
			int yAdj1 = (int) (FOCUS_Y_SCALE / 2.2);
			int yAdj2 = (FOCUS_Y_SCALE / 2) + 20;
			gc2D.drawImage(gfxFocusUnavailable, x1 - 32, y1 + yAdj1);
			gc2D.drawImage(focus.getDDSImage(), x1, y1);
			String name = focus.name();
			gc2D.fillText(name, x1 - 20, y1 + yAdj2);
		}
	}

	;

	@FXML
	public void mouseMovedFocusTreeViewAdapter(MouseEvent e) {
		/* get focus being hovered over */
		Point2D p = new Point2D(e.getX(), e.getY());
		Focus focusTemp = getFocusHover(p);
		if (focusTemp == null) {
			/*
			 * if no focus being hovered over -> if there was a focus detail view open,
			 * get rid of it and reset
			 */
			if (focusTooltipView != null) {
				focusTooltipView.hide();
				focusTooltipView = null;
				focusDetailsFocus = null;
			}
			return;
		}
		if (focusTemp == focusDetailsFocus) {
			return;
		}
		focusDetailsFocus = focusTemp;

		/* focus details view */
		if (focusTooltipView != null) {
			focusTooltipView.hide();
		}
		// focusTreeDetailsWindow = new FocusTreeDetailsWindow(focusDetailsFocus, p);
		String details = focusDetailsFocus.getFocusDetails();
		focusTooltipView = new Tooltip(details);
		focusTooltipView.show(focusTreeCanvas, e.getScreenX() + 10, e.getScreenY() + 10);
	}

	private Focus getFocusHover(Point2D p) {
		int x = (int) (p.getX() / FOCUS_X_SCALE) + focusTree.minX();
		int y = (int) (p.getY() / FOCUS_Y_SCALE);

		for (Focus f : focusTree.focuses()) {
			if (f.absolutePosition().equals(new Point2D(x, y))) {
				return f;
			}
		}

		return null;
	}

	private void selectClosestMatch(ComboBox<FocusTree> comboBox, String typedText) {
		for (FocusTree item : comboBox.getItems()) {
			if (item.country().tag().toLowerCase().startsWith(typedText.toLowerCase())) {
				comboBox.getSelectionModel().select(item);
				comboBox.getEditor().setText(String.valueOf(item));
				return;
			}
		}
	}

	@FXML
	public void mousePressedFocusTreeViewAdapter(MouseEvent e) {
		mouseX = e.getX();
		mouseY = e.getY();

		// Identify the focus being dragged based on the mouse press position
		draggedFocus = getFocusHover(new Point2D(mouseX, mouseY));
	}

	@FXML
	public void mouseDraggedFocusTreeViewAdapter(MouseEvent e) {
		if (e.isPrimaryButtonDown() && draggedFocus != null) {
			double deltaX = e.getX() - mouseX;
			double deltaY = e.getY() - mouseY;

			// Update focus positions based on mouse movement
			int newX = draggedFocus.x() + (int) ((deltaX / FOCUS_X_SCALE));
			int newY = draggedFocus.y() + (int) (deltaY / FOCUS_Y_SCALE);
			draggedFocus.setXY(newX, newY);

			// Redraw the focus tree with updated positions
			drawFocusTree(focusTree);

			mouseX = e.getX();
			mouseY = e.getY();
		}
	}

	@FXML
	public void mouseReleasedFocusTreeViewAdapter(MouseEvent e) {
		draggedFocus = null; // Reset the reference when the mouse is released
	}
}