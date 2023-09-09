package ui.table_data;

import javafx.scene.control.TableCell;
import javafx.util.StringConverter;
import java.text.DecimalFormat;

public class DoubleTableCell<S> extends TableCell<S, Double> {

	private final StringConverter<Double> converter;

	public DoubleTableCell() {
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