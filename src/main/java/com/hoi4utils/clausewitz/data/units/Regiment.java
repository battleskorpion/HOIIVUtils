package com.hoi4utils.clausewitz.data.units;

public class Regiment {
	enum subunitType {
		COMBAT_BATALLION,
		SUPPORT_COMPANY
	}

	// etc.

	int x;      // position in template
	int y;      // position in template

	public Regiment(int x, int y) {
		this.x = x;
		this.y = y;
	}
}
