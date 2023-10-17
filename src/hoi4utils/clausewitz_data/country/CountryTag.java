package hoi4utils.clausewitz_data.country;

import java.util.Objects;
public final class CountryTag {
	public static final CountryTag NULL_TAG = new CountryTag("###");
	public static final int COUNTRY_TAG_LENGTH = 3;         // standard country tag length (for a normal country tag)
	private final String tag;

	public CountryTag(String tag) {
		if (tag == null || tag.equals("")) {
			tag = NULL_TAG.tag;
		}

		this.tag = tag;
	}

	public String toString() {
		return tag;
	}

	// todo make this adhere to equals implementation requirements
	public boolean equals(Object other) {
		if (other.getClass() == this.getClass()) {
			return this.tag.equals(((CountryTag) other).tag);
		}
		else if (other.getClass() == String.class) {
			return this.tag.equals(other);
		}

		return false;
	}

	@Override
	public int hashCode() {
		return Objects.hash(tag);
	}
}
