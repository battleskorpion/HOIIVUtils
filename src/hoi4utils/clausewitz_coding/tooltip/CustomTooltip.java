package hoi4utils.clausewitz_coding.tooltip;

import hoi4utils.clausewitz_coding.country.Country;
import hoi4utils.clausewitz_parser.Expression;
import hoi4utils.clausewitz_parser.Parser;
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

	public CustomTooltip(String ID) {
		this.tooltipID = ID;
	}

	public static List<Function<CustomTooltip, ?>> getDataFunctions() {
		List<Function<CustomTooltip, ?>> dataFunctions = new ArrayList<>(18);         // 18 for optimization, limited number of data functions.

		dataFunctions.add(CustomTooltip::getID);

		return dataFunctions;
	}

	public String getID() {
		return tooltipID;
	}

	public static void loadTooltips(File file) {
		tooltips = new ArrayList<>();

		if (file == null) {
			System.err.println("File null in: " + "CustomTooltip.java -> loadTooltips()");
			return;
		}
		if (!file.exists()) {
			System.err.println("File does not exist: " + file);
		}
		if (!file.isFile()) {
			System.err.println("File is not file: " + file);
		}

		Parser parser = new Parser(file);
		Expression[] tooltipExpressions = parser.expression().getAll("custom_trigger_tooltip");
		for (Expression exp : tooltipExpressions) {
			System.out.println("expression: " + exp);
			Expression tooltipExp = exp.getSubexpression("tooltip");
			System.out.println("subexpression: " + tooltipExp);
			String expID = tooltipExp.getText();
			if (expID == null) {
				continue;
			}
			tooltips.add(new CustomTooltip(expID));
		}
	}

	public static CustomTooltip[] getTooltips() {
		if (tooltips.size() == 0) {
			return null;
		}

		return tooltips.toArray(new CustomTooltip[]{});
	}

	public String toString() {
		if (tooltipID != null) {
			return tooltipID;
		}

		return super.toString();
	}
}
