package ui.table_data;

import javafx.scene.control.TableCell;
import javafx.util.StringConverter;
import java.text.DecimalFormat;

public class DoubleTableCell<S> extends TableCell<S, Double> {

	private final StringConverter<Double> converter;

	protected String decimalStringFormat;

	public DoubleTableCell() {
		this.converter = new StringConverter<Double>() {
			@Override
			public String toString(Double object) {
				if (object == null) {
					return "";
				} else {
					/**
					 * exactly 1 decimal place, and allow for an optional zero in the decimal part
					 * Format the double value with commas in the thousands places,
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

	@Override
	protected void updateItem(Double item, boolean empty) {
		super.updateItem(item, empty);

		if (empty || item == null) {
			setText(null);
		} else {
			setText(converter.toString(item));
		}
	}
}