package com.HOIIVUtils.clauzewitz.map.province;

import java.util.ArrayList;
import java.util.List;

public class Province {
	static List<Province> provinces = new ArrayList<>();
	int id;

	protected Province(int provinceid) throws IllegalArgumentException {
		if (provinceid < 0) {
			throw new IllegalArgumentException("Province id must be non-negative");
		}
		this.id = provinceid;
	}

	public static Province of(int provinceid) throws IllegalArgumentException {
		for (Province province : provinces) {
			if (province.id == provinceid) {
				return province;
			}
		}
		return new Province(provinceid);
	}
}
