package ui.focus_view;

//package ui.focus_view;
//
//import hoi4utils.HOIIVFile;
//import hoi4utils.clausewitz_data.country.CountryTag;
//import hoi4utils.clausewitz_data.focus.Focus;
//import hoi4utils.clausewitz_data.focus.FocusTree;
//import hoi4utils.ddsreader.DDSReader;

import hoi4utils.HOIIVFile;
import hoi4utils.clausewitz_data.country.CountryTag;
import hoi4utils.clausewitz_data.focus.FixFocus;
import hoi4utils.clausewitz_data.focus.Focus;
import hoi4utils.clausewitz_data.focus.FocusTree;
import hoi4utils.clausewitz_data.localization.FocusLocalizationFile;
import hoi4utils.ddsreader.DDSReader;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx_utils.JavaFXImageUtils;
import ui.HOIUtilsWindow;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Set;

public class FocusTreeWindow extends HOIUtilsWindow {
	static final int FOCUS_X_SCALE = 90;	  // ~2x per 1 y
	public static final int CENTER_FOCUS_X = (FOCUS_X_SCALE / 2);
	static final int FOCUS_Y_SCALE = 140;
	public static final int CENTER_FOCUS_Y = FOCUS_Y_SCALE / 2;

	@FXML Canvas focusTreeCanvas;
	@FXML ScrollPane focusTreeCanvasScrollPane;


	FocusTree focusTree;
	Tooltip focusTooltipView;
	Focus focusDetailsFocus;

	public FocusTreeWindow() {
		setFxmlResource("FocusTreeWindow.fxml");
		setTitle("Focus Tree View");
	}

	int getMaxX(){
		int max = 0;

		for (Focus focus : focusTree.focuses()){
			if (focus.absoluteX() > max)
				max = focus.absoluteX();
		}
		return max * FOCUS_X_SCALE + 150;
	}
	int getMaxY(){
		int max = 0;

		for (Focus focus : focusTree.focuses()){
			if (focus.absoluteY() > max)
				max = focus.absoluteY();
		}
		return max * FOCUS_Y_SCALE;
	}
	@FXML
	void initialize() {
		focusTree = FocusTree.get(new CountryTag("SMA"));
		if (focusTree == null) {
			focusTree = new FocusTree(new File(HOIIVFile.focus_folder + "//massachusetts.txt"));
			focusTree.setLocalization(new FocusLocalizationFile(HOIIVFile.localization_eng_folder + "\\focus_Massachusetts_SMA_l_english.yml"));
		}

		try {
			FixFocus.addFocusLoc(focusTree);
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}

		/* focus tree canvas */
		focusTreeCanvas.setWidth(getMaxX());
		focusTreeCanvas.setHeight(getMaxY());

		focusTreeCanvasScrollPane.setOnMousePressed(e -> {
			if (e.getButton() == MouseButton.MIDDLE) focusTreeCanvasScrollPane.setPannable(true);
		});
		focusTreeCanvasScrollPane.setOnMouseReleased(e -> {
			if (e.getButton() == MouseButton.MIDDLE) focusTreeCanvasScrollPane.setPannable(false);
		});

		drawFocusTree(focusTree);
	}
	public Canvas focusTreeCanvas() {
		return focusTreeCanvas;
	}

	public void drawFocusTree(FocusTree focusTree) {
		//		if (focusTree != null) {    // todo
		// }

		GraphicsContext gc2D = focusTreeCanvas.getGraphicsContext2D();

		int minX = -focusTree.minX();       // todo
		double width = focusTreeCanvas.getWidth();
		double height = focusTreeCanvas.getHeight();
		gc2D.setFill(Color.DARKGRAY);
		gc2D.fillRect(0, 0, width, height);

		/* dds stuff */
		Image GFX_focus_unavailable = null;
		try {
			FileInputStream fis = new FileInputStream("hoi4files\\gfx\\focus_unavailable_bg.dds");
			byte[] buffer = new byte[fis.available()];
			fis.read(buffer);
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
			int x1 = FOCUS_X_SCALE * (focus.absoluteX() + minX);
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
						int linex4 = (FOCUS_X_SCALE * (prereqFocus.absoluteX() + minX)) + (FOCUS_X_SCALE / 2);
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
					int x1 = FOCUS_X_SCALE * (focus.absoluteX() + minX) + CENTER_FOCUS_X;
					int y1 = FOCUS_Y_SCALE * focus.absoluteY() + FOCUS_Y_SCALE / 3;
					int x2 = (FOCUS_X_SCALE * (mutexFocus.absoluteX() + minX)) + CENTER_FOCUS_X;
					int y2 = (FOCUS_Y_SCALE * mutexFocus.absoluteY()) + FOCUS_Y_SCALE / 3;
					gc2D.strokeLine(x1, y1, x2, y2);
				}
			}
		}

		/* focus image */
		for (Focus focus : focusTree.focuses()){
			int x1 = FOCUS_X_SCALE * (focus.absoluteX() + minX);
			int y1 = FOCUS_Y_SCALE * focus.absoluteY();
			int yAdj1 = (int)(FOCUS_Y_SCALE / 2.2);
			int yAdj2 = (FOCUS_Y_SCALE / 2) + 20;
			gc2D.drawImage(GFX_focus_unavailable, x1 - 32, y1 + yAdj1);
			gc2D.drawImage(focus.getDDSImage(), x1, y1);
			String name = focus.name();
			gc2D.fillText(name, x1, y1 + yAdj2);
		}
	}



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

}