package com.hoi4utils.ui.focus_view;


import com.hoi4utils.clausewitz.HOIIVUtils;
import com.hoi4utils.clausewitz.data.country.CountryTag;
import com.hoi4utils.clausewitz.data.focus.FocusTree$;
import com.hoi4utils.clausewitz.localization.*;
import com.hoi4utils.clausewitz.script.PDXScript;
import com.hoi4utils.ddsreader.DDSReader;
import com.hoi4utils.clausewitz.HOIIVFile;
import com.hoi4utils.clausewitz.data.focus.FixFocus;
import com.hoi4utils.clausewitz.data.focus.Focus;
import com.hoi4utils.clausewitz.data.focus.FocusTree;
import com.hoi4utils.ui.pdxscript.NewFocusTreeWindow;
import com.hoi4utils.ui.pdxscript.PDXEditorWindow;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;

import java.awt.*;
import java.time.LocalDateTime;
import java.util.*;

import javafx.geometry.Rectangle2D;
import javafx.scene.control.*;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import com.hoi4utils.ui.javafx.image.JavaFXImageUtils;
import com.hoi4utils.ui.HOIIVUtilsWindow;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import scala.jdk.javaapi.CollectionConverters;

import javax.swing.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.List;
import java.util.function.Consumer;

public class FocusTreeWindow extends HOIIVUtilsWindow {
	private static final Logger LOGGER = LogManager.getLogger(FocusTreeWindow.class);
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
	private Point2D marqueeStartPoint;
	private Point2D marqueeEndPoint;
	private final List<Focus> selectedFocuses = new ArrayList<>();
	private boolean gridLines = false;

	public FocusTreeWindow() {
		setFxmlResource("FocusTreeWindow.fxml");
		setTitle("Focus Tree View");
	}

	/**
	 * Called when the application is initialized.
	 * <p> 
	 * Sets up the focus tree dropdown and its listeners, sets the focus tree
	 * canvas's width and height, draws the focus tree, and sets up the
	 * focus tree canvas's scroll pane.
	 */
	@FXML
	void initialize() {
		
		// Set up the focus tree dropdown and its listeners
		ObservableList<FocusTree> trees = FocusTree$.MODULE$.observeFocusTrees();
		if (trees != null && !trees.isEmpty()) {
			focusTreeDropdown.setItems(trees);
		} else {
			LOGGER.warn("No focus trees found.");
		}

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
		
		// Set up the focus tree canvas's scroll pane
		focusTreeCanvasScrollPane.setOnMousePressed(e -> {
			if (e.getButton() == MouseButton.MIDDLE)
				focusTreeCanvasScrollPane.setPannable(true);
		});
		focusTreeCanvasScrollPane.setOnMouseReleased(e -> {
			if (e.getButton() == MouseButton.MIDDLE)
				focusTreeCanvasScrollPane.setPannable(false);
		});
		exportFocusTreeButton.setOnAction(event -> handleExportFocusTreeButtonClick());

		// Load the focus unavailable image
		gfxFocusUnavailable = loadFocusUnavailableImage();

		// Set up the focus tree
		focusTree = FocusTree$.MODULE$.get(new CountryTag("SMA"));  // TODO
		if (focusTree == null) {
			focusTree = FocusTree.listFocusTrees().head();
		}
		// If focusTree is still null, assign a new value
		if (focusTree == null) {
			focusTree = FocusTree$.MODULE$.get(new File(HOIIVFile.mod_focus_folder + "//massachusetts.txt")).getOrElse(null);
		}

        try {
            FixFocus.fixLocalization(focusTree);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        // Draw the focus tree
		drawFocusTree();

		JOptionPane.showMessageDialog(null,
				"dev @end of initialize() [FocusTreeWindow] - loaded focuses: " + focusTree.focuses().size()
						+ "\n" + "loaded tree of country: " + focusTree.country().get()
						+ "\n" + "draw focus tree: " + HOIIVUtils.getBoolean("draw_focus_tree.enabled"));
	}

	public Canvas focusTreeCanvas() {
		return focusTreeCanvas;
	}

	private void exportFocusTree(FocusTree focusTree, String path) {
		try (PrintWriter writer = new PrintWriter(path)) {
			// Write the focus tree to the file
			writer.println("### Generated by HOI4Utils, time: " + LocalDateTime.now() + "###");
			writer.println(focusTree.toScript());
			LOGGER.info("Exported focus tree {} to {}", focusTree, path);
		} catch (FileNotFoundException e) {
			LOGGER.error("Error exporting focus tree: {}", e.getMessage());
			JOptionPane.showMessageDialog(null, "Error exporting focus tree: " + e.getMessage(), "Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	private int getMinX() {
		return focusTree.minX();
	}

	private int getMinY() { return 0; }

	private int focusTreeViewLength() {
		return getMaxX() - getMinX();
	}

	private int focusTreeViewHeight() {
		return getMaxY() - getMinY();
	}

	private Dimension focusTreeViewDimension() {
		return new Dimension(focusTreeViewLength(), focusTreeViewHeight());
	}

	private Image gfxFocusUnavailable;

	int getMaxX() {
		return CollectionConverters.asJavaCollection(focusTree.focuses()).stream()
				.mapToInt(Focus::absoluteX)
				.max().orElse(10);
	}

	int getMaxY() {
		return CollectionConverters.asJavaCollection(focusTree.focuses()).stream()
				.mapToInt(Focus::absoluteY)
				.max().orElse(10);
	}

	private Image loadFocusUnavailableImage() {
		String focusUnavailablePath = "hoi4files/gfx/focus_unavailable_bg.dds";
		final InputStream fis = getClass().getClassLoader().getResourceAsStream(focusUnavailablePath);
		try (fis) {
			if (fis == null) {
				throw new FileNotFoundException("Unable to find '" + focusUnavailablePath + "'");
			}

			byte[] buffer = new byte[fis.available()];
			int bytesRead = fis.read(buffer);
			fis.close();

			int[] ddspixels = DDSReader.read(buffer, DDSReader.ARGB, 0);
			int ddswidth = DDSReader.getWidth(buffer);
			int ddsheight = DDSReader.getHeight(buffer);

			return JavaFXImageUtils.imageFromDDS(ddspixels, ddswidth, ddsheight);
		} catch (IOException e) {
			LOGGER.error("Error loading focus unavailable image: {}", e.getMessage());
			return null;
		}
	}

	private void drawFocus(GraphicsContext gc2D, Focus focus, int minX) {
		boolean isSelected = selectedFocuses.contains(focus);

		gc2D.setFill(Color.WHITE);
		int x1 = FOCUS_X_SCALE * (focus.absoluteX() - minX) + X_OFFSET_FIX;
		int y1 = focusToCanvasY(focus);
		int yAdj1 = (int) (FOCUS_Y_SCALE / 2.2);
		int yAdj2 = (FOCUS_Y_SCALE / 2) + 20;

		gc2D.drawImage(gfxFocusUnavailable, x1 - 32, y1 + yAdj1);
		gc2D.drawImage(focus.getDDSImage(), x1, y1);
		var locName = focus.localizationText(Property.NAME);
		String name = locName.equals("[null]") && !focus.id().str().isBlank() ? focus.id().str() : locName;
		gc2D.fillText(name, x1 - 20, y1 + yAdj2);

		if (isSelected) {
			gc2D.setStroke(Color.YELLOW);
			gc2D.setLineWidth(2);
			gc2D.strokeRect(x1 - FOCUS_X_SCALE / 2.3, y1 + yAdj1, FOCUS_X_SCALE * 2, FOCUS_Y_SCALE / 2.3);
		}
	}

	public void drawFocusTree() {
		if (!HOIIVUtils.getBoolean("draw_focus_tree.enabled")) return;
		if (focusTree == null) return;

		var focuses = CollectionConverters.asJavaCollection(focusTree.focuses());
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
		if (gridLines) drawGridLines(gc2D);

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
				for (var prereqFocusSet: CollectionConverters.asJava(focus.prerequisiteSets())) {
					prereqFocusSet.foreach(prereqFocus -> {
						int x1 = FOCUS_X_SCALE * (focus.absoluteX() - minX) + X_OFFSET_FIX;
						int y1 = focusToCanvasY(focus);
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
                        return null;
                    });
				}
			}
		}
	}

	private void drawMutuallyExclusiveFocuses(GraphicsContext gc2D, Collection<Focus> focuses, int minX) {

		gc2D.setStroke(Color.DARKRED);
		gc2D.setLineWidth(3);

		for (Focus focus : focuses) {
			if (focus.isMutuallyExclusive()) {
				var mutuallyExclusiveFocuses = CollectionConverters.asJava(focus.mutuallyExclusiveList());
				for (Focus mutexFocus : mutuallyExclusiveFocuses) {
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

	public void drawGridLines(GraphicsContext gc) {
		int minX = getMinX();
		int maxX = getMaxX();
		int maxY = getMaxY();
		gc.setStroke(Color.GRAY);
		gc.setLineWidth(1);
		/* vertical lines */
		for (int x = minX; x <= maxX; x++) {
			int x1 = focusToCanvasX(x);
			int y1 = 0, y2 = focusToCanvasY(maxY);
			gc.strokeLine(x1, y1, x1, y2);
		}
		/* horizontal lines */
		for (int y = 1; y <= maxY; y++) {
			int x1 = focusToCanvasX(minX);
			int x2 = focusToCanvasX(maxX);
			int y1 = focusToCanvasY(y);
			gc.strokeLine(x1, y1, x2, y1);
		}
		/* write coordinates */
		var font = gc.getFont();
		for (int x = minX; x <= maxX; x++) {
			int x1 = focusToCanvasX(x) - 8;
			for (int y = 1; y <= maxY; y++) {
				int y1 = focusToCanvasY(y) - 5;
				gc.setFont(new javafx.scene.text.Font("Arial", 8));
				gc.fillText(x + ", " + y, x1, y1);
			}
		}
		// reset font
		gc.setFont(font);
	}

	private Focus getFocusHover(Point2D p) {
		int x = (int) (p.getX() / FOCUS_X_SCALE) + focusTree.minX();
		int y = (int) (p.getY() / FOCUS_Y_SCALE);

		for (Focus f : CollectionConverters.asJava(focusTree.focuses())) {
			if (f.hasAbsolutePosition(x, y)) {
				return f;
			}
		}

		return null;
	}

	private void selectClosestMatch(ComboBox<FocusTree> comboBox, String typedText) {
		for (FocusTree item : comboBox.getItems()) {
			scala.Option<CountryTag> optionalCountry = item.country().get();
			if (optionalCountry.nonEmpty() && optionalCountry.getOrElse(() -> "").toLowerCase().startsWith(typedText.toLowerCase())) {
				comboBox.getSelectionModel().select(item);
				comboBox.getEditor().setText(String.valueOf(item));
				return;
			}
		}
	}

	private void addFocusTree(FocusTree focusTree) {
		focusTreeDropdown.getItems().add(focusTree);
		focusTreeDropdown.getItems().sort(Comparator.comparing(FocusTree::toString));
	}

	private void viewFocusTree(FocusTree focusTree) {
		// manually change the selected focus tree
		LOGGER.info("Viewing focus tree: {}", focusTree);
		focusTreeDropdown.getSelectionModel().select(focusTree);
	}

	private int focusToCanvasX(Focus f) {
		return focusToCanvasX(f.absoluteX());
	}

	private int focusToCanvasX(int focusAbsX) {
		return FOCUS_X_SCALE * (focusAbsX - getMinX()) + X_OFFSET_FIX;
	}

	private int focusToCanvasY(Focus f) {
		return focusToCanvasY(f.absoluteY());
	}

	private int focusToCanvasY(int focusAbsY) {
		return FOCUS_Y_SCALE * (focusAbsY - getMinY()) + Y_OFFSET_FIX;
	}


	private int canvasToFocusX(double canvasX) {
		return (int) ((canvasX - X_OFFSET_FIX) / FOCUS_X_SCALE + getMinX());
	}

	private int canvasToFocusY(double canvasY) {
		return (int) ((canvasY - Y_OFFSET_FIX) / FOCUS_Y_SCALE);
	}

	private boolean isWithinMarquee(Focus f) {
		double focusX = focusToCanvasX(f);
		double focusY = focusToCanvasY(f);
		// Check if the focus is within the marquee selection
		return focusMarqueeRectangle().contains(focusX, focusY);
	}

	/**
	 * Returns the rectangle representing the marquee selection. This is an enlarged version of the
	 * theoretical marquee selection rectangle to account for the focus size.
	 */
	private Rectangle2D focusMarqueeRectangle() {
		return new Rectangle2D(
				Math.min(marqueeStartPoint.getX(), marqueeEndPoint.getX()) - (CENTER_FOCUS_X / 2.0),
				Math.min(marqueeStartPoint.getY(), marqueeEndPoint.getY()) - (CENTER_FOCUS_Y / 2.0),
				Math.abs(marqueeEndPoint.getX() - marqueeStartPoint.getX()) + CENTER_FOCUS_X,
				Math.abs(marqueeEndPoint.getY() - marqueeStartPoint.getY()) + CENTER_FOCUS_X
		);
	}

	@FXML
	private void toggleGridLines() {
		gridLines = !gridLines;
		drawFocusTree();
	}

	private int limitFocusMoveX(int newFocusX) {
		int minX = getMinX();
		int maxX = getMaxX();
		if (newFocusX < minX) {
			return minX;
		} else if (newFocusX > maxX) {
			return maxX;
		}
		return newFocusX;
	}

	private int limitFocusMoveY(int newFocusY) {
		int minY = getMinY();
		int maxY = getMaxY();
		if (newFocusY < minY) {
			return minY;
		} else if (newFocusY > maxY) {
			return maxY;
		}
		return newFocusY;
	}

	/* event handlers */

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

	@FXML
	public void handleFocusTreeViewMousePressed(MouseEvent e) {
		if (e.isPrimaryButtonDown()) {
			// if primary click -> clear focus selection
			selectedFocuses.clear();

			// Calculate internal grid position from mouse position
			int internalX = (int) ((e.getX() - X_OFFSET_FIX) / FOCUS_X_SCALE) + focusTree.minX();
			int internalY = (int) ((e.getY() - Y_OFFSET_FIX) / FOCUS_Y_SCALE);

			//draggedFocus = getFocusHover(new Point2D(internalX, internalY));
			// Identify the focus being dragged based on the mouse press position
			draggedFocus = CollectionConverters.asJavaCollection(focusTree.focuses()).stream()
					.filter(f -> f.absoluteX() == internalX && f.absoluteY() == internalY)
					.findFirst()
					.orElse(null);
			LOGGER.info("Focus {} selected", draggedFocus);
		} else if (e.isSecondaryButtonDown()) {
			// if secondary click -> add focus menu
			ContextMenu contextMenu = new ContextMenu();
			MenuItem addFocusItem = new MenuItem("Add Focus");
			MenuItem newFocusTreeItem = new MenuItem("New Focus Tree");
			addFocusItem.setOnAction(event -> {
				LOGGER.info("Adding focus via context menu");
				// open add focus menu to side of focus tree view
				Focus newFocus = new Focus(focusTree);
				focusTree.addNewFocus(newFocus);
				newFocus.setAbsoluteXY(canvasToFocusX(e.getX()), canvasToFocusY(e.getY()), false);
				newFocus.setID(focusTree.nextTempFocusID());
				openEditorWindow(newFocus, this::drawFocusTree);
			});
			newFocusTreeItem.setOnAction(event -> {
				LOGGER.info("Creating new focus tree via context menu");
				// open new focus tree menu to side of focus tree view
				openNewFocusTreeWindow();
			});
			contextMenu.getItems().add(addFocusItem);
			contextMenu.getItems().add(newFocusTreeItem);
			contextMenu.show(focusTreeCanvas, e.getScreenX(), e.getScreenY());
		}
	}

	@FXML
	public void handleFocusTreeViewMouseDragged(MouseEvent e) {
		if (e.isPrimaryButtonDown() && draggedFocus != null) {
			// Calculate internal grid position from mouse position
//			int internalX = (int) ((e.getX() - X_OFFSET_FIX) / FOCUS_X_SCALE) + focusTree.minX();
//			int internalY = (int) ((e.getY() - Y_OFFSET_FIX) / FOCUS_Y_SCALE);
			int newX = limitFocusMoveX(this.canvasToFocusX(e.getX()));
			int newY = limitFocusMoveY(this.canvasToFocusY(e.getY()));
			if (draggedFocus.samePosition(newX, newY)) return;
            Dimension prevDim = focusTreeViewDimension();
            if (e.isShiftDown()) {
                // Update the focus position with SHIFT key pressed
				draggedFocus.setAbsoluteXY(newX, newY, true);
				LOGGER.info("Focus {} moved to {}, {}", draggedFocus, newX, newY);

				// TODO: Redraw the focus tree to reflect the change
                // TODO: fix for bad behavior when the pane is resized
            } else {
                // Update the focus position with SHIFT key not pressed
				draggedFocus.setAbsoluteXY(newX, newY, false);
				LOGGER.info("Focus {} moved to {}, {}", draggedFocus, newX, newY);

				// TODO: Redraw the focus tree to reflect the change
                // TODO: fix for bad behavior when the pane is resized
            }
            drawFocusTree();
            adjustFocusTreeViewport(prevDim);
        } else if (e.isSecondaryButtonDown()) {
			if (marqueeStartPoint == null)
				marqueeStartPoint = new Point2D(e.getX(), e.getY());
			else
				marqueeEndPoint = new Point2D(e.getX(), e.getY());
		}
	}

	/**
	 * Fix for unwanted behavior when the focus tree pane is resized
	 * @param prevDim the previous dimensions of the focus tree view
	 */
	private void adjustFocusTreeViewport(Dimension prevDim) {
		var dim = focusTreeViewDimension();
		if (!dim.equals(prevDim)) {
			// adjust the viewport to maintain its position
			double x = focusTreeCanvasScrollPane.getHvalue();
			double y = focusTreeCanvasScrollPane.getVvalue();
			double xRatio = x * prevDim.width / dim.width;
			double yRatio = y * prevDim.height / dim.height;
			focusTreeCanvasScrollPane.setHvalue(xRatio);
			focusTreeCanvasScrollPane.setVvalue(yRatio);
		}
	}

	@FXML
	public void handleFocusTreeViewMouseReleased(MouseEvent e) {
		if (draggedFocus != null) {
			draggedFocus = null; // Reset the reference when the mouse is released
		}

		if (marqueeStartPoint != null && marqueeEndPoint != null) {
			// Identify the focuses within the marquee selection
			selectedFocuses.clear(); // Clear previous selections
			selectedFocuses.addAll(CollectionConverters.asJavaCollection(focusTree.focuses()).stream()
					.filter(this::isWithinMarquee)
					.toList());

			// Perform the desired action on the selected focuses
			for (Focus focus : selectedFocuses) {
				// TODO: Replace this with your desired action
				LOGGER.info("Marquee selected focus: {}", focus);
			}

			marqueeStartPoint = null;
			marqueeEndPoint = null;
		}

		drawFocusTree();
	}


	@FXML
	private void handleFocusTreeViewMouseClicked(MouseEvent event) {
		if (event.getButton() == MouseButton.PRIMARY) {
			if (event.getClickCount() == 2) {
				Point2D clickedPoint = new Point2D(event.getX(), event.getY());
				Focus clickedFocus = getFocusHover(clickedPoint);
				if (clickedFocus != null) {
					openEditorWindow(clickedFocus, this::drawFocusTree);
				}
			}
		} else if (event.getButton() == MouseButton.SECONDARY) {
			if (!selectedFocuses.isEmpty()) {
				// open context menu via right click
				// actions for the selected focuses
				ContextMenu contextMenu = new ContextMenu();
				MenuItem setRelativeFocusItem = new MenuItem("Set Relative Focus");
				setRelativeFocusItem.setOnAction(e -> {
					// TODO: Replace this with your desired action
					LOGGER.info("Set relative focus for selected focuses");
				});
				contextMenu.getItems().add(setRelativeFocusItem);
				contextMenu.show(focusTreeCanvas, event.getScreenX(), event.getScreenY());
			}
		}
	}

	@FXML
	private void handleExportFocusTreeButtonClick() {
		String path = "focusOutput.txt";
		exportFocusTree(focusTree, path);
		JOptionPane.showMessageDialog(null, "Focus tree exported to " + path, "Export Successful",
				JOptionPane.INFORMATION_MESSAGE);
	}

	@FXML
	private void openEditorWindow(Focus focus) {
		if (focus == null) throw new IllegalArgumentException("Focus cannot be null");
		PDXEditorWindow pdxEditorWindow = new PDXEditorWindow();
		pdxEditorWindow.open((PDXScript<?>) focus); // this is not necessarily redundant. DO NOT CHANGE
	}

	@FXML
	private void openEditorWindow(Focus focus, Runnable onUpdate) {
		if (focus == null) throw new IllegalArgumentException("Focus cannot be null");
		PDXEditorWindow pdxEditorWindow = new PDXEditorWindow();
		pdxEditorWindow.open((PDXScript<?>) focus, onUpdate); // DO NOT CHANGE
	}

	@FXML
	private void openNewFocusTreeWindow() {
		NewFocusTreeWindow newFocusTreeWindow = new NewFocusTreeWindow();
		newFocusTreeWindow.open((Consumer<FocusTree>) ((FocusTree f) -> {
			addFocusTree(f);
			viewFocusTree(f);
		}));
	}
}