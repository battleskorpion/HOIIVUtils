package com.HOIIVUtils.hoi4utils.clausewitz_data.country;

import org.jetbrains.annotations.NotNull;

public record CountryTag(String tag) implements Comparable<CountryTag> {
	public static final CountryTag NULL_TAG = new CountryTag("###");
	public static final int COUNTRY_TAG_LENGTH = 3;         // standard country tag length (for a normal country tag)

	public CountryTag {
		if (tag == null || tag.isEmpty()) {
			tag = NULL_TAG.tag;
		}
	}


	@Override
	public String toString() {
		return tag;
	}

	// todo make this adhere to equals implementation requirements
	@Override
	public boolean equals(Object other) {
		if (other.getClass() == this.getClass()) {
			return this.tag.equals(((CountryTag) other).tag);
		} else if (other.getClass() == String.class) {
			return this.tag.equals(other);
		}

		return false;
	}

	@Override
	public int compareTo(@NotNull CountryTag o) {
		return this.tag.compareTo(o.tag);
	}
}
