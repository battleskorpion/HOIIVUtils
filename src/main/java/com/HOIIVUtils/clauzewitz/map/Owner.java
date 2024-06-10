package com.HOIIVUtils.clauzewitz.map;

import com.HOIIVUtils.clauzewitz.data.country.CountryTag;
import org.jetbrains.annotations.NotNull;

/*
 * Owner File
 */
public record Owner (CountryTag tag) implements Comparable<Owner> {

	/**
	 * returns whether the owner country represented by this record is the same country as represented by
	 * the 'other' country/owner
	 */
	public boolean isCountry(Object other) {
		if (other instanceof Owner) {
			return this.tag.equals(((Owner) other).tag);
		}
		if (other instanceof CountryTag) {
			return this.tag.equals(other);
		}

		return false;
	}

	@Override
	public int compareTo(@NotNull Owner o) {
		return this.tag.compareTo(o.tag);
	}
}
