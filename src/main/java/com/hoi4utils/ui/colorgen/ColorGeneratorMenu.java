//package main.java.com.HOIIVUtils.ui.colorgen;
//
//import main.java.com.HOIIVUtils.hoi4utils.map.province.ColorGenerator;
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
