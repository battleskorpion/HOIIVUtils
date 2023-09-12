package ui.javafx.table;

import javafx.scene.control.TableCell;
import javafx.util.StringConverter;
import java.text.DecimalFormat;

public class IntegerOrPercentTableCell<S> extends TableCell<S, Double> {

	private boolean integer;
	private boolean percent;
	private String decimalStringFormat;

	public IntegerOrPercentTableCell() {
		new StringConverter<Double>() {
			@Override
			public String toString(Double object) {
				if (object == null) {
					decimalStringFormat = "";
					return decimalStringFormat;
				} else if (integer) {
					decimalStringFormat = "#,##0";
					DecimalFormat decimalFormat = new DecimalFormat(decimalStringFormat);
					return decimalFormat.format(object);
				} else if (percent) {
					decimalStringFormat = "#,##0.0#%";
					DecimalFormat decimalFormat = new DecimalFormat(decimalStringFormat);
					return decimalFormat.format(object);
				} else {
					/**
					 * Format the double value with commas in the thousands places,
					 * exactly 1 decimal place, and allow for an optional zero in the decimal part
					 */
					decimalStringFormat = "#,##0.0#";
					DecimalFormat decimalFormat = new DecimalFormat(decimalStringFormat);
					return decimalFormat.format(object);
				}
			}

			@Override
			public Double fromString(String string) {
				return null;
			}
		};
	}
	
	public boolean getInteger() {
		return integer;
	}
	
	public boolean getPercent() {
		return percent;
	}

	/** 
	 * if integer is true, percent is false, 
	 * both can be false
	 * both can NOT be true
	 * for a regular double with no % symbol.
	 */
	public boolean setInteger(boolean integer) {
		this.integer = integer;
		if (integer) {
			this.percent = false;
		}
		return integer;

	}

	/** 
	 * if percent is true, integer is false, 
	 * both can be false
	 * both can NOT be true
	 * 
	 */
	public boolean setPercent(boolean percent) {
		this.percent = percent;
		if (percent) {
			this.integer = false;
		}
		return percent;
	}
	
	public boolean toggleInteger() {
		return setInteger(!integer);
	}

	public boolean togglePercent() {
		return setPercent(!percent);
	}
}
