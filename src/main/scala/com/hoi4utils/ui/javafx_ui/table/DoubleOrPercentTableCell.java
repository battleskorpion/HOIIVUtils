package com.hoi4utils.ui.javafx_ui.table;

import javafx.scene.control.TableCell;
import javafx.util.StringConverter;

import java.text.DecimalFormat;
import java.text.NumberFormat;

public class DoubleOrPercentTableCell<S> extends TableCell<S, Double> {

	private boolean _double;
	private boolean percent;
	private static final String DEFAULT_FORMAT = "#,##0.0#";
	private static final String PERCENT_FORMAT = "#,##0";
	private final NumberFormat decimalFormat = new DecimalFormat(DEFAULT_FORMAT);
	private final NumberFormat percentFormat = NumberFormat.getPercentInstance();

	public DoubleOrPercentTableCell() {
		percentFormat.setMinimumFractionDigits(1);
	}

	private final StringConverter<Double> converter = new StringConverter<>() {
		@Override
		public String toString(Double object) {
			if (object == null) {
				return "";
			} else if (_double) {
				return decimalFormat.format(object);
			} else if (percent) {
				return percentFormat.format(object);
			} else {
				return decimalFormat.format(object);
			}
		}
		@Override
		public Double fromString(String string) {
			return null;
		}
	};
	
	public boolean getInteger() {
		return _double;
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
	public boolean setDouble(boolean _double) {
		this._double = _double;
		if (_double) {
			this.percent = false;
		}
		return _double;

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
			this._double = false;
		}
		return percent;
	}
	
	public boolean toggleInteger() {
		return setDouble(!_double);
	}

	public boolean togglePercent() {
		return setPercent(!percent);
	}

	@Override
	protected void updateItem(Double item, boolean empty) {
		super.updateItem(item, empty);
		setText(empty || item == null ? null : converter.toString(item));
	}
}
