package main.java.com.hoi4utils.clauzewitz.data.tooltip;

import main.kotlin.com.hoi4utils.clausewitz_parser.ParserException;
import main.java.com.hoi4utils.clauzewitz.localization.Localization;
import main.kotlin.com.hoi4utils.clausewitz_parser.Node;
import main.kotlin.com.hoi4utils.clausewitz_parser.Parser;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/*
 * CustomTooltip File
 */
public class CustomTooltip {
	String tooltipID;
	public static ArrayList<CustomTooltip> tooltips;
	private Localization localization;      // localization text for tooltip id

	public CustomTooltip(String ID) {
		this.tooltipID = ID;
	}

	public static List<Function<CustomTooltip, ?>> getDataFunctions() {
		List<Function<CustomTooltip, ?>> dataFunctions = new ArrayList<>(2);         // 2 for optimization, limited number of data functions.

		dataFunctions.add(CustomTooltip::id);
		dataFunctions.add(CustomTooltip::getTooltipText);

		return dataFunctions;
	}

	public String id() {
		return tooltipID;
	}

	public static void loadTooltips(File file) {
		tooltips = new ArrayList<>();

		if (!validFile(file)) return;

		Parser parser = new Parser(file);
		Node rootNode = null;
		try {
			rootNode = parser.parse();
		} catch (ParserException e) {
			throw new RuntimeException(e);
		}
		Node[] tooltipExpressions = rootNode.filter("custom_trigger_tooltip").toList().toArray(new Node[0]); // prev2 = prev = parser.expression().getAll("custom_trigger_tooltip");
		for (Node exp : tooltipExpressions) {
			if (!exp.contains("tooltip")) {
				continue;
			}
			String expID = exp.getValue("tooltip").string();
			if (expID == null) {
				continue;
			}
			tooltips.add(new CustomTooltip(expID));
		}
	}

	private static boolean validFile(File file) {
		if (file == null) {
			System.err.println("File null in: " + CustomTooltip.class + "-> loadTooltips()");
			return false;
		}
		if (!file.exists()) {
			System.err.println("File does not exist: " + file);
			return false;
		}
		if (!file.isFile()) {
			System.err.println("File is not file: " + file);
			return false;
		}
		return true;
	}

	public static ArrayList<CustomTooltip> getTooltips() {
		if (tooltips.isEmpty()) {
			return null;
		}

		return tooltips;
	}

	public String toString() {
		if (tooltipID != null) {
			return tooltipID;
		}

		return super.toString();
	}

	public void setLocalization(Localization tooltipLocalization) {
		this.localization = tooltipLocalization;
	}

	public Localization localization() {
		return localization;
	}

	public String getTooltipText() {
		if (localization == null) {
			return null;
		}
		return localization.text();
	}
}
