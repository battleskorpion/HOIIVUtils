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
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx_utils.JavaFXImageUtils;
import org.jetbrains.annotations.NotNull;
import ui.HOIUtilsWindow;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Set;

public class FocusTreeWindow extends HOIUtilsWindow {
	static final int FOCUS_X_SCALE = 90;	  // ~2x per 1 y
	static final int FOCUS_Y_SCALE = 140;

	@FXML Canvas focusTreeCanvas;
	@FXML ScrollPane focusTreeCanvasScrollPane;

	FocusTree focusTree;
	Tooltip focusTooltipView;
	Focus focusDetailsFocus;

	public FocusTreeWindow() {
		setFxmlResource("FocusTreeWindow.fxml");
		setTitle("Focus Tree View");
	}

	@FXML
	void initialize() {
		focusTreeCanvas.setWidth(4000);
		focusTreeCanvas.setHeight(2000);
//		focusTreeCanvasScrollPane.setFitToWidth(true);
//		focusTreeCanvasScrollPane.setFitToHeight(true);

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

			gc2D.strokeRect(x1, y1, 100, 100);
//			BufferedImage image = ImageIO.read(new File(focus.icon()));   // todo
			gc2D.drawImage(GFX_focus_unavailable, x1 - 32, y1 + yAdj1);
			gc2D.drawImage(focus.getDDSImage(), x1, y1);

			if (focus.hasPrerequisites()) {
				gc2D.setStroke(Color.BLACK);

				for (Set<Focus> prereqFocusSet : focus.getPrerequisites()) {
					for (Focus prereqFocus : prereqFocusSet) {
						int linex1 = x1 + (FOCUS_X_SCALE / 2);
						int liney1 = y1;
						int linex2 = (FOCUS_X_SCALE * (prereqFocus.absoluteX() + minX)) + (FOCUS_X_SCALE / 2);
						int liney2 = (FOCUS_Y_SCALE * prereqFocus.absoluteY()) + 100;
						gc2D.strokeLine(linex1, liney1, linex2, liney2);
					}
				}
			}

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
		String details = getFocusDetails(focusDetailsFocus);
		focusTooltipView = new Tooltip(details);
		focusTooltipView.show(focusTreeCanvas, e.getScreenX() + 10, e.getScreenY() + 10);
	}

	@NotNull
	private String getFocusDetails(Focus focus) {       // todo modification of this should be in Focus class
		//		focusTreeDetailsWindow.show();
		StringBuilder details = new StringBuilder();
		details.append("\n");
		details.append("Completion time: ");
		details.append(focus.completionTime());
		details.append("\n");

		for (Set<Focus> prereqSet : focus.getPrerequisites()) {
			if (prereqSet.size() > 1) {
				details.append("Requires one of the following: \n");
				for (Focus f : prereqSet) {
					details.append("- ");
					details.append(f.nameLocalization());
					details.append("\n");
				}
			} else {
				details.append("Requires: ");
				details.append(prereqSet.iterator().next().nameLocalization());
				details.append("\n");
			}
		}

		details.append("\n\nEffect: \n");
		return details.toString();
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

/*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*//*/
//
//import hoi4utils.HOIIVFile;
//import hoi4utils.clausewitz_data.country.CountryTag;
//import hoi4utils.clausewitz_data.focus.Focus;
//import hoi4utils.clausewitz_data.focus.FocusTree;
//import hoi4utils.ddsreader.DDSReader;
//
//import javax.swing.*;
//import javax.swing.border.LineBorder;
//import java.awt.*;
//import java.awt.event.MouseAdapter;
//import java.awt.event.MouseEvent;
//import java.awt.event.MouseWheelEvent;
//import java.awt.image.BufferedImage;
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.IOException;
//import java.util.Set;
//
//public class FocusTreeWindow extends JFrame {
//	private JPanel TreeBuilderJPanel;
//	private FocusTreeViewport focusTreeBuilderWindow$FocusTreeViewport1;
//
//	public FocusTreeWindow() {
//		super ("Focus Tree");
//
//		/* window */
//		setContentPane(TreeBuilderJPanel);
//		setSize(700, 500);
//		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
//		pack();
//
//		/* draw focus tree */
//		focusTreeBuilderWindow$FocusTreeViewport1.repaint();   // have to draw after window stuff
//		focusTreeBuilderWindow$FocusTreeViewport1.setVisible(true);
////		focusTreeViewport.setViewPosition(new Point(-256, 0));
//	}
//
//	public void createUIComponents() {
//		FocusTree focusTree = FocusTree.get(new CountryTag("SMA"));
//		if (focusTree == null) {
//			focusTree = new FocusTree(new File(HOIIVFile.focus_folder + "//massachusetts.txt"));
//		}
//		focusTreeBuilderWindow$FocusTreeViewport1 = new FocusTreeViewport(focusTree);
//	}
//
//	public class FocusTreeViewport extends JPanel {
//		//JPanel riverMapJPanel;
//
//		private static final int X_SCALE = 90;	  // ~2x per 1 y
//		private static final int Y_SCALE = 140;
//		private FocusTree focusTree;
//
//		public FocusTreeViewport(FocusTree focusTree) {
//			/* init */
//			this.focusTree = focusTree;
//
//			FocusPanel focusPanel = new FocusPanel();
//			this.setLayout(new BorderLayout());
//			this.add(focusPanel.scrollPane, BorderLayout.CENTER);
//			this.setPreferredSize(new Dimension((int) Toolkit.getDefaultToolkit().getScreenSize().getWidth() - 200, (int) Toolkit.getDefaultToolkit().getScreenSize().getHeight() - 200));
//			this.setVisible(true);
//		}
//
//		public class FocusPanel extends JPanel implements Scrollable {
//			JScrollPane scrollPane;
//
//			public FocusPanel() {
//				super();
//
//				/* scrollPane */
//				scrollPane = new JScrollPane(this);
//				scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
//				scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
//				scrollPane.setPreferredSize(new Dimension(1000, 1000));
//				scrollPane.setViewportBorder(new LineBorder(Color.RED));
//
//				MouseAdapter adapter = new MouseAdapter() {
//					FocusTreeDetailsWindow focusTreeDetailsWindow;
//					Focus focus;
//
//					/**
//					 * {@inheritDoc}
//					 *
//					 * @param e
//					 * @since 1.6
//					 */
//					@Override
//					public void mouseWheelMoved(MouseWheelEvent e) {
//						super.mouseWheelMoved(e);
//					}
//
//					/**
//					 * {@inheritDoc}
//					 *
//					 * @param e
//					 * @since 1.6
//					 */
//					@Override
//					public void mouseDragged(MouseEvent e) {
//						super.mouseDragged(e);
//					}
//
//					/**
//					 * {@inheritDoc}
//					 *
//					 * @param e
//					 * @since 1.6
//					 */
//					@Override
//					public void mouseMoved(MouseEvent e) {
//						super.mouseMoved(e);
//
//						/* get focus being hovered over */
//						Point p = e.getPoint();
//						Focus focusTemp = getFocusHover(p);
//						if (focusTemp == null) {
//							/*
//							if no focus being hovered over -> if there was a focus detail view open,
//							get rid of it and reset
//							 */
//							if (focusTreeDetailsWindow != null) {
//								focusTreeDetailsWindow.dispose();
//								focusTreeDetailsWindow = null;
//								focus = null;
//							}
//							return;
//						}
//						if (focusTemp == focus) {
//							return;
//						}
//						focus = focusTemp;
//
//						/* focus details view */
//						if (focusTreeDetailsWindow != null) {
//							focusTreeDetailsWindow.dispose();
//						}
//						focusTreeDetailsWindow = new FocusTreeDetailsWindow(focus, p);
//						focusTreeDetailsWindow.setVisible(true);
//					}
//				};
//				addMouseListener(adapter);
//				addMouseMotionListener(adapter);
//			}
//
//			private Focus getFocusHover(Point p) {
//				int x = (int) (p.getX() / X_SCALE) + focusTree.minX();
//				int y = (int) (p.getY() / Y_SCALE);
//
//				for (Focus f : focusTree.focuses()) {
//					if (f.absolutePosition().equals(new Point(x, y))) {
//						return f;
//					}
//				}
//
//				return null;
//			}
//
//			public void drawFocus(Graphics g, Focus focus) {
//
//			}
//
//			public void removeFocus(Focus focus) {
//
//			}
//
//			@Override
//			public Dimension getPreferredSize() {
//				int width = 100 * X_SCALE;
//				return new Dimension(width, 50 * Y_SCALE);
//			}
//
//			@Override
//			public Dimension getMinimumSize() {
//				return new Dimension(10 * X_SCALE, 10 * Y_SCALE);
//			}
//
//			/**
//			 * draws each focus as well as related graphics
//			 * @param g the <code>Graphics</code> object to protect
//			 */
//			@Override
//			protected void paintComponent(Graphics g) {
//				super.paintComponent(g);
//
//				if (focusTree != null) {
//					int minX = -focusTree.minX();
//					int width = getWidth();
//					int height = getHeight();
//					Graphics2D g2d = (Graphics2D) g.create();
//					g2d.setColor(Color.DARK_GRAY);
//					g2d.drawRect(0, 0, width, height);
////					int xPos = 0;
//
//					/* dds stuff */
//					BufferedImage GFX_focus_unavailable = null;
//					try {
//						FileInputStream fis = new FileInputStream("hoi4files\\gfx\\focus_unavailable_bg.dds");
//						byte[] buffer = new byte[fis.available()];
//						fis.read(buffer);
//						fis.close();
//						int[] ddspixels = DDSReader.read(buffer, DDSReader.ARGB, 0);
//						int ddswidth = DDSReader.getWidth(buffer);
//						int ddsheight = DDSReader.getHeight(buffer);
//						GFX_focus_unavailable = new BufferedImage(ddswidth, ddsheight, BufferedImage.TYPE_INT_ARGB);
//						GFX_focus_unavailable.setRGB(0, 0, ddswidth, ddsheight, ddspixels, 0, ddswidth);
//					} catch (IOException exc) {
//						exc.printStackTrace();
//					}
//
//					for (Focus focus : focusTree.focuses()) {
//						g2d.setColor(Color.WHITE);
//						int x1 = X_SCALE * (focus.absoluteX() + minX);
//						int y1 = Y_SCALE * focus.absoluteY();
//						int yAdj1 = (int)(Y_SCALE / 2.2);
//						int yAdj2 = (Y_SCALE / 2) + 20;
//
//						g2d.drawRect(x1, y1, 100, 100);
////							BufferedImage image = ImageIO.read(new File(focus.icon()));   // todo
//						g2d.drawImage(GFX_focus_unavailable, x1 - 32, y1 + yAdj1, null);
//						g2d.drawImage(focus.getDDSImage(), x1, y1, null);
//
//						String name;
//						if (focus.nameLocalization() == null) {
//							name = focus.id();
//						} else {
//							name = focus.nameLocalization();
//						}
//						g2d.drawString(name, x1, y1 + yAdj2);
//
////						xPos += X_SCALE;
//
//						if (focus.hasPrerequisites()) {
////							System.out.println(focus.id() + ", " + focus.getPrerequisites());
//							g2d.setColor(Color.WHITE);
//
//							for (Set<Focus> prereqFocusSet : focus.getPrerequisites()) {
//								for (Focus prereqFocus : prereqFocusSet) {
//									int linex1 = x1 + (X_SCALE / 2);
//									int liney1 = y1;
//									int linex2 = (X_SCALE * (prereqFocus.absoluteX() + minX)) + (X_SCALE / 2);
//									int liney2 = (Y_SCALE * prereqFocus.absoluteY()) + 100;
//									g2d.drawLine(linex1, liney1, linex2, liney2);
//								}
//							}
//						}
//					}
//					g2d.dispose();
//				}
//			}
//
//			/**
//			 * Returns the preferred size of the viewport for a view component.
//			 * For example, the preferred size of a <code>JList</code> component
//			 * is the size required to accommodate all of the cells in its list.
//			 * However, the value of <code>preferredScrollableViewportSize</code>
//			 * is the size required for <code>JList.getVisibleRowCount</code> rows.
//			 * A component without any properties that would affect the viewport
//			 * size should just return <code>getPreferredSize</code> here.
//			 *
//			 * @return the preferredSize of a <code>JViewport</code> whose view
//			 * is this <code>Scrollable</code>
//			 * @see JViewport#getPreferredSize
//			 */
//			@Override
//			public Dimension getPreferredScrollableViewportSize() {
//				return new Dimension(Toolkit.getDefaultToolkit().getScreenSize());
//			}
//
//			/**
//			 * Components that display logical rows or columns should compute
//			 * the scroll increment that will completely expose one new row
//			 * or column, depending on the value of orientation.  Ideally,
//			 * components should handle a partially exposed row or column by
//			 * returning the distance required to completely expose the item.
//			 * <p>
//			 * Scrolling containers, like JScrollPane, will use this method
//			 * each time the user requests a unit scroll.
//			 *
//			 * @param visibleRect The view area visible within the viewport
//			 * @param orientation Either SwingConstants.VERTICAL or SwingConstants.HORIZONTAL.
//			 * @param direction   Less than zero to scroll up/left, greater than zero for down/right.
//			 * @return The "unit" increment for scrolling in the specified direction.
//			 * This value should always be positive.
//			 * @see JScrollBar#setUnitIncrement
//			 */
//			@Override
//			public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
//				return 1;
//			}
//
//			/**
//			 * Components that display logical rows or columns should compute
//			 * the scroll increment that will completely expose one block
//			 * of rows or columns, depending on the value of orientation.
//			 * <p>
//			 * Scrolling containers, like JScrollPane, will use this method
//			 * each time the user requests a block scroll.
//			 *
//			 * @param visibleRect The view area visible within the viewport
//			 * @param orientation Either SwingConstants.VERTICAL or SwingConstants.HORIZONTAL.
//			 * @param direction   Less than zero to scroll up/left, greater than zero for down/right.
//			 * @return The "block" increment for scrolling in the specified direction.
//			 * This value should always be positive.
//			 * @see JScrollBar#setBlockIncrement
//			 */
//			@Override
//			public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
//				return 1;
//			}
//
//			/**
//			 * Return true if a viewport should always force the width of this
//			 * <code>Scrollable</code> to match the width of the viewport.
//			 * For example a normal
//			 * text view that supported line wrapping would return true here, since it
//			 * would be undesirable for wrapped lines to disappear beyond the right
//			 * edge of the viewport.  Note that returning true for a Scrollable
//			 * whose ancestor is a JScrollPane effectively disables horizontal
//			 * scrolling.
//			 * <p>
//			 * Scrolling containers, like JViewport, will use this method each
//			 * time they are validated.
//			 *
//			 * @return True if a viewport should force the Scrollables width to match its own.
//			 */
//			@Override
//			public boolean getScrollableTracksViewportWidth() {
//				return getPreferredSize().width
//						<= getParent().getSize().width;
//			}
//
//			/**
//			 * Return true if a viewport should always force the height of this
//			 * Scrollable to match the height of the viewport.  For example a
//			 * columnar text view that flowed text in left to right columns
//			 * could effectively disable vertical scrolling by returning
//			 * true here.
//			 * <p>
//			 * Scrolling containers, like JViewport, will use this method each
//			 * time they are validated.
//			 *
//			 * @return True if a viewport should force the Scrollables height to match its own.
//			 */
//			@Override
//			public boolean getScrollableTracksViewportHeight() {
//				return getPreferredSize().height
//						<= getParent().getSize().height;
//			}
//
//		}
//
//	}
//}
