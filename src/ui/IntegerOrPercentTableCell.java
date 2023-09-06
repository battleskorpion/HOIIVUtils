package ui;

import javafx.scene.control.TableCell;
import javafx.util.StringConverter;
import java.text.DecimalFormat;

public class IntegerOrPercentTableCell<S> extends TableCell<S, Double> {

	private final StringConverter<Double> converter;

	private boolean integer;        // if integer is true, percent is false, both can be false.

	private boolean percent;        // if percent is true, integer is false, both can be false
									// for a regular double with no % symbol.

	public IntegerOrPercentTableCell() {
		this.converter = new StringConverter<Double>() {
			@Override
			public String toString(Double object) {
				if (object == null) {
					return "";
				} else {
					// Format the double value with commas in the thousands places,
					// exactly 1 decimal place, and allow for an optional zero in the decimal part
					DecimalFormat decimalFormat = new DecimalFormat("#,##0.0#");
					String formattedValue = decimalFormat.format(object);
					return formattedValue;
				}
			}

			@Override
			public Double fromString(String string) {
				// You can implement this method if needed
				return null;
			}
		};
	}

	public boolean toggleInteger() {
		return setInteger(!integer);
	}

	public boolean togglePercent() {
		return setPercent(!percent);
	}

	public boolean isIntegerEnabled() {
		return integer;
	}

	public boolean setInteger(boolean integer) {
		this.integer = integer;
		if (integer) {
			this.percent = false;
		}
		return integer;
	}

	public boolean isPercentEnabled() {
		return percent;
	}

	public boolean setPercent(boolean percent) {
		this.percent = percent;
		if (percent) {
			this.integer = false;
		}
		return percent;
	}
}
