package com.hoi4utils.ui.focus_view;

import com.hoi4utils.HOIIVFiles;
import com.hoi4utils.ddsreader.DDSReader;
import com.hoi4utils.hoi4.country.CountryTag;
import com.hoi4utils.hoi4.focus.FixFocus;
import com.hoi4utils.hoi4.focus.Focus;
import com.hoi4utils.hoi4.focus.FocusTree;
import com.hoi4utils.hoi4.focus.FocusTree$;
import com.hoi4utils.script.PDXScript;
import com.hoi4utils.ui.HOIIVUtilsAbstractController;
import com.hoi4utils.ui.javafx_ui.image.JavaFXImageUtils;
import com.hoi4utils.ui.pdxscript.NewFocusTreeController;
import com.hoi4utils.ui.pdxscript.PDXEditorPane;
import com.sun.javafx.geom.Dimension;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import scala.jdk.javaapi.CollectionConverters;

import javax.swing.*;
import java.io.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

public class FocusTreeController extends HOIIVUtilsAbstractController {
	private static final Logger logger = LogManager.getLogger(FocusTreeController.class);
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
	@FXML
	SplitPane focusTreeViewSplitPane; 

	private FocusTree focusTree;
	private Tooltip focusTooltipView;
	private Focus focusDetailsFocus;
	private Focus draggedFocus;
	private Point2D marqueeStartPoint;
	private Point2D marqueeEndPoint;
	private final List<Focus> selectedFocuses = new ArrayList<>();
	private boolean gridLines = false;

	public FocusTreeController() {
		setFxmlResource("FocusTree.fxml");
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
		logger.info("Initializing FocusTreeWindow...");

		// Load available focus trees
		ObservableList<FocusTree> trees = FocusTree$.MODULE$.observeFocusTrees();
		logger.debug("Loaded focus trees count: {}", (trees == null ? "null" : trees.size()));

		if (trees == null || trees.isEmpty()) {
			logger.warn("No focus trees found. Ensure mod files are loaded correctly.");
		} else {
			focusTreeDropdown.setItems(trees);
		}

		focusTreeDropdown.setTooltip(new Tooltip("Select a focus tree to view"));
		focusTreeDropdown.setVisibleRowCount(VISIBLE_DROPDOWN_ROW_COUNT);

		// Select first available item if list is not empty
		if (trees != null && !trees.isEmpty()) {
			focusTreeDropdown.getSelectionModel().select(0);
			logger.debug("Preselected first focus tree: {}", trees.get(0));
		}

		focusTreeDropdown.getItems().sort(Comparator.comparing(FocusTree::toString));

		// Listener for dropdown selection
		focusTreeDropdown.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue != null) {
				logger.debug("Focus tree selected: {}", newValue);
				focusTree = newValue;
				drawFocusTree();
			}
		});

		// Set up scroll pane behavior for middle mouse button
		focusTreeCanvasScrollPane.setOnMousePressed(e -> {
			if (e.getButton() == MouseButton.MIDDLE) {
				logger.debug("Middle mouse button pressed - enabling panning.");
				focusTreeCanvasScrollPane.setPannable(true);
			}
		});
		focusTreeCanvasScrollPane.setOnMouseReleased(e -> {
			if (e.getButton() == MouseButton.MIDDLE) {
				logger.debug("Middle mouse button released - disabling panning.");
				focusTreeCanvasScrollPane.setPannable(false);
			}
		});

		// Set button action
		exportFocusTreeButton.setOnAction(event -> handleExportFocusTreeButtonClick());

		// Load missing focus image
		gfxFocusUnavailable = loadFocusUnavailableImage();
		logger.debug("gfxFocusUnavailable image loaded successfully.");

		// Try loading the focus tree
		focusTree = getFocusTree();
		if (focusTree == null) {
			logger.error("Failed to load a valid focus tree. This may indicate an issue with mod loading.");
			JOptionPane.showMessageDialog(null, "Error: No valid Focus Tree found!", "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}

		logger.info("Loaded focus tree: {}", focusTree);

		// Fix localization
		if (focusTree != null) {
			try {
				logger.debug("Fixing localization for focus tree: {}", focusTree);
				FixFocus.fixLocalization(focusTree);
				logger.debug("Localization fix completed.");
			} catch (IOException e) {
				logger.error("Failed to fix localization for focus tree: {}", focusTree, e);
				JOptionPane.showMessageDialog(null, "Error fixing localization: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			}
		}

		// Draw the focus tree
		drawFocusTree();
		logger.info("FocusTreeWindow initialized successfully.");

		JOptionPane.showMessageDialog(null,
				"dev @end of initialize() [FocusTreeWindow] "
						+ "\nLoaded focuses: " + focusTree.focuses().size()
						+ "\nLoaded tree of country: " + focusTree.country().value()
						+ "\nFocus tree: " + focusTree.toString());
	}

	/**
	 * Attempts to load a valid FocusTree from different sources.
	 */
	private FocusTree getFocusTree() {
		logger.info("Attempting to load a FocusTree...");

		// Try getting by country tag
		FocusTree focusTree = FocusTree$.MODULE$.get(new CountryTag("SMA"));
		logger.debug("Attempted to load focus tree by country tag (SMA): {}", focusTree);

		// If still null, try getting the first available focus tree
		if (focusTree == null) {
			var list = FocusTree.listFocusTrees();
			logger.debug("Focus tree list size: {}", list.size());

			if (!list.isEmpty()) {
				logger.debug("First focus tree in list (raw Scala output): {}", list);
				focusTree = list.find(FocusTree::nonEmpty).getOrElse(null); // Ensure this actually returns a FocusTree
				logger.debug("Loaded first available focus tree: {}", focusTree);
			} else {
				logger.error("No focus trees found in list!");
			}
		}

		// If still null, try loading from a file
		if (focusTree == null) {
			File focusFile = new File(HOIIVFiles.Mod.focus_folder, "massachusetts.txt");
			if (focusFile.exists()) {
				logger.debug("Trying to load focus tree from file: {}",
						focusFile.getAbsolutePath());

				focusTree = FocusTree$.MODULE$.get(focusFile).getOrElse(null);
				logger.debug("Loaded focus tree from file: {}", focusTree);
			}
		}

		// Final validation
		if (focusTree == null || focusTree.focuses() == null || focusTree.focuses().isEmpty()) {
			logger.error("All focus tree loading attempts failed or the tree is empty.");
			return null;
		}

		logger.info("Successfully loaded valid focus tree: {}", focusTree);
		return focusTree;
	}


	public Canvas focusTreeCanvas() {
		return focusTreeCanvas;
	}

	private void exportFocusTree(FocusTree focusTree, String path) {
		try (PrintWriter writer = new PrintWriter(path)) {
			// Write the focus tree to the file
			writer.println("### Generated by HOI4Utils, time: " + LocalDateTime.now() + "###");
			writer.println(focusTree.toScript());
			logger.info("Exported focus tree {} to {}", focusTree, path);
		} catch (FileNotFoundException e) {
			logger.error("Error exporting focus tree: {}", e.getMessage());
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
			logger.error("Error loading focus unavailable image: {}", e.getMessage());
			return null;
		}
	}

	public void drawFocusTree() {
		logger.debug("Drawing focus tree...");
        List<Focus> focuses;
		if (focusTree == null) focuses = null;
		else focuses = CollectionConverters.asJava(focusTree.focuses());
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

		/* specifically focus drawing stuff */
		if (focuses == null || focuses.isEmpty()) return;

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

	private void drawPrerequisites(GraphicsContext gc2D, Collection<Focus> focuses, int minX) {
		gc2D.setStroke(Color.BLACK);
		gc2D.setLineWidth(3);

		// Convert the input focuses (a Java collection) into a Scala Iterable:
		var focusesWithPrereqs = focuses.stream().filter(f -> f.hasPrerequisites()).toList();

		int numFocuses = focusesWithPrereqs.size();
		logger.debug("Drawing prerequisites for {} focuses...", numFocuses);

		// Iterate over each tuple: the first element is the main focus,
		// and the second element is the list of its prerequisite focuses.
		for (var focus: focusesWithPrereqs) {
			logger.debug("Drawing {} prerequisite focus connections", focusesWithPrereqs.size());
			var prereqFocuses = focus.prerequisiteList();  

			// Calculate the main focus coordinates
			int x1 = FOCUS_X_SCALE * (focus.absoluteX() - minX) + X_OFFSET_FIX;
			int y1 = focusToCanvasY(focus);
			int linex1 = x1 + (FOCUS_X_SCALE / 2);
			int liney1 = y1 + (FOCUS_Y_SCALE / 2);
			int linex2 = linex1;
			int liney2 = y1 - 12; // For example, an upward offset

			// For each prerequisite focus, draw the connecting lines.
			for (int i = 0; i < prereqFocuses.size(); i++) {
				// Compute coordinates for the prerequisite focus.
				int linex4 = (FOCUS_X_SCALE * (prereqFocuses.apply(i).absoluteX() - minX)) + (FOCUS_X_SCALE / 2) + X_OFFSET_FIX;
				int liney4 = (FOCUS_Y_SCALE * prereqFocuses.apply(i).absoluteY()) + (FOCUS_Y_SCALE / 2) + Y_OFFSET_FIX;
				// We'll use linex2 as the start of the horizontal segment
				int linex3 = linex4;
				int liney3 = liney2; // horizontal line at same y as liney2

				// Draw the three segments:
				// 1. Vertical line from main focus center upward.
				gc2D.strokeLine(linex1, liney1, linex2, liney2);
				// 2. Horizontal line to the x coordinate of the prerequisite focus.
				gc2D.strokeLine(linex2, liney2, linex3, liney3);
				// 3. Vertical line downward to the prerequisite focus.
				gc2D.strokeLine(linex3, liney3, linex4, liney4);
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

	private void drawFocus(GraphicsContext gc2D, Focus focus, int minX) {
		boolean isSelected = selectedFocuses.contains(focus);

		gc2D.setFill(Color.WHITE);
		int x1 = FOCUS_X_SCALE * (focus.absoluteX() - minX) + X_OFFSET_FIX;
		int y1 = focusToCanvasY(focus);
		int yAdj1 = (int) (FOCUS_Y_SCALE / 2.2);
		int yAdj2 = (FOCUS_Y_SCALE / 2) + 20;

		/* focus name plate gfx (focus unavailable version) */
		gc2D.drawImage(gfxFocusUnavailable, x1 - 32, y1 + yAdj1);
		/* focus icon */ 
		var ddsImage = focus.getDDSImage(); 
		if (ddsImage.isDefined())
			gc2D.drawImage(ddsImage.get(), x1, y1);
		/* focus name text */ 
		var locName = focus.localizationText(com.hoi4utils.localization.Property.NAME); // ignore .NAME error 
		String name = locName.equals("[null]") && !focus.id().str().isBlank() ? focus.id().str() : locName;
		gc2D.fillText(name, x1 - 20, y1 + yAdj2);

		if (isSelected) {
			gc2D.setStroke(Color.YELLOW);
			gc2D.setLineWidth(2);
			gc2D.strokeRect(x1 - FOCUS_X_SCALE / 2.3, y1 + yAdj1, FOCUS_X_SCALE * 2, FOCUS_Y_SCALE / 2.3);
		}
	}

	private Focus getFocusHover(Point2D p) {
		if (focusTree == null) return null; 
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
			scala.Option<CountryTag> optionalCountry = item.countryTag();
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
		logger.info("Viewing focus tree: {}", focusTree);
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
			logger.info("Focus {} selected", draggedFocus);
		} else if (e.isSecondaryButtonDown()) {
			// if secondary click -> add focus menu
			ContextMenu contextMenu = new ContextMenu();
			MenuItem addFocusItem = new MenuItem("Add Focus");
			MenuItem newFocusTreeItem = new MenuItem("New Focus Tree");
			addFocusItem.setOnAction(event -> {
				logger.info("Adding focus via context menu");
				// open add focus menu to side of focus tree view
				Focus newFocus = new Focus(focusTree);
				focusTree.addNewFocus(newFocus);
				newFocus.setAbsoluteXY(canvasToFocusX(e.getX()), canvasToFocusY(e.getY()), false);
				newFocus.setID(focusTree.nextTempFocusID());
				openEditorWindow(newFocus, this::drawFocusTree);
			});
			newFocusTreeItem.setOnAction(event -> {
				logger.info("Creating new focus tree via context menu");
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
			if (draggedFocus.hasRelativePosition(newX, newY)) return;
            Dimension prevDim = focusTreeViewDimension();
    
			var prev = draggedFocus.setAbsoluteXY(newX, newY, e.isShiftDown());
			if (!prev.equals(draggedFocus.position())) {
				logger.info("Focus {} moved to {}, {}", draggedFocus, newX, newY);
				
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
				logger.info("Marquee selected focus: {}", focus);
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
					logger.info("Set relative focus for selected focuses");
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
		var pdxEditor = new PDXEditorPane((PDXScript<?>) focus); // this is not necessarily redundant. DO NOT CHANGE
		focusTreeViewSplitPane.getItems().removeIf(node -> node instanceof PDXEditorPane);
		focusTreeViewSplitPane.getItems().add(pdxEditor);
	}

	@FXML
	private void openEditorWindow(Focus focus, Runnable onUpdate) {
		if (focus == null) throw new IllegalArgumentException("Focus cannot be null");
		var pdxEditor = new PDXEditorPane((PDXScript<?>) focus, onUpdate); // DO NOT CHANGE
		focusTreeViewSplitPane.getItems().removeIf(node -> node instanceof PDXEditorPane);
		focusTreeViewSplitPane.getItems().add(pdxEditor); 
	}

	@FXML
	private void openNewFocusTreeWindow() {
		NewFocusTreeController newFocusTreeController = new NewFocusTreeController();
		newFocusTreeController.open((Consumer<FocusTree>) ((FocusTree f) -> {
			addFocusTree(f);
			viewFocusTree(f);
		}));
	}
}