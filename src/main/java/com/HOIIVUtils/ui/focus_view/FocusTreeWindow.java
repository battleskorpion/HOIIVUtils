package com.HOIIVUtils.ui.focus_view;

import com.HOIIVUtils.Settings;
import com.HOIIVUtils.clauzewitz.localization.Localizable;
import com.HOIIVUtils.clauzewitz.script.MultiPDXScript;
import com.HOIIVUtils.clauzewitz.script.PDXScript;
import com.HOIIVUtils.ddsreader.DDSReader;
import com.HOIIVUtils.clauzewitz.HOIIVFile;
import com.HOIIVUtils.clauzewitz.data.country.CountryTag;
import com.HOIIVUtils.clauzewitz.data.focus.FixFocus;
import com.HOIIVUtils.clauzewitz.data.focus.Focus;
import com.HOIIVUtils.clauzewitz.data.focus.FocusTree;
import com.HOIIVUtils.ui.pdxscript.PDXEditorWindow;
import javafx.fxml.FXML;

import java.util.*;

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
import com.HOIIVUtils.ui.HOIIVUtilsWindow;
import javax.swing.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

public class FocusTreeWindow extends HOIIVUtilsWindow {
	public static final int FOCUS_X_SCALE = 90; // ~2x per 1 y
	public static final int CENTER_FOCUS_X = (FOCUS_X_SCALE / 2);
	public static final int FOCUS_Y_SCALE = 140;
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
		return focusTree.focuses().stream().mapToInt(Focus::absoluteX).max().orElse(10);
	}

	int getMaxY() {
		return focusTree.focuses().stream().mapToInt(Focus::absoluteY).max().orElse(10);
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
		focusTreeDropdown.getItems().sort(Comparator.comparing(FocusTree::toString));
		focusTreeDropdown.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue != null) {
				focusTree = newValue;
				drawFocusTree();
			}
		});

		exportFocusTreeButton.setOnAction(event -> handleExportFocusTreeButtonClick());

		// Load the focus unavailable image
		gfxFocusUnavailable = loadFocusUnavailableImage();

		// Set up the focus tree
		focusTree = FocusTree.get(new CountryTag("SMA"));
		if (focusTree == null) {
			focusTree = FocusTree.listFocusTrees()[0];
		}

		// If focusTree is still null, assign a new value
		if (focusTree == null) {
			focusTree = FocusTree.get(new File(HOIIVFile.mod_focus_folder + "//massachusetts.txt"));
		}

		try {
			FixFocus.fixLocalization(focusTree);
		} catch (IOException e) {
			e.printStackTrace();
		}

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
			drawFocusTree();
		}

		if (Settings.DEV_MODE.enabled()) {
			JOptionPane.showMessageDialog(null,
					"dev @end of initialize() - loaded focuses: " + focusTree.focuses().size()
							+ "\n" + "loaded tree of country: " + focusTree.country.get()
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
					.getResourceAsStream(
							"com/HOIIVUtils/clauzewitz/hoi4files/gfx/focus_unavailable_bg.dds");
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
		int x1 = FOCUS_X_SCALE * (focus.absoluteX() - minX) + X_OFFSET_FIX;
		int y1 = FOCUS_Y_SCALE * focus.absoluteY() + Y_OFFSET_FIX;
		int yAdj1 = (int) (FOCUS_Y_SCALE / 2.2);
		int yAdj2 = (FOCUS_Y_SCALE / 2) + 20;

		gc2D.drawImage(gfxFocusUnavailable, x1 - 32, y1 + yAdj1);
		gc2D.drawImage(focus.getDDSImage(), x1, y1);
		String name = focus.localizationText(Localizable.Property.NAME);
		gc2D.fillText(name, x1 - 20, y1 + yAdj2);
	}

	public void drawFocusTree() {
		if (Settings.DRAW_FOCUS_TREE.disabled()) return;
		if (focusTree == null) return;

		var focuses = focusTree.focuses();
		GraphicsContext gc2D = focusTreeCanvas.getGraphicsContext2D();

		// Calculate the maximum/minimum X and Y values
		int minX = getMinX();
		int maxX = getMaxX();
		int maxY = getMaxY();

		// Set the canvas's width and height based on the maximum x and y values
		double width = Math.max(FOCUS_X_SCALE * (maxX - minX) + 2 * X_OFFSET_FIX, 800);
		double height = Math.max(FOCUS_Y_SCALE * (maxY + 2), 600);
		focusTreeCanvas.setWidth(width);
		focusTreeCanvas.setHeight(height);

		// Clear the canvas with a dark gray color
		gc2D.setFill(Color.DARKGRAY);
		gc2D.fillRect(0, 0, width, height);

		if (focuses.isEmpty()) return;

		// Load the focus unavailable image
		Image GFX_focus_unavailable = loadFocusUnavailableImage();

		// Draw the prerequisites
		drawPrerequisites(gc2D, focuses, minX);
		drawMutuallyExclusiveFocuses(gc2D, focuses, minX);

		// Draw the focuses
		for (Focus focus : focuses) {
			drawFocus(gc2D, focus, minX);
		}
	}

	private void drawPrerequisites(GraphicsContext gc2D, Collection<Focus> focuses, int minX) {
		gc2D.setStroke(Color.BLACK);
		gc2D.setLineWidth(3);

		for (Focus focus : focuses) {
			if (focus.hasPrerequisites()) {
				for (var prereqFocusSet: focus.prerequisites) {
					for (Focus prereqFocus : prereqFocusSet) {
						int x1 = FOCUS_X_SCALE * (focus.absoluteX() - minX) + X_OFFSET_FIX;
						int y1 = FOCUS_Y_SCALE * focus.absoluteY() + Y_OFFSET_FIX;
						int linex1 = x1 + (FOCUS_X_SCALE / 2);
						int liney1 = y1 + (FOCUS_Y_SCALE / 2);
						int linex2 = linex1;
						int liney2 = y1 - 12;
						int liney4 = (FOCUS_Y_SCALE * prereqFocus.absoluteY()) + (FOCUS_Y_SCALE / 2)
								+ Y_OFFSET_FIX;
						int linex4 = (FOCUS_X_SCALE * (prereqFocus.absoluteX() - minX)) + (FOCUS_X_SCALE / 2)
								+ X_OFFSET_FIX;
						int linex3 = linex4;
						int liney3 = liney2;

						// gc2D.setStroke(Color.BLACK);
						gc2D.strokeLine(linex1, liney1, linex2, liney2);    // large vertical
						// gc2D.setStroke(Color.RED);
						gc2D.strokeLine(linex2, liney2, linex3, liney3);    // horizonal
						// gc2D.setStroke(Color.GREEN);
						gc2D.strokeLine(linex3, liney3, linex4, liney4);    // small vertical
					}
				}
			}
		}
	}

	private void drawMutuallyExclusiveFocuses(GraphicsContext gc2D, Collection<Focus> focuses, int minX) {

		gc2D.setStroke(Color.DARKRED);
		gc2D.setLineWidth(3);

		for (Focus focus : focuses) {
			if (focus.isMutuallyExclusive()) {
				for (Focus mutexFocus : focus.mutually_exclusive.stream().flatMap(MultiPDXScript::stream).toList()) {
					int x1 = FOCUS_X_SCALE * (focus.absoluteX() - minX) + (FOCUS_X_SCALE / 2) + X_OFFSET_FIX;
					int y1 = FOCUS_Y_SCALE * focus.absoluteY() + (int) (FOCUS_Y_SCALE / 1.6) + Y_OFFSET_FIX;
					int x2 = FOCUS_X_SCALE * (mutexFocus.absoluteX() - minX) + (FOCUS_X_SCALE / 2) + X_OFFSET_FIX;
					int y2 = FOCUS_Y_SCALE * mutexFocus.absoluteY() + (int) (FOCUS_Y_SCALE / 1.6) + Y_OFFSET_FIX;

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
	}

	@FXML
	public void handleFocusTreeViewMouseMoved(MouseEvent e) {
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
		String details = focusDetailsFocus.toScript();
		focusTooltipView = new Tooltip(details);
		focusTooltipView.show(focusTreeCanvas, e.getScreenX() + 10, e.getScreenY() + 10);
	}

	private Focus getFocusHover(Point2D p) {
		int x = (int) (p.getX() / FOCUS_X_SCALE) + focusTree.minX();
		int y = (int) (p.getY() / FOCUS_Y_SCALE);

		for (Focus f : focusTree.focuses()) {
			if (f.hasAbsolutePosition(x, y)) {
				return f;
			}
		}

		return null;
	}

	private void selectClosestMatch(ComboBox<FocusTree> comboBox, String typedText) {
		for (FocusTree item : comboBox.getItems()) {
			if (item.country.get() != null && item.country.get().get().toLowerCase().startsWith(typedText.toLowerCase())) {
				comboBox.getSelectionModel().select(item);
				comboBox.getEditor().setText(String.valueOf(item));
				return;
			}
		}
	}

	@FXML
	public void handleFocusTreeViewMousePressed(MouseEvent e) {
		// Calculate internal grid position from mouse position
		int internalX = (int) ((e.getX() - X_OFFSET_FIX) / FOCUS_X_SCALE) + focusTree.minX();
		int internalY = (int) ((e.getY() - Y_OFFSET_FIX) / FOCUS_Y_SCALE);

		//draggedFocus = getFocusHover(new Point2D(internalX, internalY));
		// Identify the focus being dragged based on the mouse press position
		draggedFocus = focusTree.focuses().stream()
				.filter(f -> f.absoluteX() == internalX && f.absoluteY() == internalY)
				.findFirst()
				.orElse(null);
		if (Settings.DEV_MODE.enabled() && draggedFocus != null)
			System.out.println("Focus " + draggedFocus + " selected");
	}

	@FXML
	public void handleFocusTreeViewMouseDragged(MouseEvent e) {
		if (e.isPrimaryButtonDown() && draggedFocus != null) {
			// Calculate internal grid position from mouse position
			int internalX = (int) ((e.getX() - X_OFFSET_FIX) / FOCUS_X_SCALE) + focusTree.minX();
			int internalY = (int) ((e.getY() - Y_OFFSET_FIX) / FOCUS_Y_SCALE);

			// Update the focus position
			draggedFocus.setAbsoluteXY(internalX, internalY);
			if (Settings.DEV_MODE.enabled())
				System.out.println("Focus " + draggedFocus + " moved to " + internalX + ", " + internalY);

			// Redraw the focus tree to reflect the change
			drawFocusTree();
		}
	}

	@FXML
	public void handleFocusTreeViewMouseReleased(MouseEvent e) {
		if (draggedFocus != null) {

		}

		draggedFocus = null; // Reset the reference when the mouse is released
	}

	@FXML
	private void handleFocusTreeViewMouseClicked(MouseEvent event) {
		if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
			Point2D clickedPoint = new Point2D(event.getX(), event.getY());
			Focus clickedFocus = getFocusHover(clickedPoint);
			if (clickedFocus != null) {
				openEditorWindow(clickedFocus);
			}
		}
	}

	@FXML
	private void openEditorWindow(Focus focus) {
		PDXEditorWindow pdxEditorWindow = new PDXEditorWindow();
		pdxEditorWindow.open((PDXScript<?>) focus);
	}
}