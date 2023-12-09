package com.HOIIVUtils.hoi4utils.map.province;

import com.HOIIVUtils.hoi4utils.clausewitz_data.Localizable;

/**
 * localizable: victory point name
 */
public class VictoryPoint implements Localizable {
	int value;
	int province;       // todo final?

	public VictoryPoint(int value, int province) {
		this.value = value;
		this.province = province;
	}

	public int value() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}

	public int province() {
		return province;
	}

	public void setProvince(int province) {
		this.province = province;
	}


}
