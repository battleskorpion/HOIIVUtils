package clausewitz_coding.state;

import clausewitz_coding.country.CountryTag;
/*
 * Owner File
 */
public record Owner (CountryTag tag) {

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
}
