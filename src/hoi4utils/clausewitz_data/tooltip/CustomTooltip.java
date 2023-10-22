package hoi4utils.clausewitz_data.tooltip;

import hoi4utils.clausewitz_data.localization.Localization;
import hoi4utils.clausewitz_parser_deprecated.Expression;
import hoi4utils.clausewitz_parser_deprecated.Parser;

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
		Expression[] tooltipExpressions = parser.findAll("custom_trigger_tooltip"); // prev = parser.expression().getAll("custom_trigger_tooltip");
		for (Expression exp : tooltipExpressions) {
//			System.out.println("expression: " + exp);
			Expression tooltipExp = exp.getSubexpression("tooltip");
//			System.out.println("subexpression: " + tooltipExp);
			String expID = tooltipExp.getText();
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
