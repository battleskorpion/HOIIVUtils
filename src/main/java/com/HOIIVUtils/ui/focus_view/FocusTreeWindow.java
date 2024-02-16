package com.HOIIVUtils.ui.focus_view;

import com.HOIIVUtils.hoi4utils.Settings;
import com.HOIIVUtils.hoi4utils.clausewitz_data.localization.IllegalLocalizationFileTypeException;
import com.HOIIVUtils.hoi4utils.ddsreader.DDSReader;
import com.HOIIVUtils.hoi4utils.HOIIVFile;
import com.HOIIVUtils.hoi4utils.clausewitz_data.country.CountryTag;
import com.HOIIVUtils.hoi4utils.clausewitz_data.focus.FixFocus;
import com.HOIIVUtils.hoi4utils.clausewitz_data.focus.Focus;
import com.HOIIVUtils.hoi4utils.clausewitz_data.focus.FocusTree;
import com.HOIIVUtils.hoi4utils.clausewitz_data.localization.FocusLocalizationFile;
import javafx.fxml.FXML;
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
import com.HOIIVUtils.ui.HOIUtilsWindow;

import javax.swing.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

public class FocusTreeWindow extends HOIUtilsWindow {
	static final int FOCUS_X_SCALE = 90;	  // ~2x per 1 y
	public static final int CENTER_FOCUS_X = (FOCUS_X_SCALE / 2);
	static final int FOCUS_Y_SCALE = 140;
	public static final int CENTER_FOCUS_Y = FOCUS_Y_SCALE / 2;
	public static final int VISIBLE_DROPDOWN_ROW_COUNT = 20;

	@FXML Canvas focusTreeCanvas;
	@FXML ScrollPane focusTreeCanvasScrollPane;
	@FXML ComboBox<FocusTree> focusTreeDropdown;

	private FocusTree focusTree;
	private Tooltip focusTooltipView;
	private Focus focusDetailsFocus;

	private double mouseX, mouseY;
	private Focus draggedFocus;


	public FocusTreeWindow() {
		setFxmlResource("FocusTreeWindow.fxml");
		setTitle("Focus Tree View");
	}

	private int getMinX() {
		return focusTree.minX();
	}

	int getMaxX(){
		return focusTree.focuses().stream().mapToInt(Focus::absoluteX).max().orElse(200);
	}

	int getMaxY(){
		return focusTree.focuses().stream().mapToInt(Focus::absoluteY).max().orElse(200);
	}

	@FXML
	void initialize() {
		focusTreeDropdown.setItems(FocusTree.observeFocusTrees());
		focusTreeDropdown.setTooltip(new Tooltip("Select a focus tree to view"));
		focusTreeDropdown.getSelectionModel().select(0);
		focusTreeDropdown.setVisibleRowCount(VISIBLE_DROPDOWN_ROW_COUNT);
		focusTreeDropdown.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue != null) {
				focusTree = newValue;
				drawFocusTree(focusTree);
			}
		});
		focusTreeDropdown.setEditable(false);
		// Enable auto-selection based on user typing
		focusTreeDropdown.setOnKeyTyped(event -> {
			String typedText = focusTreeDropdown.getEditor().getText();
			selectClosestMatch(focusTreeDropdown, typedText);
		});

		focusTree = FocusTree.get(new CountryTag("SMA"));
		try {
			focusTree.setLocalization(new FocusLocalizationFile(HOIIVFile.localization_eng_folder + "\\focus_Massachusetts_SMA_l_english.yml"));
		} catch (IllegalLocalizationFileTypeException e) {
			throw new RuntimeException(e);
		}
		if (focusTree == null) {
			focusTree = new FocusTree(new File(HOIIVFile.focus_folder + "//massachusetts.txt"));
			try {
				focusTree.setLocalization(new FocusLocalizationFile(HOIIVFile.localization_eng_folder + "\\focus_Massachusetts_SMA_l_english.yml"));
			} catch (IllegalLocalizationFileTypeException e) {
				throw new RuntimeException(e);
			}
		}

		try {
			FixFocus.addFocusLoc(focusTree);
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}

		/* focus tree canvas */
		focusTreeCanvas.setWidth((double) (getMaxX() + -getMinX()) * FOCUS_X_SCALE + 180);
		focusTreeCanvas.setHeight((getMaxY() * FOCUS_Y_SCALE) + (FOCUS_Y_SCALE * 2));

		focusTreeCanvasScrollPane.setOnMousePressed(e -> {
			if (e.getButton() == MouseButton.MIDDLE) focusTreeCanvasScrollPane.setPannable(true);
		});
		focusTreeCanvasScrollPane.setOnMouseReleased(e -> {
			if (e.getButton() == MouseButton.MIDDLE) focusTreeCanvasScrollPane.setPannable(false);
		});

		/* draw the focus tree */
		drawFocusTree(focusTree);

		if (Settings.DEV_MODE.enabled()) {
			JOptionPane.showMessageDialog(null, "dev @end of initialize() - loaded focuses: " + focusTree.focuses().size()
			+ "\n" + "loaded tree of country: " + focusTree.country()
			+ "\n" + "draw focus tree: " + Settings.DRAW_FOCUS_TREE.enabled());
		}
	}

	public Canvas focusTreeCanvas() {
		return focusTreeCanvas;
	}

	public void drawFocusTree(FocusTree focusTree) {
		if (Settings.DRAW_FOCUS_TREE.disabled()) return;

		//		if (focusTree != null) {    // todo
		// }

		GraphicsContext gc2D = focusTreeCanvas.getGraphicsContext2D();

		int minX = -getMinX();       // todo
		final int X_OFFSET_FIX = 30;
//		JOptionPane.showMessageDialog(null, minX);
		double width = focusTreeCanvas.getWidth();
		double height = focusTreeCanvas.getHeight();
		gc2D.setFill(Color.DARKGRAY);
		gc2D.fillRect(0, 0, width, height);

		/* dds stuff */
		Image GFX_focus_unavailable = null;
		try {
			InputStream fis = getClass().getClassLoader().getResourceAsStream("com/HOIIVUtils/hoi4utils/hoi4files/gfx/focus_unavailable_bg.dds");
			if (fis == null) {
				throw new FileNotFoundException("Unable to find 'com/HOIIVUtils/hoi4utils/hoi4files/gfx/focus_unavailable_bg.dds'");
			}
			byte[] buffer = new byte[fis.available()];
			int bytesRead = fis.read(buffer);
			fis.close();
			int[] ddspixels = DDSReader.read(buffer, DDSReader.ARGB, 0);
			int ddswidth = DDSReader.getWidth(buffer);
			int ddsheight = DDSReader.getHeight(buffer);

			GFX_focus_unavailable = JavaFXImageUtils.imageFromDDS(ddspixels, ddswidth, ddsheight);
		} catch (IOException exc) {
			exc.printStackTrace();
		}

		for (Focus focus : focusTree.focuses()) {
			gc2D.setFill(Color.WHITE);
			int x1 = FOCUS_X_SCALE * (focus.absoluteX() + minX) + X_OFFSET_FIX;
			int y1 = FOCUS_Y_SCALE * focus.absoluteY();
			int yAdj1 = (int)(FOCUS_Y_SCALE / 2.2);
			int yAdj2 = (FOCUS_Y_SCALE / 2) + 20;

			//gc2D.strokeRect(x1, y1, 100, 100);
//			BufferedImage image = ImageIO.read(new File(focus.icon()));   // todo
//			gc2D.drawImage(GFX_focus_unavailable, x1 - 32, y1 + yAdj1);
//			gc2D.drawImage(focus.getDDSImage(), x1, y1);

			if (focus.hasPrerequisites()) {
				gc2D.setStroke(Color.BLACK);
				gc2D.setLineWidth(3);

				for (Set<Focus> prereqFocusSet : focus.getPrerequisites()) {
					for (Focus prereqFocus : prereqFocusSet) {
						int linex1 = x1 + (FOCUS_X_SCALE / 2);
						int liney1 = y1;
						int linex2 = linex1;
						int liney2 = liney1 - 12;
						int liney4 = (FOCUS_Y_SCALE * prereqFocus.absoluteY());
						int linex4 = (FOCUS_X_SCALE * (prereqFocus.absoluteX() + minX)) + (FOCUS_X_SCALE / 2) + X_OFFSET_FIX;
						int linex3 = linex4;
						int liney3 = liney2;

						gc2D.strokeLine(linex1, liney1, linex2, liney2);
						gc2D.strokeLine(linex2, liney2, linex3, liney3);
						gc2D.strokeLine(linex3, liney3, linex4, liney4);
					}
				}
			}
		}

		/* mutually exclusive */
		gc2D.setStroke(Color.DARKRED);
		for (Focus focus : focusTree.focuses()) {
			if (focus.isMutuallyExclusive()) {
				for (Focus mutexFocus : focus.getMutuallyExclusive()) {
					int x1 = FOCUS_X_SCALE * (focus.absoluteX() + minX) + CENTER_FOCUS_X + X_OFFSET_FIX;
					int y1 = FOCUS_Y_SCALE * focus.absoluteY() + FOCUS_Y_SCALE / 3;
					int x2 = (FOCUS_X_SCALE * (mutexFocus.absoluteX() + minX)) + CENTER_FOCUS_X + X_OFFSET_FIX;
					int y2 = (FOCUS_Y_SCALE * mutexFocus.absoluteY()) + FOCUS_Y_SCALE / 3;
					gc2D.strokeLine(x1, y1, x2, y2);
				}
			}
		}

		/* focus image */
		for (Focus focus : focusTree.focuses()){
			int x1 = FOCUS_X_SCALE * (focus.absoluteX() + minX) + X_OFFSET_FIX;
			int y1 = FOCUS_Y_SCALE * focus.absoluteY();
			int yAdj1 = (int)(FOCUS_Y_SCALE / 2.2);
			int yAdj2 = (FOCUS_Y_SCALE / 2) + 20;
			gc2D.drawImage(GFX_focus_unavailable, x1 - 32, y1 + yAdj1);
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
		if no focus being hovered over -> if there was a focus detail view open,
		get rid of it and reset
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
//		focusTreeDetailsWindow = new FocusTreeDetailsWindow(focusDetailsFocus, p);
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