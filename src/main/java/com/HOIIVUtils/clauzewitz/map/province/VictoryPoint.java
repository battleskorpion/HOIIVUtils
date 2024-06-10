package com.HOIIVUtils.clauzewitz.map.province;

import com.HOIIVUtils.clauzewitz.localization.Localizable;
import com.HOIIVUtils.clauzewitz.localization.Localization;

/**
 * localizable: victory point name
 */
public class VictoryPoint extends Province implements Localizable {
	int value;
	Localization vpName;

	public VictoryPoint(int province, int value) {
		this(province, value, null);
	}

	public VictoryPoint(int province, int value, Localization vpName) {
		super(province);
		this.value = value;
		this.vpName = vpName;
	}

	public int value() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}

	public Localization name() {
		return vpName;
	}

	public void setName(Localization vpName) {
		this.vpName = vpName;
	}

}
