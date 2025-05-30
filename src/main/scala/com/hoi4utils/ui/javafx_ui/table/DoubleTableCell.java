package com.hoi4utils.ui.javafx_ui.table;

import javafx.scene.control.TableCell;
import javafx.util.StringConverter;

import java.text.DecimalFormat;

/**
 * A custom TableCell for displaying Double values with a specific decimal format.
 *
 * @param <S> The type of the TableView generic type
 */
public class DoubleTableCell<S> extends TableCell<S, Double> {

	private static final String DEFAULT_FORMAT = "#,##0.0#";
	private final DecimalFormat decimalFormat = new DecimalFormat(DEFAULT_FORMAT);

	private final StringConverter<Double> converter = new StringConverter<>() {
		@Override
		public String toString(Double object) {
			return object == null ? "" : decimalFormat.format(object);
		}

		@Override
		public Double fromString(String string) {
			// This cell is read-only; conversion from string is not supported.
			return null;
		}
	};

	@Override
	protected void updateItem(Double item, boolean empty) {
		super.updateItem(item, empty);
		setText(empty || item == null ? null : converter.toString(item));
	}
}
