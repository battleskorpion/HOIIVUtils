package com.HOIIVUtils.hoi4utils.clausewitz_map.province;

import com.HOIIVUtils.hoi4utils.clausewitz_data.localization.Localizable;

/**
 * localizable: victory point name
 */
public class VictoryPoint extends Province implements Localizable {
	int value;

	public VictoryPoint(int value, int province) {
		super(province);
		this.value = value;
	}

	public int value() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}

}
