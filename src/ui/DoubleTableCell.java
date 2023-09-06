package ui;

import javafx.scene.control.TableCell;
import javafx.util.StringConverter;

public class DoubleTableCell<S> extends TableCell<S, Double> {

	private final StringConverter<Double> converter;

	public DoubleTableCell() {
		this.converter = new StringConverter<Double>() {
			@Override
			public String toString(Double object) {
				if (object == null) {
					return "";
				} else {
					// Format the double value with up to 2 decimal places and a maximum of 1 trailing zero
					String formattedValue = String.format("%.2f", object);
					formattedValue = formattedValue.replaceAll("0*$", "");
					formattedValue = formattedValue.replaceAll("\\.$", "");
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